package jh.exp.sys.core.req.dic;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DictTypeCreateReq {
    @NotBlank(message = "dictCode 不能为空")
    private String dictCode;
    @NotBlank(message = "dictName 不能为空")
    private String dictName;
    @NotBlank(message = "status 不能为空")
    private String status;
    private String description;
}
