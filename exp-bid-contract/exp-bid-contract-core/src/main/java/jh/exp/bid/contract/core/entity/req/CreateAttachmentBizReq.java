package jh.exp.bid.contract.core.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateAttachmentBizReq {
    @NotBlank(message = "业务类型不能为空")
    private String businessType;

    @NotNull(message = "业务ID不能为空")
    private Long businessId;

    @NotBlank(message = "文件类型不能为空")
    private String fileType;

    private String fileCategory;

    private String versionNo;

    private String securityLevel;

    private String remark;
}
