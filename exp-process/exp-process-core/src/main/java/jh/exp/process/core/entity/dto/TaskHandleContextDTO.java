package jh.exp.process.core.entity.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审批处理上下文 DTO，用于 taskId 一次查询 task + instance 关联信息（下沉到 XML，减少 2 次查询）
 */
@Data
public class TaskHandleContextDTO {
    // --- WfTask 字段 ---
    private Long taskId;
    private Long instanceId;
    private Long nodeId;
    private String candidateType;
    private String candidateId;
    private Long handlerId;
    private String action;
    private String opinion;
    private Integer isDone;
    private LocalDateTime createTime;
    private LocalDateTime finishTime;

    // --- WfInstance 字段（用于后续 updateById）---
    private Long instanceProcDefId;
    private Long instanceBusId;
    private Long instanceStarterId;
    private LocalDateTime instanceStartTime;
    private LocalDateTime instanceEndTime;
    private String instanceStatus;
    private Long instanceClosedBy;
    private String instanceCloseReason;
    private String instanceTitle;
}
