package jh.exp.sys.core.res.storage;

import lombok.Data;

@Data
public class StorageUploadRes {
    private String objectKey;
    private String fileName;
    private Long fileSize;
    private String fileMd5;
    private String contentType;
    private String url;
}
