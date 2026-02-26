package jh.exp.corp.core.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateQualificationReq {
    @NotNull(message = "qualificationId不能为空")
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
    private String remark;
}
