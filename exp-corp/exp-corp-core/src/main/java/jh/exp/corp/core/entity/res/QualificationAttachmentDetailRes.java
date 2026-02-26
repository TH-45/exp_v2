package jh.exp.corp.core.entity.res;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QualificationAttachmentDetailRes {
    private Long attachmentId;
    private Long qualificationId;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private Long uploadUserId;
    private LocalDateTime uploadTime;
    private String remark;
}
