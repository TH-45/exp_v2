package jh.exp.process.core.entity.req;

import lombok.Data;

import java.util.List;

@Data
public class ApprovalActionReq {
    private Long taskId;
    private List<Long> taskIds;
    private String comments;
    private List<AttachmentItem> attachments;

    @Data
    public static class AttachmentItem {
        private String name;
        private String url;
        private Long size;
    }
}
