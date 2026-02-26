package jh.exp.corp.core.entity.res;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CompanyDetailRes {
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
    private Long createdBy;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private String remark;
}
