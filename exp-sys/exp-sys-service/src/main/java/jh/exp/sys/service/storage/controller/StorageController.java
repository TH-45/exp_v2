package jh.exp.sys.service.storage.controller;

import jh.exp.common.core.api.ApiResponse;
import jh.exp.sys.core.req.storage.StorageBatchDownloadReq;
import jh.exp.sys.core.req.storage.StorageDeleteReq;
import jh.exp.sys.core.req.storage.StorageUploadBizReq;
import jh.exp.sys.core.res.storage.StorageUploadRes;
import jh.exp.sys.service.storage.service.StorageAppService;
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
/**
 * 存储控制器
 * 提供文件上传、下载和删除的 RESTful API 接口
 */
@RestController
@RequestMapping("/storage")
@RequiredArgsConstructor
public class StorageController {
    private final StorageAppService storageAppService;

    /**
     * 上传文件
     *
     * @param file 上传的文件
     * @param biz 业务请求参数
     * @return 上传结果响应
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<StorageUploadRes> upload(@RequestPart("file") MultipartFile file,
                                                @RequestPart("biz") @Valid StorageUploadBizReq biz) {
        return ApiResponse.success(storageAppService.upload(file, biz));
    }

    /**
     * 下载文件
     *
     * @param objectKey 对象键（文件路径）
     * @return 文件资源响应，包含文件内容和下载头信息
     */
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

    /**
     * 检查文件是否存在
     *
     * @param objectKey 对象键（文件路径）
     * @return 是否存在
     */
    @GetMapping("/exist")
    public ApiResponse<Boolean> exist(@RequestParam("objectKey") String objectKey) {
        return ApiResponse.success(storageAppService.exists(objectKey));
    }

    /**
     * 批量下载文件（严格模式：任一不存在则整体失败）
     *
     * @param req 批量下载请求参数
     * @return zip 文件资源响应
     */
    @PostMapping("/batchDownload")
    public ResponseEntity<Resource> batchDownload(@RequestBody @Valid StorageBatchDownloadReq req) {
        byte[] bytes = storageAppService.batchDownload(req);
        String archiveName = normalizeArchiveName(req.getArchiveName());
        ByteArrayResource resource = new ByteArrayResource(bytes);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(archiveName).build().toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(bytes.length)
                .body(resource);
    }

    /**
     * 删除文件
     *
     * @param req 删除请求参数，包含对象键
     * @return 操作结果响应
     */
    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody @Valid StorageDeleteReq req) {
        storageAppService.delete(req.getObjectKey());
        return ApiResponse.success(null);
    }

    /**
     * 从对象键中提取文件名
     *
     * @param objectKey 对象键（完整路径）
     * @return 提取出的文件名
     */
    private String extractName(String objectKey) {
        int idx = objectKey.lastIndexOf('/');
        return idx >= 0 ? objectKey.substring(idx + 1) : objectKey;
    }

    private String normalizeArchiveName(String archiveName) {
        if (archiveName == null || archiveName.isBlank()) {
            return "attachments.zip";
        }
        String name = archiveName.endsWith(".zip") ? archiveName : archiveName + ".zip";
        return name.replace("\\", "_").replace("/", "_");
    }
}
