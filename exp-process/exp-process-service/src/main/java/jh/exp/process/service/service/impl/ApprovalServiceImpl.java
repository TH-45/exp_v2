package jh.exp.process.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jh.exp.auth.clinet.api.bus.PersonService;
import jh.exp.auth.core.entity.res.PersonDetailRes;
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
import jh.exp.process.service.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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

    @Override
    @Transactional
    public Long start(StartProcessReq req) {
        CurrentUser currentUser = requireCurrentUser();
        WfProcessDefinition definition = resolveDefinition(req);
        WfNodeDefinition firstNode = getFirstNode(definition.getProcDefId());
        if (firstNode == null) {
            throw new RuntimeException("流程未配置节点");
        }

        LocalDateTime now = LocalDateTime.now();
        WfInstance instance = new WfInstance();
        instance.setProcDefId(definition.getProcDefId());
        instance.setBusId(req.getBusId());
        instance.setStarterId(currentUser.getUserId());
        instance.setStartTime(now);
        instance.setStatus(ProcessConstant.INSTANCE_RUNNING);
        instanceMapper.insert(instance);

        createPendingTask(instance.getInstanceId(), firstNode);
        return instance.getInstanceId();
    }

    @Override
    public ApprovalStatsRes stats() {
        CurrentUser currentUser = requireCurrentUser();
        PersonDetailRes person = getCurrentPerson(currentUser.getUserId());

        ApprovalStatsRes res = new ApprovalStatsRes();
        res.setTodoCount(countTodo(currentUser, person));
        res.setDoneCount(taskMapper.selectCount(
                new LambdaQueryWrapper<WfTask>()
                        .eq(WfTask::getIsDone, 1)
                        .eq(WfTask::getHandlerId, currentUser.getUserId())
        ));
        res.setStartedCount(instanceMapper.selectCount(
                new LambdaQueryWrapper<WfInstance>().eq(WfInstance::getStarterId, currentUser.getUserId())
        ));
        res.setClosedCount(instanceMapper.selectCount(
                new LambdaQueryWrapper<WfInstance>()
                        .eq(WfInstance::getStarterId, currentUser.getUserId())
                        .eq(WfInstance::getStatus, ProcessConstant.INSTANCE_CLOSED)
        ));
        return res;
    }

    @Override
    public SimplePageRes<ApprovalTaskRes> listTasks(SimplePageReq<ApprovalTaskQueryReq> req) {
        req.pageDefault();
        CurrentUser currentUser = requireCurrentUser();
        PersonDetailRes person = getCurrentPerson(currentUser.getUserId());
        ApprovalTaskQueryReq query = req.getQueryParam() == null ? new ApprovalTaskQueryReq() : req.getQueryParam();

        String tab = query.getTab() == null ? "todo" : query.getTab();
        List<ApprovalTaskRes> allRows = new ArrayList<>();
        if ("todo".equalsIgnoreCase(tab)) {
            allRows = buildTodoRows(currentUser, person);
        } else if ("done".equalsIgnoreCase(tab)) {
            allRows = buildDoneRows(currentUser);
        } else if ("closed".equalsIgnoreCase(tab)) {
            allRows = buildStartedRows(currentUser, ProcessConstant.INSTANCE_CLOSED);
        } else {
            allRows = buildStartedRows(currentUser, null);
        }

        List<ApprovalTaskRes> filtered = allRows.stream()
                .filter(row -> query.getBusType() == null || query.getBusType().isBlank() || query.getBusType().equalsIgnoreCase(row.getBusType()))
                .filter(row -> query.getStatus() == null || query.getStatus().isBlank() || query.getStatus().equalsIgnoreCase(row.getStatus()))
                .filter(row -> {
                    if (query.getKeyword() == null || query.getKeyword().isBlank()) {
                        return true;
                    }
                    String keyword = query.getKeyword().trim();
                    return (row.getTitle() != null && row.getTitle().contains(keyword))
                            || (row.getBusId() != null && row.getBusId().contains(keyword));
                })
                .toList();

        int from = Math.max((req.getPageNum() - 1) * req.getPageSize(), 0);
        int to = Math.min(from + req.getPageSize(), filtered.size());
        List<ApprovalTaskRes> pageRows = from >= filtered.size() ? List.of() : filtered.subList(from, to);
        return new SimplePageRes<>((long) filtered.size(), (long) req.getPageNum(), (long) req.getPageSize(), pageRows);
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

        ApprovalDetailRes res = new ApprovalDetailRes();
        res.setTaskId(task.getTaskId());
        res.setInstanceId(instance.getInstanceId());
        res.setBusId(instance.getBusId());
        WfProcessDefinition definition = processDefinitionMapper.selectById(instance.getProcDefId());
        res.setBusType(definition == null ? null : definition.getBusType());
        res.setStatus(instance.getStatus());
        res.setCurrentNode(node == null ? "-" : node.getNodeName());
        res.setStarterId(instance.getStarterId());
        res.setBusinessData("业务单据详情由业务服务提供，当前为流程服务占位信息");
        res.setApprovalHistory(history(taskId));
        res.setAttachments(listAttachments(task.getTaskId()));
        return res;
    }

    @Override
    public List<ApprovalHistoryRes> history(Long taskId) {
        WfTask task = taskMapper.selectById(taskId);
        if (task == null) {
            return List.of();
        }
        List<WfTask> list = taskMapper.selectList(
                new LambdaQueryWrapper<WfTask>()
                        .eq(WfTask::getInstanceId, task.getInstanceId())
                        .orderByAsc(WfTask::getCreateTime)
        );
        return list.stream().map(item -> {
            ApprovalHistoryRes res = new ApprovalHistoryRes();
            BeanUtils.copyProperties(item, res);
            WfNodeDefinition node = nodeDefinitionMapper.selectById(item.getNodeId());
            res.setNodeName(node == null ? "-" : node.getNodeName());
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
            throw new RuntimeException("驳回意见不能为空");
        }
        doHandle(req.getTaskId(), ProcessConstant.ACTION_REJECT, req.getComments(), req.getAttachments());
    }

    @Override
    @Transactional
    public void batchApprove(ApprovalActionReq req) {
        if (req.getTaskIds() == null || req.getTaskIds().isEmpty()) {
            return;
        }
        for (Long taskId : req.getTaskIds()) {
            doHandle(taskId, ProcessConstant.ACTION_AGREE, req.getComments(), req.getAttachments());
        }
    }

    @Override
    @Transactional
    public void batchReject(ApprovalActionReq req) {
        if (req.getTaskIds() == null || req.getTaskIds().isEmpty()) {
            return;
        }
        if (req.getComments() == null || req.getComments().isBlank()) {
            throw new RuntimeException("批量驳回意见不能为空");
        }
        for (Long taskId : req.getTaskIds()) {
            doHandle(taskId, ProcessConstant.ACTION_REJECT, req.getComments(), req.getAttachments());
        }
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
        if (!ProcessConstant.INSTANCE_RUNNING.equals(instance.getStatus())) {
            throw new RuntimeException("当前流程状态不允许关闭");
        }

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
        WfTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        if (task.getIsDone() != null && task.getIsDone() == 1) {
            throw new RuntimeException("任务已处理");
        }

        WfInstance instance = instanceMapper.selectById(task.getInstanceId());
        if (instance == null) {
            throw new RuntimeException("流程实例不存在");
        }
        if (!ProcessConstant.INSTANCE_RUNNING.equals(instance.getStatus())) {
            throw new RuntimeException("流程已结束");
        }

        if (!matchCandidate(task, currentUser, person)) {
            throw new RuntimeException("当前用户无审批权限");
        }

        LocalDateTime now = LocalDateTime.now();
        task.setIsDone(1);
        task.setAction(action);
        task.setOpinion(comments);
        task.setHandlerId(currentUser.getUserId());
        task.setFinishTime(now);
        taskMapper.updateById(task);
        saveAttachments(task.getTaskId(), attachments, currentUser.getUserId());

        WfNodeDefinition currentNode = nodeDefinitionMapper.selectById(task.getNodeId());
        if (currentNode == null) {
            throw new RuntimeException("节点配置不存在");
        }
        if (ProcessConstant.ACTION_AGREE.equals(action)) {
            WfNodeDefinition next = nextNode(currentNode.getProcDefId(), currentNode.getSortNo());
            if (next == null) {
                instance.setStatus(ProcessConstant.INSTANCE_COMPLETED);
                instance.setEndTime(now);
                instanceMapper.updateById(instance);
            } else {
                createPendingTask(instance.getInstanceId(), next);
            }
            return;
        }

        WfNodeDefinition prev = prevNode(currentNode.getProcDefId(), currentNode.getSortNo());
        if (prev == null) {
            throw new RuntimeException("当前节点不存在上一步，无法驳回");
        }
        createPendingTask(instance.getInstanceId(), prev);
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

    private List<ApprovalTaskRes> buildTodoRows(CurrentUser currentUser, PersonDetailRes person) {
        List<WfTask> tasks = taskMapper.selectList(
                new LambdaQueryWrapper<WfTask>()
                        .eq(WfTask::getIsDone, 0)
                        .orderByDesc(WfTask::getCreateTime)
        );
        return tasks.stream()
                .filter(task -> matchCandidate(task, currentUser, person))
                .map(this::toTaskRes)
                .toList();
    }

    private List<ApprovalTaskRes> buildDoneRows(CurrentUser currentUser) {
        return taskMapper.selectList(
                new LambdaQueryWrapper<WfTask>()
                        .eq(WfTask::getIsDone, 1)
                        .eq(WfTask::getHandlerId, currentUser.getUserId())
                        .orderByDesc(WfTask::getFinishTime)
        ).stream().map(this::toTaskRes).toList();
    }

    private List<ApprovalTaskRes> buildStartedRows(CurrentUser currentUser, String status) {
        LambdaQueryWrapper<WfInstance> instanceWrapper = new LambdaQueryWrapper<WfInstance>()
                .eq(WfInstance::getStarterId, currentUser.getUserId())
                .orderByDesc(WfInstance::getStartTime);
        if (status != null) {
            instanceWrapper.eq(WfInstance::getStatus, status);
        }
        List<WfInstance> instances = instanceMapper.selectList(instanceWrapper);
        List<ApprovalTaskRes> result = new ArrayList<>();
        for (WfInstance instance : instances) {
            WfTask latestTask = taskMapper.selectOne(
                    new LambdaQueryWrapper<WfTask>()
                            .eq(WfTask::getInstanceId, instance.getInstanceId())
                            .orderByDesc(WfTask::getCreateTime)
                            .last("limit 1")
            );
            if (latestTask != null) {
                result.add(toTaskRes(latestTask));
            } else {
                ApprovalTaskRes row = new ApprovalTaskRes();
                row.setTaskId(null);
                row.setInstanceId(instance.getInstanceId());
                WfProcessDefinition definition = processDefinitionMapper.selectById(instance.getProcDefId());
                row.setBusType(definition == null ? "-" : definition.getBusType());
                row.setTitle(definition == null ? "流程申请" : definition.getProcName());
                row.setBusId(instance.getBusId());
                row.setStarterId(instance.getStarterId());
                row.setStartTime(instance.getStartTime());
                row.setCurrentNode("-");
                row.setStatus(instance.getStatus());
                row.setIsDone(1);
                result.add(row);
            }
        }
        return result;
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
            WfProcessDefinition definition = processDefinitionMapper.selectById(instance.getProcDefId());
            if (definition != null) {
                row.setBusType(definition.getBusType());
                row.setTitle(definition.getProcName());
            }
        }
        WfNodeDefinition node = nodeDefinitionMapper.selectById(task.getNodeId());
        row.setCurrentNode(node == null ? "-" : node.getNodeName());
        return row;
    }

    private Long countTodo(CurrentUser currentUser, PersonDetailRes person) {
        List<WfTask> tasks = taskMapper.selectList(
                new LambdaQueryWrapper<WfTask>().eq(WfTask::getIsDone, 0)
        );
        return tasks.stream().filter(task -> matchCandidate(task, currentUser, person)).count();
    }

    private boolean matchCandidate(WfTask task, CurrentUser currentUser, PersonDetailRes person) {
        if (task.getCandidateType() == null || task.getCandidateId() == null) {
            return false;
        }
        String candidateId = task.getCandidateId();
        if (ProcessConstant.ASSIGNEE_USER.equalsIgnoreCase(task.getCandidateType())) {
            return candidateId.equals(String.valueOf(currentUser.getUserId()));
        }
        if (ProcessConstant.ASSIGNEE_ROLE.equalsIgnoreCase(task.getCandidateType())) {
            Set<String> roles = currentUser.getRoles().stream().filter(Objects::nonNull).collect(Collectors.toSet());
            return roles.contains(candidateId);
        }
        if (ProcessConstant.ASSIGNEE_POST.equalsIgnoreCase(task.getCandidateType())) {
            if (person == null) {
                return false;
            }
            return candidateId.equals(person.getPostCode()) || candidateId.equals(String.valueOf(person.getPostId()));
        }
        return false;
    }

    private WfProcessDefinition resolveDefinition(StartProcessReq req) {
        WfProcessDefinition definition = null;
        if (req.getProcDefId() != null) {
            definition = processDefinitionMapper.selectById(req.getProcDefId());
        }
        if (definition == null && req.getProcCode() != null && !req.getProcCode().isBlank()) {
            definition = processDefinitionMapper.selectOne(
                    new LambdaQueryWrapper<WfProcessDefinition>()
                            .eq(WfProcessDefinition::getProcCode, req.getProcCode())
                            .eq(WfProcessDefinition::getIsActive, 1)
                            .orderByDesc(WfProcessDefinition::getVersion)
                            .last("limit 1")
            );
        }
        if (definition == null) {
            throw new RuntimeException("流程定义不存在");
        }
        if (!Objects.equals(definition.getIsActive(), 1)) {
            throw new RuntimeException("流程定义未启用");
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

    private WfNodeDefinition prevNode(Long procDefId, Integer currentSortNo) {
        return nodeDefinitionMapper.selectOne(
                new LambdaQueryWrapper<WfNodeDefinition>()
                        .eq(WfNodeDefinition::getProcDefId, procDefId)
                        .lt(WfNodeDefinition::getSortNo, currentSortNo)
                        .orderByDesc(WfNodeDefinition::getSortNo)
                        .last("limit 1")
        );
    }

    private void createPendingTask(Long instanceId, WfNodeDefinition node) {
        WfTask task = new WfTask();
        task.setInstanceId(instanceId);
        task.setNodeId(node.getNodeId());
        task.setCandidateType(node.getAssigneeType());
        task.setCandidateId(node.getAssigneeId());
        task.setIsDone(0);
        task.setAction(ProcessConstant.ACTION_SUBMIT);
        task.setCreateTime(LocalDateTime.now());
        taskMapper.insert(task);
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
            return personService.getPersonById(userId);
        } catch (Exception ex) {
            return null;
        }
    }
}
