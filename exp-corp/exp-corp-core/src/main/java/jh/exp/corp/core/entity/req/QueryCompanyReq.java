package jh.exp.corp.core.entity.req;

import lombok.Data;

@Data
public class QueryCompanyReq {
    private String companyCode;
    private String companyName;
    private String companyType;
    private String status;
}
