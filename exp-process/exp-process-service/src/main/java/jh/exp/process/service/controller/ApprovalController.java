package jh.exp.process.service.controller;

import jakarta.validation.Valid;
import jh.exp.common.core.annotation.RequiresMenuLevel;
import jh.exp.common.core.annotation.RequiresPermissions;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.auth.CurrentUserHolder;
import jh.exp.common.core.exception.AuthException;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.process.core.constant.ProcessConstant;
import jh.exp.process.core.entity.req.ApprovalActionReq;
import jh.exp.process.core.entity.req.ApprovalTaskQueryReq;
import jh.exp.process.core.entity.req.ForceCloseReq;
import jh.exp.process.core.entity.req.ProcessDriveReq;
import jh.exp.process.core.entity.req.StartProcessReq;
import jh.exp.process.core.entity.res.ApprovalDetailRes;
import jh.exp.process.core.entity.res.ApprovalHistoryRes;
import jh.exp.process.core.entity.res.ApprovalStatsRes;
import jh.exp.process.core.entity.res.ApprovalTaskRes;
import jh.exp.process.service.driver.ProcessCommandDriver;
import jh.exp.process.service.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/approval")
@RequiredArgsConstructor
@RequiresMenuLevel(code = "approval:center", level = 1)
public class ApprovalController {

    private final ApprovalService approvalService;
    private final ProcessCommandDriver processCommandDriver;

    /**
     * 创建（流程发起）
     */
    @PostMapping("/create")
    @RequiresMenuLevel(code = "process:start", level = 2)
    public ApiResponse<Long> create(@RequestBody @Valid StartProcessReq req) {
        Long instanceId = approvalService.create(req);
        return ApiResponse.success(instanceId);
    }

    /**
     * 统计：待办/已办/我发起/已关闭 数量
     */
    @GetMapping("/stats")
    public ApiResponse<ApprovalStatsRes> stats() {
        return ApiResponse.success(approvalService.getStats());
    }

    /**
     * 查询任务
     * @param req
     * @return
     */
    @PostMapping("/tasks")
    public ApiResponse<SimplePageRes<ApprovalTaskRes>> tasks(@RequestBody SimplePageReq<ApprovalTaskQueryReq> req) {
        req.pageDefault();
        return ApiResponse.success(approvalService.listTasks(req));
    }

    /**
     * 详情
     * @param taskId
     * @return
     */
    @GetMapping("/detail")
    public ApiResponse<ApprovalDetailRes> detail(@RequestParam Long taskId) {
        return ApiResponse.success(approvalService.detail(taskId));
    }
    
    /**
     * 按流程实例查询详情
     */
    @GetMapping("/instance-detail")
    public ApiResponse<ApprovalDetailRes> instanceDetail(@RequestParam Long instanceId) {
        return ApiResponse.success(approvalService.detailByInstance(instanceId));
    }

    /**
     * 审批（通过/驳回）。按 action 校验对应权限：通过类动作需 approval:task:approve，驳回类动作需 approval:task:reject。
     */
    @PostMapping("/approve")
    @RequiresMenuLevel(code = "approval:center", level = 1)
    public ApiResponse<Void> approve(@RequestBody ApprovalActionReq req) {
        String action = req.getAction();
        String requiredPerm;
        if (ProcessConstant.ACTION_APPROVE.equals(action) || ProcessConstant.ACTION_AGREE.equals(action)) {
            requiredPerm = "approval:task:approve";
        } else if (ProcessConstant.ACTION_REJECT.equals(action) || ProcessConstant.ACTION_RETURN.equals(action)) {
            requiredPerm = "approval:task:reject";
        } else {
            throw new AuthException("AUTH_FORBIDDEN", "未支持的审批动作: " + action);
        }
        var user = CurrentUserHolder.get();
        if (user == null) {
            throw new AuthException("AUTH_FORBIDDEN", "未登录或登录已失效");
        }
        Set<String> userPerms = user.getFuncPermissionSet();
        if (userPerms == null || userPerms.isEmpty()) {
            var legacy = user.getPermissions();
            userPerms = legacy == null ? Collections.emptySet() : new HashSet<>(legacy);
        }
        if (!userPerms.contains(requiredPerm)) {
            throw new AuthException("AUTH_FORBIDDEN", "权限不足，需要权限: " + requiredPerm);
        }
        ProcessDriveReq driveReq = new ProcessDriveReq();
        driveReq.setAction(req.getAction());
        driveReq.setTaskId(req.getTaskId());
        driveReq.setComments(req.getComments());
        driveReq.setAttachments(req.getAttachments());
        processCommandDriver.execute(driveReq);
        return ApiResponse.success(null);
    }



    @GetMapping("/history")
    public ApiResponse<List<ApprovalHistoryRes>> history(@RequestParam Long taskId) {
        return ApiResponse.success(approvalService.history(taskId));
    }


//    @PostMapping("/batch-approve")
//    public ApiResponse<Void> batchApprove(@RequestBody ApprovalActionReq req) {
//        approvalService.batchApprove(req);
//        return ApiResponse.success(null);
//    }
//
//    @PostMapping("/batch-reject")
//    public ApiResponse<Void> batchReject(@RequestBody ApprovalActionReq req) {
//        approvalService.batchReject(req);
//        return ApiResponse.success(null);
//    }

    /**
     * 强制关闭流程。需具备特殊权限 approval:task:force_close。
     */
    @PostMapping("/force-close")
    @RequiresPermissions("approval:task:force_close")
    public ApiResponse<Void> forceClose(@RequestBody @Valid ForceCloseReq req) {
        ProcessDriveReq driveReq = new ProcessDriveReq();
        driveReq.setAction(req.getAction() != null ? req.getAction() : ProcessConstant.ACTION_CLOSE);
        driveReq.setInstanceId(req.getInstanceId());
        driveReq.setReason(req.getReason());
        processCommandDriver.execute(driveReq);
        return ApiResponse.success(null);
    }
}
