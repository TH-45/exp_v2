package jh.exp.corp.core.entity.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateNoticeReq {
    private String noticeCode;

    @NotBlank(message = "noticeType不能为空")
    private String noticeType;

    @NotBlank(message = "title不能为空")
    private String title;

    @NotBlank(message = "content不能为空")
    private String content;

    private LocalDateTime expireTime;
    private String scopeType;
    private String scopeDetail;
    private String remark;
}
