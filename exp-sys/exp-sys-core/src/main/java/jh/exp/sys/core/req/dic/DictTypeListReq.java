package jh.exp.sys.core.req.dic;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class DictTypeListReq {
    @Min(value = 1, message = "page 不能小于 1")
    private Integer page;
    @Min(value = 1, message = "pageSize 不能小于 1")
    private Integer pageSize;
    private String dictCode;
    private String dictName;
    private String status;
}
