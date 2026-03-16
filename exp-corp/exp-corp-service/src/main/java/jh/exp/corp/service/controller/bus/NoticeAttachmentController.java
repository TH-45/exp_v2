package jh.exp.corp.service.controller.bus;

import jakarta.validation.Valid;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.corp.core.entity.req.DeleteNoticeAttachmentReq;
import jh.exp.corp.core.entity.req.QueryNoticeAttachmentReq;
import jh.exp.corp.core.entity.res.NoticeAttachmentRes;
import jh.exp.corp.service.service.bus.NoticeAttachmentInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/notice-attachment")
@RequiredArgsConstructor
public class NoticeAttachmentController {

    private final NoticeAttachmentInternalService noticeAttachmentInternalService;

    @PostMapping("/list")
    public ApiResponse<List<NoticeAttachmentRes>> list(@RequestBody QueryNoticeAttachmentReq req) {
        return ApiResponse.success(noticeAttachmentInternalService.list(req));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<NoticeAttachmentRes> upload(@RequestPart("file") MultipartFile file,
                                                   @RequestParam("noticeId") Long noticeId) {
        return ApiResponse.success(noticeAttachmentInternalService.upload(noticeId, file));
    }

    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody @Valid DeleteNoticeAttachmentReq req) {
        noticeAttachmentInternalService.delete(req);
        return ApiResponse.success(null);
    }

    @GetMapping("/downloadStream")
    public ResponseEntity<Resource> downloadStream(@RequestParam("attachmentId") Long attachmentId,
                                                   @RequestParam(value = "fileName", required = false) String fileName) {
        byte[] bytes = noticeAttachmentInternalService.download(attachmentId);
        String safeName = fileName == null || fileName.isBlank() ? ("notice_attachment_" + attachmentId) : fileName;
        ByteArrayResource resource = new ByteArrayResource(bytes);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(safeName, StandardCharsets.UTF_8).build().toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(bytes.length)
                .body(resource);
    }
}
