package jh.exp.sys.core.req.storage;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StorageDeleteReq {
    @NotBlank(message = "objectKey 不能为空")
    private String objectKey;
}
