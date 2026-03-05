package jh.exp.process.core.entity.res;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApprovalTaskRes {
    private Long taskId;
    private Long instanceId;
    private String title;
    private String busType;
    private String busId;
    private Long starterId;
    private LocalDateTime startTime;
    private String currentNode;
    private String status;
    private Integer isDone;
}
