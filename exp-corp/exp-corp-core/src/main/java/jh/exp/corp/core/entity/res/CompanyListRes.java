package jh.exp.corp.core.entity.res;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CompanyListRes {
    private Long companyId;
    private String companyCode;
    private String companyName;
    private String companyShortName;
    private String companyType;
    private String unifiedSocialCreditCode;
    private String legalPerson;
    private String contactPhone;
    private String status;
    private LocalDateTime updatedTime;
}
