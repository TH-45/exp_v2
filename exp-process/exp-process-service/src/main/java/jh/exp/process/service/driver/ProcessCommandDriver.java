package jh.exp.process.service.driver;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jh.exp.common.core.auth.CurrentUserHolder;
import jh.exp.common.core.auth.dto.CurrentUser;
import jh.exp.process.core.constant.ProcessConstant;
import jh.exp.process.core.entity.WfInstance;
import jh.exp.process.core.entity.WfProcessDefinition;
import jh.exp.process.core.entity.WfTask;
import jh.exp.process.core.entity.dto.ProcessDriveContext;
import jh.exp.process.core.entity.dto.TaskContextDTO;
import jh.exp.process.core.entity.req.ApprovalActionReq;
import jh.exp.process.core.entity.req.ForceCloseReq;
import jh.exp.process.core.entity.req.ProcessDriveReq;
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
        // 构建上下文 获取实例的相关信息，方便做个性化处理
        ProcessDriveContext ctx = buildContext(req);
        //路由到业务处理器
        ProcessBusinessDriver<?> handler = driverRegistry.route(ctx.getBusType(), ctx.getProcCode());
        if (!handler.supportsAction(ctx.getAction())) {
            throw new RuntimeException("当前流程不支持该操作: " + ctx.getAction());
        }
        //执行前
        handler.beforeHandle(ctx);
        //执行
        runEngineAction(ctx);
        //更新状态
        refreshStatus(ctx);
        //执行后
        handler.afterHandle(ctx);

        //响应
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

    // 构建上下文，获取工单的常规性信息
    private ProcessDriveContext buildContext(ProcessDriveReq req) {
        if (req == null) {
            throw new RuntimeException("请求不能为空");
        }
        //通过 taskId，除开taskId、action、comments、attachments
        if (!StringUtils.hasText(req.getAction())) {
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

        // 校验 action，非法动作提前失败
        boolean supported = ProcessConstant.ACTION_AGREE.equals(ctx.getAction())
                || ProcessConstant.ACTION_REJECT.equals(ctx.getAction())
                || ProcessConstant.ACTION_RETURN.equals(ctx.getAction())
                || ProcessConstant.ACTION_CLOSE.equals(ctx.getAction());
        if (!supported) {
            throw new RuntimeException("不支持的 action: " + ctx.getAction());
        }
        // CLOSE 支持仅通过 instanceId 进行上下文构建
        if (ProcessConstant.ACTION_CLOSE.equals(ctx.getAction())) {
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
            ctx.setInstanceStatus(instance.getStatus());
            ctx.setBusType(definition.getBusType());
            ctx.setProcCode(definition.getProcCode());
            return ctx;
        }
        // 其余动作通过 taskId 构建上下文
        if (req.getTaskId() == null) {
            throw new RuntimeException("taskId 不能为空");
        }
        TaskContextDTO taskCtx = taskMapper.selectTaskContextByTaskId(req.getTaskId());
        if (taskCtx == null) {
            throw new RuntimeException("任务不存在");
        }
        ctx.setTaskId(taskCtx.getTaskId());
        ctx.setInstanceId(taskCtx.getInstanceId());
        ctx.setBusId(taskCtx.getBusId());
        ctx.setInstanceStatus(taskCtx.getInstanceStatus());
        ctx.setBusType(taskCtx.getBusType());
        ctx.setProcCode(taskCtx.getProcCode());
        return ctx;
    }

    private void runEngineAction(ProcessDriveContext ctx) {
        ProcessDriveReq req = ctx.getReq();
        //todo：这的创建处理线先搁置
//        if ("CREATE".equals(ctx.getAction())) {
//            StartProcessReq startReq = new StartProcessReq();
//            startReq.setProcDefId(req.getProcDefId());
//            startReq.setProcCode(ctx.getProcCode());
//            startReq.setBusId(req.getBusId());
//            Long instanceId = approvalService.start(startReq);
//            ctx.setInstanceId(instanceId);
//            return;
//        }
        //同意
        if (ProcessConstant.ACTION_AGREE.equals(ctx.getAction())) {
            ApprovalActionReq actionReq = new ApprovalActionReq();
            actionReq.setTaskId(req.getTaskId());
            actionReq.setComments(req.getComments());
            actionReq.setAttachments(req.getAttachments());
            approvalService.approve(actionReq);
            return;
        }
        //拒绝
        if (ProcessConstant.ACTION_REJECT.equals(ctx.getAction())) {
            ApprovalActionReq actionReq = new ApprovalActionReq();
            actionReq.setTaskId(req.getTaskId());
            actionReq.setComments(req.getComments());
            actionReq.setAttachments(req.getAttachments());
            approvalService.reject(actionReq);
            return;
        }
        //驳回
        if (ProcessConstant.ACTION_RETURN.equals(ctx.getAction())) {
            ApprovalActionReq actionReq = new ApprovalActionReq();
            actionReq.setTaskId(req.getTaskId());
            actionReq.setComments(req.getComments());
            actionReq.setAttachments(req.getAttachments());
            approvalService.returnToPrev(actionReq);
            return;
        }
        //关闭
        if (ProcessConstant.ACTION_CLOSE.equals(ctx.getAction())) {
            ForceCloseReq closeReq = new ForceCloseReq();
            closeReq.setInstanceId(ctx.getInstanceId());
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
        }
        // 当前实例仍有待办时，回传最新待办 taskId；已结束场景则保持原 taskId
        WfTask latestTask = taskMapper.selectOne(new LambdaQueryWrapper<WfTask>()
                .eq(WfTask::getInstanceId, ctx.getInstanceId())
                .eq(WfTask::getIsDone, 0)
                .orderByDesc(WfTask::getCreateTime)
                .last("limit 1"));
        if (latestTask != null) {
            ctx.setTaskId(latestTask.getTaskId());
        }
    }

}
