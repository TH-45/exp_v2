package jh.exp.process.core.entity.req;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ProcessDriveReq {
    /**
     * CREATE | APPROVE | REJECT | FORCE_CLOSE
     */
    private String action;
    private Long procDefId;
    private String procCode;
    private String busType;
    private String busId;
    private Long instanceId;
    private Long taskId;
    private String comments;
    private String reason;
    private String idempotencyKey;
    private Map<String, Object> payload;
    private List<ApprovalActionReq.AttachmentItem> attachments;
}
