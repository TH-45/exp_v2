package jh.exp.sys.servcie.storage.controller;

import jh.exp.common.core.api.ApiResponse;
import jh.exp.sys.core.req.storage.StorageDeleteReq;
import jh.exp.sys.core.req.storage.StorageUploadBizReq;
import jh.exp.sys.core.res.storage.StorageUploadRes;
import jh.exp.sys.servcie.storage.service.StorageAppService;
import jakarta.validation.Valid;
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

@RestController
@RequestMapping("/storage")
@RequiredArgsConstructor
public class StorageController {
    private final StorageAppService storageAppService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<StorageUploadRes> upload(@RequestPart("file") MultipartFile file,
                                                @RequestPart("biz") @Valid StorageUploadBizReq biz) {
        return ApiResponse.success(storageAppService.upload(file, biz));
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> download(@RequestParam("objectKey") String objectKey) {
        byte[] bytes = storageAppService.download(objectKey);
        String fileName = extractName(objectKey);
        ByteArrayResource resource = new ByteArrayResource(bytes);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(fileName).build().toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(bytes.length)
                .body(resource);
    }

    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody @Valid StorageDeleteReq req) {
        storageAppService.delete(req.getObjectKey());
        return ApiResponse.success(null);
    }

    private String extractName(String objectKey) {
        int idx = objectKey.lastIndexOf('/');
        return idx >= 0 ? objectKey.substring(idx + 1) : objectKey;
    }
}
