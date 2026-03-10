package jh.exp.corp.core.entity.req;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateCompanyReq {
    @NotNull(message = "companyId不能为空")
    private Long companyId;
    private String companyCode;
    @NotBlank(message = "企业全称不能为空")
    private String companyName;
    private String companyShortName;
    @NotBlank(message = "企业类型不能为空")
    private String companyType;
    @NotBlank(message = "统一社会信用代码不能为空")
    private String unifiedSocialCreditCode;
    private String taxNo;
    private String legalPerson;
    private String regAddress;
    @NotBlank(message = "办公地址不能为空")
    private String officeAddress;
    private String contactPhone;
    private String contactEmail;
    private String website;
    private String status;
    private String remark;
}
