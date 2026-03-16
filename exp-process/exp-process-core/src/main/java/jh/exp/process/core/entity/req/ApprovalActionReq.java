package jh.exp.process.core.entity.req;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
public class ApprovalActionReq {
    /** 任务 ID */
    @NotNull(message = "请选择工单")
    private Long taskId;
//    /** 任务 ID 列表 */
//    private List<Long> taskIds;
    /** 审批类型 */
    @NotNull(message = "请选择审批类型")
    private String action;
    /** 审批意见 */
    private String comments;
    /** 附件列表 */
    private List<AttachmentItem> attachments;

    @Data
    public static class AttachmentItem {
        /** 附件名称 */
        private String name;
        /** 附件 URL */
        private String url;
        /** 附件大小（字节） */
        private Long size;
    }
}
