package jh.exp.corp.core.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateQualificationAttachmentReq {
    @NotNull(message = "attachmentId不能为空")
    private Long attachmentId;
    private Long qualificationId;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private Long uploadUserId;
    private LocalDateTime uploadTime;
    private String remark;
}
