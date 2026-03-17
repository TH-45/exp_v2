package jh.exp.process.core.entity.res;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApprovalHistoryRes {
    private Long taskId;
    private Long nodeId;
    private String nodeName;
    private String action;
    /** 动作中文标签 */
    private String actionLabel;
    private Long handlerId;
    /** 处理人姓名 */
    private String handlerName;
    private String opinion;
    private Integer isDone;
    private LocalDateTime createTime;
    private LocalDateTime finishTime;
}
