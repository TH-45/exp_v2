package jh.exp.process.core.entity.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StartProcessReq {
    private Long procDefId;
    private String procCode;

    @NotBlank(message = "业务主键不能为空")
    private String busId;
}
