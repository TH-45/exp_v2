package jh.exp.project.core.entity.res;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProjectProgressRes {
    private Long projectId;
    private BigDecimal overallProgress;
    private List<ProjectMilestoneRes> milestones;
    private Integer delayedMilestones;
    private Integer completedMilestones;
    private Integer totalMilestones;
}
