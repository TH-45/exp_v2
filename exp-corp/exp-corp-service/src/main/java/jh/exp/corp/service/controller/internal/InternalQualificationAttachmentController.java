package jh.exp.corp.service.controller.internal;

import jakarta.validation.Valid;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.corp.core.entity.req.*;
import jh.exp.corp.core.entity.res.QualificationAttachmentDetailRes;
import jh.exp.corp.core.entity.res.QualificationAttachmentListRes;
import jh.exp.corp.service.service.internal.QualificationAttachmentInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/corp/qualification-attachment")
@RequiredArgsConstructor
public class InternalQualificationAttachmentController {

    private final QualificationAttachmentInternalService qualificationAttachmentInternalService;

    @PostMapping("/list")
    public ApiResponse<SimplePageRes<QualificationAttachmentListRes>> list(
            @RequestBody SimplePageReq<QueryQualificationAttachmentReq> req) {
        req.pageDefault();
        return ApiResponse.success(qualificationAttachmentInternalService.list(req));
    }

    @GetMapping("/detail")
    public ApiResponse<QualificationAttachmentDetailRes> detail(@RequestParam Long attachmentId) {
        return ApiResponse.success(qualificationAttachmentInternalService.detail(attachmentId));
    }

    @PostMapping("/create")
    public ApiResponse<QualificationAttachmentDetailRes> create(@RequestBody @Valid CreateQualificationAttachmentReq req) {
        return ApiResponse.success(qualificationAttachmentInternalService.create(req));
    }

    @PostMapping("/update")
    public ApiResponse<QualificationAttachmentDetailRes> update(@RequestBody @Valid UpdateQualificationAttachmentReq req) {
        return ApiResponse.success(qualificationAttachmentInternalService.update(req));
    }

    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody @Valid DeleteQualificationAttachmentReq req) {
        qualificationAttachmentInternalService.delete(req);
        return ApiResponse.success(null);
    }

    @PostMapping("/batchDelete")
    public ApiResponse<Void> batchDelete(@RequestBody @Valid BatchDeleteQualificationAttachmentReq req) {
        qualificationAttachmentInternalService.batchDelete(req);
        return ApiResponse.success(null);
    }
}
