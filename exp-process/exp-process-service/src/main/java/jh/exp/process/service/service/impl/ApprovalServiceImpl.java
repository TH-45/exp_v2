package jh.exp.process.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jh.exp.auth.clinet.api.bus.PersonService;
import jh.exp.auth.core.entity.res.PersonDetailRes;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.auth.CurrentUserHolder;
import jh.exp.common.core.auth.dto.CurrentUser;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.process.core.constant.ProcessConstant;
import jh.exp.process.core.entity.WfInstance;
import jh.exp.process.core.entity.WfNodeDefinition;
import jh.exp.process.core.entity.WfProcessDefinition;
import jh.exp.process.core.entity.WfTask;
import jh.exp.process.core.entity.WfTaskAttachment;
import jh.exp.process.core.entity.dto.TaskHandleContextDTO;
import jh.exp.process.core.entity.dto.BusParamBase;
import jh.exp.process.core.entity.req.ApprovalActionReq;
import jh.exp.process.core.entity.req.ApprovalTaskQueryReq;
import jh.exp.process.core.entity.req.ForceCloseReq;
import jh.exp.process.core.entity.req.StartProcessReq;
import jh.exp.process.core.entity.res.ApprovalDetailRes;
import jh.exp.process.core.entity.res.ApprovalHistoryRes;
import jh.exp.process.core.entity.res.ApprovalStatsRes;
import jh.exp.process.core.entity.res.ApprovalTaskRes;
import jh.exp.process.core.entity.res.AttachmentRes;
import jh.exp.process.core.mapper.WfInstanceMapper;
import jh.exp.process.core.mapper.WfNodeDefinitionMapper;
import jh.exp.process.core.mapper.WfProcessDefinitionMapper;
import jh.exp.process.core.mapper.WfTaskAttachmentMapper;
import jh.exp.process.core.mapper.WfTaskMapper;
import jh.exp.process.service.driver.ProcessBusinessDriver;
import jh.exp.process.service.driver.ProcessBusinessDriverRegistry;
import jh.exp.process.service.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private final WfProcessDefinitionMapper processDefinitionMapper;
    private final WfNodeDefinitionMapper nodeDefinitionMapper;
    private final WfInstanceMapper instanceMapper;
    private final WfTaskMapper taskMapper;
    private final WfTaskAttachmentMapper taskAttachmentMapper;
    private final PersonService personService;
    private final ProcessBusinessDriverRegistry driverRegistry;

    @Override
    @Transactional
    public Long create(StartProcessReq req) {
        CurrentUser currentUser = requireCurrentUser();
        // 查询流程定义
        WfProcessDefinition definition = resolveDefinition(req);
        //获取首节点配置
        WfNodeDefinition firstNode = getFirstNode(definition.getProcDefId());
        //创建实例
        if (firstNode == null) {
            throw new RuntimeException("流程未配置节点");
        }
        LocalDateTime now = LocalDateTime.now();
        WfInstance instance = new WfInstance();
        instance.setProcDefId(definition.getProcDefId());
        instance.setBusId(req.getBusId());
        instance.setStarterId(currentUser.getUserId());
        instance.setStartTime(now);
        instance.setStatus(ProcessConstant.INSTANCE_APPROVING);
        instance.setTitle(req.getTitle());
        instanceMapper.insert(instance);

        //创建待办
        createPendingTask(instance.getInstanceId(), firstNode);
        return instance.getInstanceId();
    }



    @Override
    public ApprovalStatsRes getStats() {
        CurrentUser currentUser = requireCurrentUser();
        PersonDetailRes person = getCurrentPerson(currentUser.getUserId());
        ApprovalStatsRes res = new ApprovalStatsRes();
        res.setTodoCount(0L);
        res.setDoneCount(0L);
        res.setStartedCount(0L);
        res.setClosedCount(0L);
        if (person == null || person.getPersonId() == null) {
            return res;
        }
        ApprovalTaskQueryReq emptyQuery = new ApprovalTaskQueryReq();
        SimplePageReq<ApprovalTaskQueryReq> pageReq = new SimplePageReq<>(1, 1, null, emptyQuery);
        res.setTodoCount(buildTodoPage(pageReq, emptyQuery, person).getTotal());
        res.setDoneCount(buildDonePage(pageReq, emptyQuery, person).getTotal());
        res.setStartedCount(buildStartedPage(pageReq, emptyQuery, currentUser.getUserId(), null).getTotal());
        res.setClosedCount(buildStartedPage(pageReq, emptyQuery, currentUser.getUserId(), ProcessConstant.INSTANCE_CLOSED).getTotal());
        return res;
    }

    @Override
    public SimplePageRes<ApprovalTaskRes> listTasks(SimplePageReq<ApprovalTaskQueryReq> req) {
        req.pageDefault();
        CurrentUser currentUser = requireCurrentUser();
        PersonDetailRes person = getCurrentPerson(currentUser.getUserId());
        ApprovalTaskQueryReq query = req.getQueryParam() == null ? new ApprovalTaskQueryReq() : req.getQueryParam();
        // 前端 keyword 映射到 instanceTitle 模糊查询
        if (query.getKeyword() != null && !query.getKeyword().isBlank()
                && (query.getInstanceTitle() == null || query.getInstanceTitle().isBlank())) {
            query.setInstanceTitle(query.getKeyword());
        }
        String tab = normalizeTab(query.getTab());

        // 待办/已办绑定人员ID；我发起/已关闭绑定用户ID
        if (ProcessConstant.DIRECTION_TODO.equalsIgnoreCase(tab)) {
            //待办
            return buildTodoPage(req, query, person);
        } else if (ProcessConstant.DIRECTION_DONE.equalsIgnoreCase(tab)) {
            //已办
            return buildDonePage(req, query, person);
        } else if (ProcessConstant.DIRECTION_START.equalsIgnoreCase(tab)) {
            // 我发起：全部实例
            return buildStartedPage(req, query, currentUser.getUserId(), null);
        } else {
            // 我关闭：仅已关闭实例
            return buildStartedPage(req, query, currentUser.getUserId(), ProcessConstant.INSTANCE_CLOSED);
        }
    }

    @Override
    public ApprovalDetailRes detail(Long taskId) {
        WfTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        WfInstance instance = instanceMapper.selectById(task.getInstanceId());
        if (instance == null) {
            throw new RuntimeException("流程实例不存在");
        }
        WfNodeDefinition node = nodeDefinitionMapper.selectById(task.getNodeId());

        WfProcessDefinition definition = processDefinitionMapper.selectById(instance.getProcDefId());
        ProcessBusinessDriver handler= driverRegistry.route(definition.getBusType(), definition.getProcCode());
        //获取业务数据
        Object businessData = handler.getBusinessData(new BusParamBase(instance.getBusId()));

        ApprovalDetailRes res = new ApprovalDetailRes();
        res.setTaskId(task.getTaskId());
        res.setInstanceId(instance.getInstanceId());
        res.setBusId(instance.getBusId());
        res.setBusType(definition.getBusType());
        res.setStatus(instance.getStatus());
        res.setCurrentNode(node == null ? "-" : node.getNodeName());
        res.setStarterId(instance.getStarterId());
        res.setBusinessData(businessData);
        res.setApprovalHistory(history(taskId));
        res.setAttachments(listAttachments(task.getTaskId()));
        res.setTitle(instance.getTitle());
        return res;
    }

    @Override
    public List<ApprovalHistoryRes> history(Long taskId) {
        // 1. 获取基础任务信息
        WfTask currentTask = taskMapper.selectById(taskId);
        if (currentTask == null) return List.of();

        // 2. 一次性查出该流程实例下所有的任务记录（包含已办、驳回、待办）
        // 按时间升序，这样返回给前端的就是一个完整的审批链路
        List<WfTask> allTasks = taskMapper.selectList(
                new LambdaQueryWrapper<WfTask>()
                        .eq(WfTask::getInstanceId, currentTask.getInstanceId())
                        .orderByAsc(WfTask::getCreateTime)
        );
        if (allTasks.isEmpty()) return List.of();

        // 3. 提取所有涉及到的节点 ID
        Set<Long> nodeIds = allTasks.stream()
                .map(WfTask::getNodeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 4. 批量查询节点定义（空间换时间）
        Map<Long, String> nodeMap = new HashMap<>();
        if (!nodeIds.isEmpty()) {
            List<WfNodeDefinition> nodes = nodeDefinitionMapper.selectList(
                    new LambdaQueryWrapper<WfNodeDefinition>()
                            .in(WfNodeDefinition::getNodeId, nodeIds)
            );
            // 这里 ID 是唯一的，直接转 Map
            nodeMap = nodes.stream().collect(Collectors.toMap(
                    WfNodeDefinition::getNodeId,
                    WfNodeDefinition::getNodeName,
                    (v1, v2) -> v1
            ));
        }

        // 5. 组装结果：保留每一条任务记录
        Map<Long, String> finalNodeMap = nodeMap;
        return allTasks.stream().map(taskItem -> {
            ApprovalHistoryRes res = new ApprovalHistoryRes();
            BeanUtils.copyProperties(taskItem, res);

            // 核心：即便同一个节点 nodeId 出现了多次，
            // 这里依然能通过 Map 拿到它对应的节点名称
            res.setNodeName(finalNodeMap.getOrDefault(taskItem.getNodeId(), "-"));

            // 注意：建议在 ApprovalHistoryRes 中保留 taskItem 的审批意见、处理结果等字段
            // 这样前端才能区分哪一条是“驳回”，哪一条是“通过”
            return res;
        }).toList();
    }

    @Override
    @Transactional
    public void approve(ApprovalActionReq req) {
        if (req.getTaskId() == null) {
            throw new RuntimeException("任务ID不能为空");
        }
        doHandle(req.getTaskId(), ProcessConstant.ACTION_AGREE, req.getComments(), req.getAttachments());
    }

    @Override
    @Transactional
    public void reject(ApprovalActionReq req) {
        if (req.getTaskId() == null) {
            throw new RuntimeException("任务ID不能为空");
        }
        if (req.getComments() == null || req.getComments().isBlank()) {
            throw new RuntimeException("拒绝意见不能为空");
        }
        doHandle(req.getTaskId(), ProcessConstant.ACTION_REJECT, req.getComments(), req.getAttachments());
    }

    @Override
    @Transactional
    public void returnToPrev(ApprovalActionReq req) {
        if (req.getTaskId() == null) {
            throw new RuntimeException("任务ID不能为空");
        }
        if (req.getComments() == null || req.getComments().isBlank()) {
            throw new RuntimeException("驳回意见不能为空");
        }
        doHandle(req.getTaskId(), ProcessConstant.ACTION_RETURN, req.getComments(), req.getAttachments());
    }

    @Override
    @Transactional
    public void batchApprove(ApprovalActionReq req) {
//        if (req.getTaskIds() == null || req.getTaskIds().isEmpty()) {
//            return;
//        }
//        for (Long taskId : req.getTaskIds()) {
//            doHandle(taskId, ProcessConstant.ACTION_AGREE, req.getComments(), req.getAttachments());
//        }
    }

    @Override
    @Transactional
    public void batchReject(ApprovalActionReq req) {
//        if (req.getTaskIds() == null || req.getTaskIds().isEmpty()) {
//            return;
//        }
//        if (req.getComments() == null || req.getComments().isBlank()) {
//            throw new RuntimeException("批量驳回意见不能为空");
//        }
//        for (Long taskId : req.getTaskIds()) {
//            doHandle(taskId, ProcessConstant.ACTION_REJECT, req.getComments(), req.getAttachments());
//        }
    }

    @Override
    @Transactional
    public void forceClose(ForceCloseReq req) {
        CurrentUser currentUser = requireCurrentUser();
        WfInstance instance = instanceMapper.selectById(req.getInstanceId());
        if (instance == null) {
            throw new RuntimeException("实例不存在");
        }
        if (!Objects.equals(instance.getStarterId(), currentUser.getUserId())) {
            throw new RuntimeException("仅发起人可强制关闭");
        }
        // 仅审批中的实例可被发起人强制关闭
        if (!ProcessConstant.INSTANCE_APPROVING.equals(instance.getStatus())) {
            throw new RuntimeException("当前流程状态不允许关闭");
        }

        // 更新流程实例状态，关闭实例
        LocalDateTime now = LocalDateTime.now();
        instance.setStatus(ProcessConstant.INSTANCE_CLOSED);
        instance.setEndTime(now);
        instance.setClosedBy(currentUser.getUserId());
        instance.setCloseReason(req.getReason());
        instanceMapper.updateById(instance);

        taskMapper.update(
                null,
                new LambdaUpdateWrapper<WfTask>()
                        .eq(WfTask::getInstanceId, instance.getInstanceId())
                        .eq(WfTask::getIsDone, 0)
                        .set(WfTask::getIsDone, 1)
                        .set(WfTask::getAction, ProcessConstant.ACTION_CLOSE)
                        .set(WfTask::getHandlerId, currentUser.getUserId())
                        .set(WfTask::getOpinion, req.getReason())
                        .set(WfTask::getFinishTime, now)
        );
    }

    private void doHandle(Long taskId, String action, String comments, List<ApprovalActionReq.AttachmentItem> attachments) {
        CurrentUser currentUser = requireCurrentUser();
        PersonDetailRes person = getCurrentPerson(currentUser.getUserId());

        // 一次查询下沉到 XML：task + instance，条件 is_done=0 且 status=APPROVING
        TaskHandleContextDTO ctx = taskMapper.selectTaskHandleContext(taskId);
        if (ctx == null) {
            throw new RuntimeException("任务不存在或当前不可处理");
        }
        WfTask task = buildTaskFromHandleContext(ctx);
        WfInstance instance = buildInstanceFromHandleContext(ctx);


        // 更新任务，结束当前任务
        LocalDateTime now = LocalDateTime.now();
        task.setIsDone(1);
        task.setAction(action);
        task.setOpinion(comments);
        task.setHandlerId(currentUser.getUserId());
        task.setFinishTime(now);
        taskMapper.updateById(task);
        saveAttachments(task.getTaskId(), attachments, currentUser.getUserId());

        //获取下一个审批节点配置
        WfNodeDefinition currentNode = nodeDefinitionMapper.selectById(task.getNodeId());
        if (currentNode == null) {
            throw new RuntimeException("节点配置不存在");
        }
        if (ProcessConstant.ACTION_AGREE.equals(action)) {
            // 同意
            WfNodeDefinition next = nextNode(currentNode.getProcDefId(), currentNode.getSortNo());
            if (next == null) {
                //无下一个节点关闭示例
                instance.setStatus(ProcessConstant.INSTANCE_COMPLETED);
                instance.setEndTime(now);
                instanceMapper.updateById(instance);
            } else {
                createPendingTask(instance.getInstanceId(), next);
            }
            return;
        } else if (ProcessConstant.ACTION_REJECT.equals(action)) {
            // 拒绝：直接关闭实例，不再流转
            instance.setStatus(ProcessConstant.INSTANCE_REJECTED);
            instance.setEndTime(now);
            instance.setClosedBy(currentUser.getUserId());
            instance.setCloseReason(comments);
            instanceMapper.updateById(instance);
            return;
        } else if (ProcessConstant.ACTION_RETURN.equals(action)) {
            // 驳回：默认回上一节点；若上一节点是初始审批节点，则关闭实例，由提单人重新提交流程
            WfNodeDefinition prev = prevNode(currentNode.getProcDefId(), currentNode.getSortNo());
            WfNodeDefinition first = getFirstNode(currentNode.getProcDefId());
            if (first == null) {
                throw new RuntimeException("流程未配置初始审批节点");
            }
            boolean shouldClose = prev == null || Objects.equals(prev.getNodeId(), first.getNodeId());
            if (shouldClose) {
                instance.setStatus(ProcessConstant.INSTANCE_CLOSED);
                instance.setEndTime(now);
                instance.setClosedBy(currentUser.getUserId());
                instance.setCloseReason(comments);
                instanceMapper.updateById(instance);
                return;
            }
            createPendingTask(instance.getInstanceId(), prev);
            return;
        }
        throw new RuntimeException("不支持的审批动作: " + action);

    }

    /** 从 TaskHandleContextDTO 构建 WfTask */
    private WfTask buildTaskFromHandleContext(TaskHandleContextDTO ctx) {
        WfTask task = new WfTask();
        task.setTaskId(ctx.getTaskId());
        task.setInstanceId(ctx.getInstanceId());
        task.setNodeId(ctx.getNodeId());
        task.setCandidateType(ctx.getCandidateType());
        task.setCandidateId(ctx.getCandidateId());
        task.setHandlerId(ctx.getHandlerId());
        task.setAction(ctx.getAction());
        task.setOpinion(ctx.getOpinion());
        task.setIsDone(ctx.getIsDone());
        task.setCreateTime(ctx.getCreateTime());
        task.setFinishTime(ctx.getFinishTime());
        return task;
    }

    /** 从 TaskHandleContextDTO 构建 WfInstance */
    private WfInstance buildInstanceFromHandleContext(TaskHandleContextDTO ctx) {
        WfInstance instance = new WfInstance();
        instance.setInstanceId(ctx.getInstanceId());
        instance.setProcDefId(ctx.getInstanceProcDefId());
        instance.setBusId(ctx.getInstanceBusId());
        instance.setStarterId(ctx.getInstanceStarterId());
        instance.setStartTime(ctx.getInstanceStartTime());
        instance.setEndTime(ctx.getInstanceEndTime());
        instance.setStatus(ctx.getInstanceStatus());
        instance.setClosedBy(ctx.getInstanceClosedBy());
        instance.setCloseReason(ctx.getInstanceCloseReason());
        instance.setTitle(ctx.getInstanceTitle());
        return instance;
    }

    private void saveAttachments(Long taskId, List<ApprovalActionReq.AttachmentItem> attachments, Long userId) {
        if (attachments == null || attachments.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (ApprovalActionReq.AttachmentItem item : attachments) {
            if (item.getUrl() == null || item.getUrl().isBlank()) {
                continue;
            }
            WfTaskAttachment attachment = new WfTaskAttachment();
            attachment.setTaskId(taskId);
            attachment.setFileName(item.getName());
            attachment.setFileUrl(item.getUrl());
            attachment.setFileSize(item.getSize());
            attachment.setCreatedBy(userId);
            attachment.setCreatedTime(now);
            taskAttachmentMapper.insert(attachment);
        }
    }

    private List<AttachmentRes> listAttachments(Long taskId) {
        return taskAttachmentMapper.selectList(
                new LambdaQueryWrapper<WfTaskAttachment>()
                        .eq(WfTaskAttachment::getTaskId, taskId)
                        .orderByAsc(WfTaskAttachment::getCreatedTime)
        ).stream().map(item -> {
            AttachmentRes res = new AttachmentRes();
            res.setId(item.getId());
            res.setName(item.getFileName());
            res.setUrl(item.getFileUrl());
            res.setSize(item.getFileSize());
            res.setUploadTime(item.getCreatedTime());
            return res;
        }).toList();
    }

    /**
     * 待办任务真分页（candidate_id 绑定人员ID）
     */
    private SimplePageRes<ApprovalTaskRes> buildTodoPage(SimplePageReq<ApprovalTaskQueryReq> req,
                                                         ApprovalTaskQueryReq query, PersonDetailRes person) {
        if (person == null || person.getPersonId() == null) {
            return new SimplePageRes<>(0L, (long) req.getPageNum(), (long) req.getPageSize(), List.of());
        }
        Page<WfTask> page = new Page<>(req.getPageNum(), req.getPageSize());
        IPage<WfTask> result = taskMapper.selectTodoPage(page, person.getPersonId(), query);
        List<ApprovalTaskRes> rows = batchPopulateTaskRes(result.getRecords());
        return new SimplePageRes<>(result.getTotal(), result.getCurrent(), result.getSize(), rows);
    }

    /**
     * 已办任务真分页（handler_id 绑定人员ID）
     */
    private SimplePageRes<ApprovalTaskRes> buildDonePage(SimplePageReq<ApprovalTaskQueryReq> req,
                                                         ApprovalTaskQueryReq query, PersonDetailRes person) {
        if (person == null || person.getPersonId() == null) {
            return new SimplePageRes<>(0L, (long) req.getPageNum(), (long) req.getPageSize(), List.of());
        }
        Page<WfTask> page = new Page<>(req.getPageNum(), req.getPageSize());
        IPage<WfTask> result = taskMapper.selectDonePage(page, person.getPersonId(), query);
        List<ApprovalTaskRes> rows = batchPopulateTaskRes(result.getRecords());
        return new SimplePageRes<>(result.getTotal(), result.getCurrent(), result.getSize(), rows);
    }

    /**
     * 我发起/已关闭实例真分页，包含“到哪一步了”和“谁在审”
     */
    private SimplePageRes<ApprovalTaskRes> buildStartedPage(SimplePageReq<ApprovalTaskQueryReq> req,
                                                          ApprovalTaskQueryReq query, Long userId,
                                                          String instanceStatus) {
        Page<WfInstance> page = new Page<>(req.getPageNum(), req.getPageSize());
        IPage<WfInstance> result = instanceMapper.selectStartedPage(page, userId, instanceStatus, query);
        List<WfInstance> instances = result.getRecords();
        if (instances.isEmpty()) {
            return new SimplePageRes<>(0L, (long) req.getPageNum(), (long) req.getPageSize(), List.of());
        }

        Set<Long> instanceIds = instances.stream().map(WfInstance::getInstanceId).collect(Collectors.toSet());
        // 批量查询本页实例下的待办任务
        List<WfTask> allTodoTasks = taskMapper.selectList(
                new LambdaQueryWrapper<WfTask>()
                        .in(WfTask::getInstanceId, instanceIds)
                        .eq(WfTask::getIsDone, 0)
        );
        Map<Long, List<WfTask>> taskGroupMap = allTodoTasks.stream().collect(Collectors.groupingBy(WfTask::getInstanceId));

        // 无待办的实例需取任意任务ID供详情查看，批量查询所有任务后按 instanceId 分组取每个实例最后一条
        List<WfTask> allTasks = taskMapper.selectList(
                new LambdaQueryWrapper<WfTask>().in(WfTask::getInstanceId, instanceIds).orderByDesc(WfTask::getFinishTime));
        Map<Long, Long> instanceToTaskIdMap = allTasks.stream()
                .collect(Collectors.toMap(WfTask::getInstanceId, WfTask::getTaskId, (a, b) -> a));

        Set<Long> defIds = instances.stream().map(WfInstance::getProcDefId).collect(Collectors.toSet());
        Map<Long, WfProcessDefinition> defMap = defIds.isEmpty() ? Map.of() :
                processDefinitionMapper.selectList(new LambdaQueryWrapper<WfProcessDefinition>().in(WfProcessDefinition::getProcDefId, defIds))
                        .stream().collect(Collectors.toMap(WfProcessDefinition::getProcDefId, d -> d));

        Set<Long> nodeIds = allTodoTasks.stream().map(WfTask::getNodeId).collect(Collectors.toSet());
        Map<Long, String> nodeNameMap = nodeIds.isEmpty() ? Map.of() :
                nodeDefinitionMapper.selectList(new LambdaQueryWrapper<WfNodeDefinition>().in(WfNodeDefinition::getNodeId, nodeIds))
                        .stream().collect(Collectors.toMap(WfNodeDefinition::getNodeId, WfNodeDefinition::getNodeName));

        List<ApprovalTaskRes> rows = instances.stream().map(instance -> {
            ApprovalTaskRes row = new ApprovalTaskRes();
            row.setInstanceId(instance.getInstanceId());
            row.setBusId(instance.getBusId());
            row.setStarterId(instance.getStarterId());
            row.setStartTime(instance.getStartTime());
            row.setStatus(instance.getStatus());
            row.setIsDone(1);
            row.setTitle(instance.getTitle());

            WfProcessDefinition def = defMap.get(instance.getProcDefId());
            if (def != null) {
                row.setBusType(def.getBusType());
            }

            List<WfTask> currentTasks = taskGroupMap.get(instance.getInstanceId());
            if (currentTasks != null && !currentTasks.isEmpty()) {
                row.setTaskId(currentTasks.get(0).getTaskId());
                row.setCurrentNode(nodeNameMap.getOrDefault(currentTasks.get(0).getNodeId(), "未知节点"));
                row.setCurrentHandler(currentTasks.stream().map(WfTask::getCandidateId).distinct().collect(Collectors.joining(", ")));
            } else {
                row.setCurrentNode(ProcessConstant.INSTANCE_COMPLETED.equals(instance.getStatus()) ? "流程已结束" : "-");
                row.setTaskId(instanceToTaskIdMap.get(instance.getInstanceId()));
            }
            return row;
        }).toList();

        return new SimplePageRes<>(result.getTotal(), result.getCurrent(), result.getSize(), rows);
    }

    /**
     * 公共方法：批量填充 TaskRes 关联信息，解决 N+1 问题
     */
    private List<ApprovalTaskRes> batchPopulateTaskRes(List<WfTask> tasks) {
        if (tasks == null || tasks.isEmpty()) return List.of();

        // --- 1. 批量收集 IDs ---
        Set<Long> instanceIds = tasks.stream().map(WfTask::getInstanceId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> nodeIds = tasks.stream().map(WfTask::getNodeId).filter(Objects::nonNull).collect(Collectors.toSet());

        // --- 2. 批量查询关联表并转为 Map ---
        // 流程实例 Map
        Map<Long, WfInstance> instanceMap = instanceIds.isEmpty() ? Map.of() :
                instanceMapper.selectList(new LambdaQueryWrapper<WfInstance>().in(WfInstance::getInstanceId, instanceIds))
                        .stream().collect(Collectors.toMap(WfInstance::getInstanceId, i -> i));

        // 流程定义 Map (基于实例中的 procDefId)
        Set<Long> defIds = instanceMap.values().stream().map(WfInstance::getProcDefId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, WfProcessDefinition> defMap = defIds.isEmpty() ? Map.of() :
                processDefinitionMapper.selectList(new LambdaQueryWrapper<WfProcessDefinition>().in(WfProcessDefinition::getProcDefId, defIds))
                        .stream().collect(Collectors.toMap(WfProcessDefinition::getProcDefId, d -> d));

        // 节点名称 Map
        Map<Long, String> nodeNameMap = nodeIds.isEmpty() ? Map.of() :
                nodeDefinitionMapper.selectList(new LambdaQueryWrapper<WfNodeDefinition>().in(WfNodeDefinition::getNodeId, nodeIds))
                        .stream().collect(Collectors.toMap(WfNodeDefinition::getNodeId, WfNodeDefinition::getNodeName));

        // --- 3. 内存映射组装 ---
        return tasks.stream().map(task -> {
            ApprovalTaskRes row = new ApprovalTaskRes();
            row.setTaskId(task.getTaskId());
            row.setInstanceId(task.getInstanceId());
            row.setIsDone(task.getIsDone());
            // 如果是已办，可能还需要 finishTime
            // row.setFinishTime(task.getFinishTime());

            WfInstance instance = instanceMap.get(task.getInstanceId());
            if (instance != null) {
                row.setBusId(instance.getBusId());
                row.setStarterId(instance.getStarterId());
                row.setStartTime(instance.getStartTime());
                row.setStatus(instance.getStatus());
                row.setTitle(instance.getTitle());

                WfProcessDefinition definition = defMap.get(instance.getProcDefId());
                if (definition != null) {
                    row.setBusType(definition.getBusType());
                }
            }
            row.setCurrentNode(nodeNameMap.getOrDefault(task.getNodeId(), "-"));
            return row;
        }).toList();
    }

    private ApprovalTaskRes toTaskRes(WfTask task) {
        ApprovalTaskRes row = new ApprovalTaskRes();
        row.setTaskId(task.getTaskId());
        row.setInstanceId(task.getInstanceId());
        row.setIsDone(task.getIsDone());

        WfInstance instance = instanceMapper.selectById(task.getInstanceId());
        if (instance != null) {
            row.setBusId(instance.getBusId());
            row.setStarterId(instance.getStarterId());
            row.setStartTime(instance.getStartTime());
            row.setStatus(instance.getStatus());
            row.setTitle(instance.getTitle());
            WfProcessDefinition definition = processDefinitionMapper.selectById(instance.getProcDefId());
            if (definition != null) {
                row.setBusType(definition.getBusType());

            }
        }
        WfNodeDefinition node = nodeDefinitionMapper.selectById(task.getNodeId());
        row.setCurrentNode(node == null ? "-" : node.getNodeName());
        return row;
    }



    private WfProcessDefinition resolveDefinition(StartProcessReq req) {
        if(req==null||req.getBusType().isBlank()){
            throw new RuntimeException("流程类型不存在");
        }
        // 查询流程定义
        WfProcessDefinition definition = processDefinitionMapper.selectOne(
                new LambdaQueryWrapper<WfProcessDefinition>()
                        .eq(WfProcessDefinition::getProcCode, req.getProcCode())
                        .eq(WfProcessDefinition::getBusType, req.getBusType())
                        .eq(WfProcessDefinition::getIsActive, 1)
                        .last("limit 1")

        );
        if (definition == null) {
            throw new RuntimeException("流程定义不存在");
        }
        return definition;
    }

    private WfNodeDefinition getFirstNode(Long procDefId) {
        return nodeDefinitionMapper.selectOne(
                new LambdaQueryWrapper<WfNodeDefinition>()
                        .eq(WfNodeDefinition::getProcDefId, procDefId)
                        .orderByAsc(WfNodeDefinition::getSortNo)
                        .last("limit 1")
        );
    }

    private WfNodeDefinition nextNode(Long procDefId, Integer currentSortNo) {
        return nodeDefinitionMapper.selectOne(
                new LambdaQueryWrapper<WfNodeDefinition>()
                        .eq(WfNodeDefinition::getProcDefId, procDefId)
                        .gt(WfNodeDefinition::getSortNo, currentSortNo)
                        .orderByAsc(WfNodeDefinition::getSortNo)
                        .last("limit 1")
        );
    }

    // 获取上一个节点
    private WfNodeDefinition prevNode(Long procDefId, Integer currentSortNo) {
        return nodeDefinitionMapper.selectOne(
                new LambdaQueryWrapper<WfNodeDefinition>()
                        .eq(WfNodeDefinition::getProcDefId, procDefId)
                        .lt(WfNodeDefinition::getSortNo, currentSortNo)
                        .orderByDesc(WfNodeDefinition::getSortNo)
                        .last("limit 1")
        );
    }

    // 创建待办任务
    private void createPendingTask(Long instanceId, WfNodeDefinition node) {
        WfTask task = new WfTask();
        task.setInstanceId(instanceId);
        task.setNodeId(node.getNodeId());
        //废弃
//        task.setCandidateType(node.getAssigneeType());
        task.setCandidateId(node.getAssigneeId());
        task.setIsDone(0);
        task.setAction(ProcessConstant.ACTION_APPROVE);
        task.setCreateTime(LocalDateTime.now());
        taskMapper.insert(task);
    }

    /** 前端 tab 值（todo/done/started/closed）转后端常量（TODO/DONE/START/CLOSE） */
    private String normalizeTab(String tab) {
        if (tab == null || tab.isBlank()) return ProcessConstant.DIRECTION_TODO;
        String t = tab.trim().toUpperCase();
        if ("STARTED".equals(t)) return ProcessConstant.DIRECTION_START;
        if ("CLOSED".equals(t)) return ProcessConstant.DIRECTION_CLOSE;
        return t;
    }

    private CurrentUser requireCurrentUser() {
        CurrentUser currentUser = CurrentUserHolder.get();
        if (currentUser == null || currentUser.getUserId() == null) {
            throw new RuntimeException("未登录或登录状态失效");
        }
        return currentUser;
    }

    private PersonDetailRes getCurrentPerson(Long userId) {
        try {
            ApiResponse<PersonDetailRes> resp = personService.getPersonById(userId);
            return (resp != null && resp.isSuccess()) ? resp.getData() : null;
        } catch (Exception ex) {
            return null;
        }
    }


}
