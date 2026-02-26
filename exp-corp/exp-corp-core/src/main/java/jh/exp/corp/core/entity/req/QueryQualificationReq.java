package jh.exp.corp.core.entity.req;

import lombok.Data;

@Data
public class QueryQualificationReq {
    private Long companyId;
    private String qualificationCode;
    private String qualificationName;
    private String qualificationType;
    private String status;
}
