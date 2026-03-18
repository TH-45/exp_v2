package jh.exp.process.core.entity.res;

import lombok.Data;

import java.util.List;

@Data
public class ApprovalDetailRes {
    /** 任务 ID */
    private Long taskId;
    /** 流程实例 ID */
    private Long instanceId;
    /** 业务类型 */
    private String busType;
    /** 流程编号 */
    private String procCode;
    /** 业务 ID */
    private Long busId;
    /** 状态 */
    private String status;
    /** 当前节点 */
    private String currentNode;
    /** 发起人 ID */
    private Long starterId;
    /** 发起人姓名 */
    private String starterName;
    /** 业务数据 */
    private Object businessData;
    /** 审批历史列表 */
    private List<ApprovalHistoryRes> approvalHistory;
    /** 附件列表 */
    private List<AttachmentRes> attachments;

    /** 任务标题 */
    private String title;

}
