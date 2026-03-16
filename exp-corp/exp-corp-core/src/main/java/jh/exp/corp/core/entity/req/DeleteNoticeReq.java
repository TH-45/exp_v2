package jh.exp.corp.core.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeleteNoticeReq {
    @NotNull(message = "noticeId不能为空")
    private Long noticeId;
}
