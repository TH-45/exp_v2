package jh.exp.sys.core.req.dic;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DictItemCreateReq {
    @NotBlank(message = "dictCode 不能为空")
    private String dictCode;
    private String itemCode;
    @NotBlank(message = "itemValue 不能为空")
    private String itemValue;
    @NotBlank(message = "itemLabel 不能为空")
    private String itemLabel;
    private Integer sortNo;
    @NotBlank(message = "status 不能为空")
    private String status;
    private String remark;
}
