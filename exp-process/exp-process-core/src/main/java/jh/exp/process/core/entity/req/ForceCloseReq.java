package jh.exp.process.core.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ForceCloseReq {
    @NotNull(message = "实例ID不能为空")
    private Long instanceId;

    private String reason;
    /** 动作，默认 CLOSE */
    private String action;
}
