package jh.exp.corp.core.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCompanyReq {
    @NotNull(message = "companyId不能为空")
    private Long companyId;
    private String companyCode;
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
    private String remark;
}
