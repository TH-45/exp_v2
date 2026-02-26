package jh.exp.corp.core.entity.res;

import lombok.Data;

@Data
public class CompanyContactListRes {
    private Long contactId;
    private Long companyId;
    private String contactName;
    private String position;
    private String mobile;
    private String email;
    private Integer isPrimary;
    private String status;
}
