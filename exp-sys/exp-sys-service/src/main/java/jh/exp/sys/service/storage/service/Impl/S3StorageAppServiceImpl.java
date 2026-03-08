package jh.exp.sys.service.storage.service.Impl;

import jh.exp.common.core.exception.BizException;
import jh.exp.sys.core.req.storage.StorageBatchDownloadReq;
import jh.exp.sys.core.req.storage.StorageUploadBizReq;
import jh.exp.sys.core.res.storage.StorageUploadRes;
import jh.exp.sys.service.storage.config.StorageProperties;
import jh.exp.sys.service.storage.service.StorageAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class S3StorageAppServiceImpl implements StorageAppService {
    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    private final S3Client s3Client;
    private final StorageProperties properties;

    @Override
    public StorageUploadRes upload(MultipartFile file, StorageUploadBizReq biz) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }
        try {
            byte[] bytes = file.getBytes();
            String objectKey = buildObjectKey(file.getOriginalFilename(), biz);
            String contentType = StringUtils.hasText(file.getContentType()) ? file.getContentType() : DEFAULT_CONTENT_TYPE;

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(properties.getBucket())
                    .key(objectKey)
                    .contentType(contentType)
                    .build();
            s3Client.putObject(request, RequestBody.fromBytes(bytes));

            StorageUploadRes res = new StorageUploadRes();
            res.setObjectKey(objectKey);
            res.setFileName(file.getOriginalFilename());
            res.setFileSize((long) bytes.length);
            res.setFileMd5(DigestUtils.md5DigestAsHex(bytes));
            res.setContentType(contentType);
            res.setUrl(buildPathStyleUrl(objectKey));
            return res;
        } catch (IOException e) {
            throw new RuntimeException("读取上传文件失败", e);
        }
    }

    @Override
    public boolean exists(String objectKey) {
        HeadObjectRequest request = HeadObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(objectKey)
                .build();
        try {
            s3Client.headObject(request);
            return true;
        } catch (S3Exception ex) {
            if (ex.statusCode() == 404) {
                return false;
            }
            throw new BizException("检查文件是否存在失败", ex);
        }
    }

    @Override
    public byte[] download(String objectKey) {
        if (!exists(objectKey)) {
            throw new BizException("文件不存在或已被清理，无法下载");
        }
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(objectKey)
                .build();
        try {
            return s3Client.getObjectAsBytes(request).asByteArray();
        } catch (S3Exception ex) {
            if (ex.statusCode() == 404) {
                throw new BizException("文件不存在或已被清理，无法下载");
            }
            throw new BizException("下载文件失败", ex);
        }
    }

    @Override
    public byte[] batchDownload(StorageBatchDownloadReq req) {
        List<String> objectKeys = req.getObjectKeys();
        if (objectKeys == null || objectKeys.isEmpty()) {
            throw new BizException("objectKeys 不能为空");
        }

        // 严格模式：只要有一个不存在即整体失败
        for (String objectKey : objectKeys) {
            if (!StringUtils.hasText(objectKey)) {
                throw new BizException("objectKey 不能为空");
            }
            if (!exists(objectKey)) {
                throw new BizException("批量下载失败，存在文件不存在或已被清理: " + objectKey);
            }
        }

        Map<String, Integer> fileNameCount = new HashMap<>();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (String objectKey : objectKeys) {
                byte[] content = download(objectKey);
                String rawName = extractName(objectKey);
                String entryName = buildUniqueEntryName(rawName, fileNameCount);
                zos.putNextEntry(new ZipEntry(entryName));
                zos.write(content);
                zos.closeEntry();
            }
            zos.finish();
            return baos.toByteArray();
        } catch (IOException ex) {
            throw new BizException("批量下载打包失败", ex);
        }
    }

    @Override
    public void delete(String objectKey) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(objectKey)
                .build();
        s3Client.deleteObject(request);
    }

    private String buildObjectKey(String originalName, StorageUploadBizReq biz) {
        String safeName = sanitizeFileName(originalName);
        String monthFolder = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        return "attachment/" + biz.getBusinessType() + "/" + biz.getBusinessId() + "/" + monthFolder + "/"
                + UUID.randomUUID() + "_" + safeName;
    }

    private String sanitizeFileName(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "unknown.bin";
        }
        return fileName.replace("\\", "_").replace("/", "_");
    }

    private String extractName(String objectKey) {
        int idx = objectKey.lastIndexOf('/');
        String name = idx >= 0 ? objectKey.substring(idx + 1) : objectKey;
        if (!StringUtils.hasText(name)) {
            return "unknown.bin";
        }
        return sanitizeFileName(name);
    }

    private String buildUniqueEntryName(String fileName, Map<String, Integer> fileNameCount) {
        int count = fileNameCount.getOrDefault(fileName, 0);
        fileNameCount.put(fileName, count + 1);
        if (count == 0) {
            return fileName;
        }
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex <= 0) {
            return fileName + "(" + count + ")";
        }
        String prefix = fileName.substring(0, dotIndex);
        String suffix = fileName.substring(dotIndex);
        return prefix + "(" + count + ")" + suffix;
    }

    private String buildPathStyleUrl(String objectKey) {
        if (!StringUtils.hasText(properties.getEndpoint())) {
            return objectKey;
        }
        String endpoint = properties.getEndpoint().endsWith("/") ? properties.getEndpoint().substring(0, properties.getEndpoint().length() - 1) : properties.getEndpoint();
        return endpoint + "/" + properties.getBucket() + "/" + objectKey;
    }
}
