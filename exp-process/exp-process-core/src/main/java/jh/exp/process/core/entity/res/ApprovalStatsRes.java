package jh.exp.process.core.entity.res;

import lombok.Data;

@Data
public class ApprovalStatsRes {
    private Long todoCount;
    private Long doneCount;
    private Long startedCount;
    private Long closedCount;
}
