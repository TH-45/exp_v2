package jh.exp.project.core.entity.res;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProjectStatsRes {
    private Long totalProjects;
    private Long ongoingProjects;
    private Long completedProjects;
    private Long delayedProjects;
    private BigDecimal totalBudget;
    private BigDecimal totalCost;
}
