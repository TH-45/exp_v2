package jh.exp.sys.servcie.storage.service.Impl;

import jh.exp.sys.core.req.storage.StorageUploadBizReq;
import jh.exp.sys.core.res.storage.StorageUploadRes;
import jh.exp.sys.servcie.storage.config.StorageProperties;
import jh.exp.sys.servcie.storage.service.StorageAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

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
    public byte[] download(String objectKey) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(objectKey)
                .build();
        return s3Client.getObjectAsBytes(request).asByteArray();
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

    private String buildPathStyleUrl(String objectKey) {
        if (!StringUtils.hasText(properties.getEndpoint())) {
            return objectKey;
        }
        String endpoint = properties.getEndpoint().endsWith("/") ? properties.getEndpoint().substring(0, properties.getEndpoint().length() - 1) : properties.getEndpoint();
        return endpoint + "/" + properties.getBucket() + "/" + objectKey;
    }
}
