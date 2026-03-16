package jh.exp.corp.core.entity.res;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoticeAttachmentRes {
    private Long attachmentId;
    private Long noticeId;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private Long uploadUserId;
    private LocalDateTime uploadTime;
}
