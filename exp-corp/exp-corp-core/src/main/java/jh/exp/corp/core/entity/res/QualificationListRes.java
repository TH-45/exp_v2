package jh.exp.corp.core.entity.res;

import lombok.Data;

import java.time.LocalDate;

@Data
public class QualificationListRes {
    private Long qualificationId;
    private Long companyId;
    private String qualificationCode;
    private String qualificationName;
    private String qualificationType;
    private String issueOrg;
    private LocalDate issueDate;
    private LocalDate validTo;
    private String status;
    private Integer attachFlag;
}
