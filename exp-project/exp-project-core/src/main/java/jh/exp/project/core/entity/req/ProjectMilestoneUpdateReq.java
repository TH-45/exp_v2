package jh.exp.project.core.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProjectMilestoneUpdateReq {
    @NotNull(message = "id不能为空")
    private Long id;

    @NotBlank(message = "name不能为空")
    private String name;

    private String description;

    @NotNull(message = "plannedStartDate不能为空")
    private LocalDate plannedStartDate;

    @NotNull(message = "plannedEndDate不能为空")
    private LocalDate plannedEndDate;

    private Long predecessorMilestoneId;

    @NotNull(message = "responsiblePersonId不能为空")
    private Long responsiblePersonId;
}
