package jh.exp.process.service.driver;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jh.exp.common.core.auth.CurrentUserHolder;
import jh.exp.common.core.auth.dto.CurrentUser;
import jh.exp.process.core.entity.WfInstance;
import jh.exp.process.core.entity.WfProcessDefinition;
import jh.exp.process.core.entity.WfTask;
import jh.exp.process.core.entity.req.ApprovalActionReq;
import jh.exp.process.core.entity.req.ForceCloseReq;
import jh.exp.process.core.entity.req.ProcessDriveReq;
import jh.exp.process.core.entity.req.StartProcessReq;
import jh.exp.process.core.entity.res.ProcessDriveRes;
import jh.exp.process.core.mapper.WfInstanceMapper;
import jh.exp.process.core.mapper.WfProcessDefinitionMapper;
import jh.exp.process.core.mapper.WfTaskMapper;
import jh.exp.process.service.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class ProcessCommandDriver {

    private final ApprovalService approvalService;
    private final WfTaskMapper taskMapper;
    private final WfInstanceMapper instanceMapper;
    private final WfProcessDefinitionMapper processDefinitionMapper;
    private final ProcessBusinessDriverRegistry driverRegistry;

    @Transactional
    public ProcessDriveRes execute(ProcessDriveReq req) {
        ProcessDriveContext ctx = buildContext(req);
        ProcessBusinessDriver handler = driverRegistry.route(ctx.getAction(), ctx.getBusType(), ctx.getProcCode());

        handler.beforeHandle(ctx);
        runEngineAction(ctx);
        refreshStatus(ctx);
        handler.afterHandle(ctx);

        ProcessDriveRes res = new ProcessDriveRes();
        res.setAction(ctx.getAction());
        res.setInstanceId(ctx.getInstanceId());
        res.setTaskId(ctx.getTaskId());
        res.setBusType(ctx.getBusType());
        res.setProcCode(ctx.getProcCode());
        res.setStatus(ctx.getInstanceStatus());
        res.setMessage("操作成功");
        return res;
    }

    private ProcessDriveContext buildContext(ProcessDriveReq req) {
        if (req == null || !StringUtils.hasText(req.getAction())) {
            throw new RuntimeException("action 不能为空");
        }
        ProcessDriveContext ctx = new ProcessDriveContext();
        ctx.setReq(req);
        ctx.setAction(req.getAction().trim().toUpperCase());
        CurrentUser currentUser = CurrentUserHolder.get();
        if (currentUser == null || currentUser.getUserId() == null) {
            throw new RuntimeException("未登录或登录状态失效");
        }
        ctx.setOperatorId(currentUser.getUserId());

        if ("CREATE".equals(ctx.getAction())) {
            if (!StringUtils.hasText(req.getBusId())) {
                throw new RuntimeException("busId 不能为空");
            }
            WfProcessDefinition definition = resolveDefinition(req);
            ctx.setProcCode(definition.getProcCode());
            ctx.setBusType(definition.getBusType());
            ctx.setBusId(req.getBusId());
            return ctx;
        }

        if ("APPROVE".equals(ctx.getAction()) || "REJECT".equals(ctx.getAction())) {
            if (req.getTaskId() == null) {
                throw new RuntimeException("taskId 不能为空");
            }
            WfTask task = taskMapper.selectById(req.getTaskId());
            if (task == null) {
                throw new RuntimeException("任务不存在");
            }
            WfInstance instance = instanceMapper.selectById(task.getInstanceId());
            if (instance == null) {
                throw new RuntimeException("流程实例不存在");
            }
            WfProcessDefinition definition = processDefinitionMapper.selectById(instance.getProcDefId());
            if (definition == null) {
                throw new RuntimeException("流程定义不存在");
            }
            ctx.setTaskId(task.getTaskId());
            ctx.setInstanceId(instance.getInstanceId());
            ctx.setBusId(instance.getBusId());
            ctx.setBusType(definition.getBusType());
            ctx.setProcCode(definition.getProcCode());
            ctx.setInstanceStatus(instance.getStatus());
            return ctx;
        }

        if ("FORCE_CLOSE".equals(ctx.getAction())) {
            if (req.getInstanceId() == null) {
                throw new RuntimeException("instanceId 不能为空");
            }
            WfInstance instance = instanceMapper.selectById(req.getInstanceId());
            if (instance == null) {
                throw new RuntimeException("流程实例不存在");
            }
            WfProcessDefinition definition = processDefinitionMapper.selectById(instance.getProcDefId());
            if (definition == null) {
                throw new RuntimeException("流程定义不存在");
            }
            ctx.setInstanceId(instance.getInstanceId());
            ctx.setBusId(instance.getBusId());
            ctx.setBusType(definition.getBusType());
            ctx.setProcCode(definition.getProcCode());
            ctx.setInstanceStatus(instance.getStatus());
            return ctx;
        }

        throw new RuntimeException("不支持的 action: " + ctx.getAction());
    }

    private void runEngineAction(ProcessDriveContext ctx) {
        ProcessDriveReq req = ctx.getReq();
        if ("CREATE".equals(ctx.getAction())) {
            StartProcessReq startReq = new StartProcessReq();
            startReq.setProcDefId(req.getProcDefId());
            startReq.setProcCode(ctx.getProcCode());
            startReq.setBusId(req.getBusId());
            Long instanceId = approvalService.start(startReq);
            ctx.setInstanceId(instanceId);
            return;
        }
        if ("APPROVE".equals(ctx.getAction())) {
            ApprovalActionReq actionReq = new ApprovalActionReq();
            actionReq.setTaskId(req.getTaskId());
            actionReq.setComments(req.getComments());
            actionReq.setAttachments(req.getAttachments());
            approvalService.approve(actionReq);
            return;
        }
        if ("REJECT".equals(ctx.getAction())) {
            ApprovalActionReq actionReq = new ApprovalActionReq();
            actionReq.setTaskId(req.getTaskId());
            actionReq.setComments(req.getComments());
            actionReq.setAttachments(req.getAttachments());
            approvalService.reject(actionReq);
            return;
        }
        if ("FORCE_CLOSE".equals(ctx.getAction())) {
            ForceCloseReq closeReq = new ForceCloseReq();
            closeReq.setInstanceId(req.getInstanceId());
            closeReq.setReason(req.getReason());
            approvalService.forceClose(closeReq);
        }
    }

    private void refreshStatus(ProcessDriveContext ctx) {
        if (ctx.getInstanceId() == null) {
            return;
        }
        WfInstance latest = instanceMapper.selectById(ctx.getInstanceId());
        if (latest != null) {
            ctx.setInstanceStatus(latest.getStatus());
            if (!StringUtils.hasText(ctx.getBusId())) {
                ctx.setBusId(latest.getBusId());
            }
        }
        if (ctx.getTaskId() == null && ("CREATE".equals(ctx.getAction()) || "REJECT".equals(ctx.getAction()))) {
            WfTask latestTask = taskMapper.selectOne(
                    new LambdaQueryWrapper<WfTask>()
                            .eq(WfTask::getInstanceId, ctx.getInstanceId())
                            .eq(WfTask::getIsDone, 0)
                            .orderByDesc(WfTask::getCreateTime)
                            .last("limit 1")
            );
            if (latestTask != null) {
                ctx.setTaskId(latestTask.getTaskId());
            }
        }
    }

    private WfProcessDefinition resolveDefinition(ProcessDriveReq req) {
        WfProcessDefinition definition = null;
        if (req.getProcDefId() != null) {
            definition = processDefinitionMapper.selectById(req.getProcDefId());
        }
        if (definition == null && StringUtils.hasText(req.getProcCode())) {
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
        if (!StringUtils.hasText(req.getBusType())) {
            return definition;
        }
        if (!req.getBusType().equalsIgnoreCase(definition.getBusType())) {
            throw new RuntimeException("入参 busType 与流程定义不一致");
        }
        return definition;
    }
}
