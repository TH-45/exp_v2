package jh.exp.sys.core.req.dic;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DictTypeUpdateReq {
    @NotNull(message = "id 不能为空")
    private Long id;
    private String dictCode;
    private String dictName;
    private String status;
    private String description;
}
