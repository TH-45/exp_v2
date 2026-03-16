package jh.exp.corp.core.entity.res;

import lombok.Data;

@Data
public class QualificationStatsRes {
    private long valid;
    private long expiring;
    private long expired;
}
