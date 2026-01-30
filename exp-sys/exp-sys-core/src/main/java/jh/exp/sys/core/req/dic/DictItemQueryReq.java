package jh.exp.sys.core.req.dic;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DictItemQueryReq {
    @NotBlank(message = "dictCode 不能为空")
    private String dictCode;
    private String keyword;
    private String status;
}
