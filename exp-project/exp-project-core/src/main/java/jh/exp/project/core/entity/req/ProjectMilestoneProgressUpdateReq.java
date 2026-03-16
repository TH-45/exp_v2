package jh.exp.project.core.entity.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ProjectMilestoneProgressUpdateReq {
    @NotNull(message = "milestoneId不能为空")
    private Long milestoneId;

    @NotNull(message = "progress不能为空")
    @Min(value = 0, message = "progress不能小于0")
    @Max(value = 100, message = "progress不能大于100")
    private BigDecimal progress;

    private LocalDate actualStartDate;

    private LocalDate actualEndDate;

    private String remarks;
}
