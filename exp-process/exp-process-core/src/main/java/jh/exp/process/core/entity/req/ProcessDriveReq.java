package jh.exp.process.core.entity.req;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ProcessDriveReq {

    /**
     * 操作类型：  APPROVE | REJECT | FORCE_CLOSE
     */
    private String action;
    /**
     * 流程定义 ID
     */
    private Long procDefId;
    /**
     * 流程编码
     */
    private String procCode;
    /**
     * 业务类型  -创建
     */
    private String busType;
    /**
     * 业务类别  -创建
     */
    private String busCategory;
    /**
     * 业务 ID
     */
    private String busId;
    /**
     * 流程实例 ID -流转
     */
    private Long instanceId;
    /**
     * 任务 ID
     */
    private Long taskId;
    /**
     * 审批意见
     */
    private String comments;
    /**
     * 驳回原因
     */
    private String reason;
    /**
     * 幂等键
     */
    private String idempotencyKey;
    /**
     * 额外负载数据
     */
    private Map<String, Object> payload;
    /**
     * 附件列表
     */
    private List<ApprovalActionReq.AttachmentItem> attachments;
}
