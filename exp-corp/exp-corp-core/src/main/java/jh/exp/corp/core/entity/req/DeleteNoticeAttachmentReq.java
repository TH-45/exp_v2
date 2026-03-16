package jh.exp.corp.core.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeleteNoticeAttachmentReq {
    @NotNull(message = "attachmentId不能为空")
    private Long attachmentId;
}
