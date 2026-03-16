package jh.exp.corp.core.entity.res;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class NoticeListRes {
    private Long noticeId;
    private String noticeType;
    private String title;
    private Long publisherUserId;
    private LocalDateTime publishTime;
    private String publishStatus;
    private Integer readCount;
    private List<NoticeAttachmentRes> attachments;
}
