package jh.exp.corp.core.entity.req;

import lombok.Data;

@Data
public class QueryCompanyContactReq {
    private Long companyId;
    private String contactName;
    private String mobile;
    private Integer isPrimary;
    private String status;
}
