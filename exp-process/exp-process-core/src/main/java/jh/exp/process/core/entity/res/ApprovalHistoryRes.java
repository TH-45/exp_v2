package jh.exp.process.core.entity.res;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApprovalHistoryRes {
    private Long taskId;
    private Long nodeId;
    private String nodeName;
    private String action;
    private Long handlerId;
    private String opinion;
    private Integer isDone;
    private LocalDateTime createTime;
    private LocalDateTime finishTime;
}
