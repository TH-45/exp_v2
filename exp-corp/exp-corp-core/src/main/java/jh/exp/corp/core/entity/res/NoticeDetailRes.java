package jh.exp.corp.core.entity.res;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class NoticeDetailRes {
    private Long noticeId;
    private String noticeCode;
    private String noticeType;
    private String title;
    private String content;
    private Integer attachFlag;
    private String publishStatus;
    private LocalDateTime publishTime;
    private LocalDateTime expireTime;
    private String scopeType;
    private String scopeDetail;
    private Long creatorUserId;
    private Long publisherUserId;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private String remark;
    private Integer readCount;
    private List<NoticeAttachmentRes> attachments;
}
