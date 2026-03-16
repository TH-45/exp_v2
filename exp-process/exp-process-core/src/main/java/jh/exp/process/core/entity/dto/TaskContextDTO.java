package jh.exp.process.core.entity.dto;

import lombok.Data;

/**
 * 任务上下文 DTO，用于 taskId 一次查询关联的 task、instance、definition 信息
 */
@Data
public class TaskContextDTO {
    private Long taskId;
    private Long instanceId;
    private Long busId;
    private String instanceStatus;
    private String busType;
    private String procCode;
}
