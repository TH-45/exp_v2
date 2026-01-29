package jh.exp.sys.core.req.dic;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DictItemListReq {
    @NotBlank(message = "dictCode 不能为空")
    private String dictCode;
    @Min(value = 1, message = "page 不能小于 1")
    private Integer page;
    @Min(value = 1, message = "pageSize 不能小于 1")
    private Integer pageSize;
    private String keyword;
    private String status;
}
