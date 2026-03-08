package jh.exp.sys.service.storage.service;

import jh.exp.sys.core.req.storage.StorageBatchDownloadReq;
import jh.exp.sys.core.req.storage.StorageUploadBizReq;
import jh.exp.sys.core.res.storage.StorageUploadRes;
import org.springframework.web.multipart.MultipartFile;

public interface StorageAppService {
    StorageUploadRes upload(MultipartFile file, StorageUploadBizReq biz);

    boolean exists(String objectKey);

    byte[] download(String objectKey);

    byte[] batchDownload(StorageBatchDownloadReq req);

    void delete(String objectKey);
}
