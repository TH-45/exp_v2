package jh.exp.process.core.entity.res;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApprovalTaskRes {
    /** 任务 ID */
    private Long taskId;
    /** 流程实例 ID */
    private Long instanceId;
    /** 标题 */
    private String title;
    /** 业务类型 */
    private String busType;
    /** 业务 ID */
    private Long busId;
    /** 发起人 ID */
    private Long starterId;
    /** 开始时间 */
    private LocalDateTime startTime;
    /** 当前节点 */
    private String currentNode;
    /** 状态 */
    private String status;
    /** 是否完成 (0:未完成, 1:已完成) */
    private Integer isDone;

    /** 当前处理人姓名/标识（多个用逗号隔开） */
    private String currentHandler;

}
