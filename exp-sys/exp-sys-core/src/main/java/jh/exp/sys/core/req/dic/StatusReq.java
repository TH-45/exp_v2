package jh.exp.sys.core.req.dic;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusReq {
    @NotNull(message = "id 不能为空")
    private Long id;
    @NotBlank(message = "status 不能为空")
    private String status;
}
