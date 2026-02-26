package jh.exp.corp.core.entity.res;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QualificationAttachmentListRes {
    private Long attachmentId;
    private Long qualificationId;
    private String fileName;
    private Long fileSize;
    private Long uploadUserId;
    private LocalDateTime uploadTime;
}
