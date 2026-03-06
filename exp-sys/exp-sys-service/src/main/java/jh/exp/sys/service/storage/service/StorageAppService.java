package jh.exp.sys.service.storage.service;

import jh.exp.sys.core.req.storage.StorageUploadBizReq;
import jh.exp.sys.core.res.storage.StorageUploadRes;
import org.springframework.web.multipart.MultipartFile;

public interface StorageAppService {
    StorageUploadRes upload(MultipartFile file, StorageUploadBizReq biz);

    byte[] download(String objectKey);

    void delete(String objectKey);
}
