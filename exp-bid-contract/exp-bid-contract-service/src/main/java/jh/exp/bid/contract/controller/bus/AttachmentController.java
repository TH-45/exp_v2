package jh.exp.bid.contract.controller.bus;

import jh.exp.bid.contract.entity.req.CreateAttachmentReq;
import jh.exp.bid.contract.entity.req.QueryAttachmentReq;
import jh.exp.bid.contract.entity.res.AttachmentDetailRes;
import jh.exp.bid.contract.entity.res.AttachmentListRes;
import jh.exp.bid.contract.service.bus.AttachmentService;
import jh.exp.common.annotation.RequiresPermissions;
import jh.exp.common.api.ApiResponse;
import jh.exp.common.req.SimplePageReq;
import jh.exp.common.res.SimplePageRes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 附件管理控制器
 */
@RestController
@RequestMapping("/attachment")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    /**
     * 分页查询附件列表
     */
    @PostMapping("/list")
    @RequiresPermissions("ATTACHMENT:VIEW")
    public ApiResponse<SimplePageRes<AttachmentListRes>> list(@RequestBody SimplePageReq<QueryAttachmentReq> req) {
        req.pageDefault();
        SimplePageRes<AttachmentListRes> result = attachmentService.queryAttachmentList(req);
        return ApiResponse.success(result);
    }

    /**
     * 根据ID查询附件详情
     */
    @GetMapping("/detail")
    @RequiresPermissions("ATTACHMENT:VIEW")
    public ApiResponse<AttachmentDetailRes> detail(@RequestParam Long attachmentId) {
        AttachmentDetailRes result = attachmentService.getAttachmentById(attachmentId);
        return ApiResponse.success(result);
    }

    /**
     * 上传附件
     */
    @PostMapping("/upload")
    @RequiresPermissions("ATTACHMENT:UPLOAD")
    public ApiResponse<AttachmentDetailRes> upload(@RequestBody @Valid CreateAttachmentReq req) {
        AttachmentDetailRes result = attachmentService.uploadAttachment(req);
        return ApiResponse.success(result);
    }

    /**
     * 批量上传附件
     */
    @PostMapping("/batchUpload")
    @RequiresPermissions("ATTACHMENT:UPLOAD")
    public ApiResponse<List<AttachmentDetailRes>> batchUpload(@RequestBody List<@Valid CreateAttachmentReq> attachments) {
        List<AttachmentDetailRes> result = attachmentService.batchUploadAttachments(attachments);
        return ApiResponse.success(result);
    }

    /**
     * 更新附件信息
     */
    @PostMapping("/update")
    @RequiresPermissions("ATTACHMENT:EDIT")
    public ApiResponse<AttachmentDetailRes> update(@RequestParam Long attachmentId,
                                                  @RequestBody @Valid CreateAttachmentReq req) {
        AttachmentDetailRes result = attachmentService.updateAttachment(attachmentId, req);
        return ApiResponse.success(result);
    }

    /**
     * 删除附件
     */
    @PostMapping("/delete")
    @RequiresPermissions("ATTACHMENT:DELETE")
    public ApiResponse<Void> delete(@RequestParam Long attachmentId) {
        attachmentService.deleteAttachment(attachmentId);
        return ApiResponse.success(null);
    }

    /**
     * 批量删除附件
     */
    @PostMapping("/batchDelete")
    @RequiresPermissions("ATTACHMENT:DELETE")
    public ApiResponse<Void> batchDelete(@RequestBody List<Long> attachmentIds) {
        attachmentService.batchDeleteAttachments(attachmentIds);
        return ApiResponse.success(null);
    }

    /**
     * 下载附件
     */
    @GetMapping("/download")
    @RequiresPermissions("ATTACHMENT:VIEW")
    public ApiResponse<AttachmentDetailRes> download(@RequestParam Long attachmentId) {
        AttachmentDetailRes result = attachmentService.downloadAttachment(attachmentId);
        return ApiResponse.success(result);
    }

    /**
     * 根据业务查询附件列表
     */
    @GetMapping("/byBusiness")
    @RequiresPermissions("ATTACHMENT:VIEW")
    public ApiResponse<List<AttachmentListRes>> getByBusiness(@RequestParam String businessType,
                                                             @RequestParam Long businessId) {
        List<AttachmentListRes> result = attachmentService.getAttachmentsByBusiness(businessType, businessId);
        return ApiResponse.success(result);
    }

    /**
     * 检查文件是否存在
     */
    @GetMapping("/checkFile")
    public ApiResponse<Boolean> checkFile(@RequestParam String fileName,
                                         @RequestParam String fileMd5,
                                         @RequestParam String businessType,
                                         @RequestParam Long businessId) {
        boolean exists = attachmentService.checkFileExists(fileName, fileMd5, businessType, businessId);
        return ApiResponse.success(exists);
    }

    /**
     * 获取业务附件统计信息
     */
    @GetMapping("/statistics")
    @RequiresPermissions("ATTACHMENT:VIEW")
    public ApiResponse<AttachmentService.AttachmentStatistics> getStatistics(@RequestParam String businessType,
                                                                            @RequestParam Long businessId) {
        AttachmentService.AttachmentStatistics result = attachmentService.getBusinessAttachmentStatistics(businessType, businessId);
        return ApiResponse.success(result);
    }

    /**
     * 更新文件状态
     */
    @PostMapping("/status")
    @RequiresPermissions("ATTACHMENT:EDIT")
    public ApiResponse<AttachmentDetailRes> updateStatus(@RequestParam Long attachmentId,
                                                        @RequestParam String fileStatus) {
        AttachmentDetailRes result = attachmentService.updateFileStatus(attachmentId, fileStatus);
        return ApiResponse.success(result);
    }

    /**
     * 批量更新文件状态
     */
    @PostMapping("/batchStatus")
    @RequiresPermissions("ATTACHMENT:EDIT")
    public ApiResponse<Void> batchUpdateStatus(@RequestBody List<Long> attachmentIds,
                                              @RequestParam String fileStatus) {
        attachmentService.batchUpdateFileStatus(attachmentIds, fileStatus);
        return ApiResponse.success(null);
    }

    /**
     * 清理无效附件
     */
    @PostMapping("/cleanup")
    @RequiresPermissions("ATTACHMENT:ADMIN")
    public ApiResponse<Void> cleanup() {
        attachmentService.cleanupInvalidAttachments();
        return ApiResponse.success(null);
    }
}