package jh.exp.sys.core.req.dic;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BatchStatusReq {
    @NotEmpty(message = "ids 不能为空")
    private List<Long> ids;
    @NotBlank(message = "status 不能为空")
    private String status;
}
