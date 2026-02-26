package jh.exp.corp.core.entity.res;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class QualificationDetailRes {
    private Long qualificationId;
    private Long companyId;
    private String qualificationCode;
    private String qualificationName;
    private String qualificationType;
    private String issueOrg;
    private LocalDate issueDate;
    private LocalDate validFrom;
    private LocalDate validTo;
    private Integer warnDays;
    private String status;
    private String fileNo;
    private Integer attachFlag;
    private Long createdBy;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private String remark;
}
