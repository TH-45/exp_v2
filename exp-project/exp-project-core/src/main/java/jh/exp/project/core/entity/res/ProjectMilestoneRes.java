package jh.exp.project.core.entity.res;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ProjectMilestoneRes {
    private Long id;
    private Long projectId;
    private String name;
    private String description;
    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
    private LocalDate actualStartDate;
    private LocalDate actualEndDate;
    private BigDecimal progress;
    private String status;
    private Long predecessorMilestoneId;
    private String responsiblePerson;
    private Long responsiblePersonId;
}
