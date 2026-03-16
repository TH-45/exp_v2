package jh.exp.process.service.controller;

import jakarta.validation.Valid;
import jh.exp.common.core.api.ApiResponse;
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
import jh.exp.process.core.entity.res.ProcessDriveRes;
import jh.exp.process.service.driver.ProcessCommandDriver;
import jh.exp.process.service.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/approval")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;
    private final ProcessCommandDriver processCommandDriver;

    /**
     * 创建
     */
    @PostMapping("/create")
    public ApiResponse<Long> create(@RequestBody @Valid StartProcessReq req) {
        Long instanceId = approvalService.create(req);
        return ApiResponse.success(instanceId);
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
     * 审批
     */
    @PostMapping("/approve")
    public ApiResponse<Void> approve(@RequestBody ApprovalActionReq req) {
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

    @PostMapping("/force-close")
    public ApiResponse<Void> forceClose(@RequestBody @Valid ForceCloseReq req) {
        ProcessDriveReq driveReq = new ProcessDriveReq();
        driveReq.setAction(req.getAction());
        driveReq.setInstanceId(req.getInstanceId());
        driveReq.setReason(req.getReason());
        processCommandDriver.execute(driveReq);
        return ApiResponse.success(null);
    }
}
