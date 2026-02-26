package jh.exp.corp.core.entity.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCompanyReq {
    private String companyCode;
    @NotBlank(message = "企业全称不能为空")
    private String companyName;
    private String companyShortName;
    private String companyType;
    private String unifiedSocialCreditCode;
    private String taxNo;
    private String legalPerson;
    private String regAddress;
    private String officeAddress;
    private String contactPhone;
    private String contactEmail;
    private String website;
    private String status;
    private Long createdBy;
    private String remark;
}
