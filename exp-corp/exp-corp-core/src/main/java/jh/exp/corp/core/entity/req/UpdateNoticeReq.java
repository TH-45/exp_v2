package jh.exp.corp.core.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateNoticeReq {
    @NotNull(message = "noticeId不能为空")
    private Long noticeId;

    private String noticeCode;
    private String noticeType;
    private String title;
    private String content;
    private LocalDateTime expireTime;
    private String scopeType;
    private String scopeDetail;
    private String remark;
}
