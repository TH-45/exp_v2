package jh.exp.sys.core.req.storage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StorageUploadBizReq {
    @NotBlank(message = "businessType 不能为空")
    private String businessType;

    @NotNull(message = "businessId 不能为空")
    private Long businessId;

    private String fileType;

    private String fileCategory;

    private String versionNo;

    private String securityLevel;

    private String remark;
}
