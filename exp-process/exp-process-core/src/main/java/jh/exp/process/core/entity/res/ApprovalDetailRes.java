package jh.exp.process.core.entity.res;

import lombok.Data;

import java.util.List;

@Data
public class ApprovalDetailRes {
    private Long taskId;
    private Long instanceId;
    private String busType;
    private String busId;
    private String status;
    private String currentNode;
    private Long starterId;
    private Object businessData;
    private List<ApprovalHistoryRes> approvalHistory;
    private List<AttachmentRes> attachments;
}
