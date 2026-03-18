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
    /** 实际处理人编码 */
    private String handlerCode;
    /** 实际处理人 */
    private String handlerName;
    /** 待处理人ID（与 candidateName 成对） */
    private String candidateId;
    /** 待处理人编码（与 candidateId 成对） */
    private String candidateCode;
    /** 待处理人 */
    private String candidateName;
    private String opinion;
    private Integer isDone;
    private LocalDateTime createTime;
    private LocalDateTime finishTime;
}
