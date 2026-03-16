package jh.exp.corp.service.controller.bus;

import jakarta.validation.Valid;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jh.exp.corp.core.entity.req.*;
import jh.exp.corp.core.entity.res.QualificationAttachmentDetailRes;
import jh.exp.corp.core.entity.res.QualificationAttachmentListRes;
import jh.exp.corp.service.service.bus.QualificationAttachmentInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("qualification-attachment")
@RequiredArgsConstructor
public class QualificationAttachmentController {

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

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<QualificationAttachmentDetailRes> upload(@RequestPart("file") MultipartFile file,
                                                                @RequestParam("qualificationId") Long qualificationId) {
        return ApiResponse.success(qualificationAttachmentInternalService.upload(qualificationId, file));
    }

    @GetMapping("/downloadStream")
    public ResponseEntity<Resource> downloadStream(@RequestParam("attachmentId") Long attachmentId,
                                                   @RequestParam(value = "fileName", required = false) String fileName) {
        byte[] bytes = qualificationAttachmentInternalService.download(attachmentId);
        String safeName = fileName == null || fileName.isBlank() ? ("qualification_attachment_" + attachmentId) : fileName;
        ByteArrayResource resource = new ByteArrayResource(bytes);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(safeName, StandardCharsets.UTF_8).build().toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(bytes.length)
                .body(resource);
    }
}
