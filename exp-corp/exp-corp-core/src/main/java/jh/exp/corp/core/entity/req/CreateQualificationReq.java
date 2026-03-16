package jh.exp.corp.core.entity.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateQualificationReq {
    private Long companyId;
    private String qualificationCode;
    @NotBlank(message = "qualificationName不能为空")
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
    private String remark;
}
