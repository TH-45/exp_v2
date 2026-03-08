package jh.exp.sys.client.api.storage;

import jh.exp.common.core.api.ApiResponse;
import jh.exp.sys.core.req.storage.StorageBatchDownloadReq;
import jh.exp.sys.core.req.storage.StorageDeleteReq;
import jh.exp.sys.core.req.storage.StorageUploadBizReq;
import jh.exp.sys.core.res.storage.StorageUploadRes;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/storage")
public interface StorageService {
    @PostExchange(value = "/upload", contentType = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<StorageUploadRes> upload(@RequestPart("file") Resource file,
                                         @RequestPart("biz") StorageUploadBizReq biz);

    @PostExchange("/delete")
    ApiResponse<Void> delete(@RequestBody StorageDeleteReq req);

    @GetExchange("/download")
    byte[] download(@RequestParam("objectKey") String objectKey);

    @GetExchange("/exist")
    ApiResponse<Boolean> exist(@RequestParam("objectKey") String objectKey);

    @PostExchange("/batchDownload")
    byte[] batchDownload(@RequestBody StorageBatchDownloadReq req);
}
