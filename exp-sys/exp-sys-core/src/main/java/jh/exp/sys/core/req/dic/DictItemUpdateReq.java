package jh.exp.sys.core.req.dic;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DictItemUpdateReq {
    @NotNull(message = "id 不能为空")
    private Long id;
    private String dictCode;
    private String itemCode;
    private String itemValue;
    private String itemLabel;
    private Integer sortNo;
    private String status;
    private String remark;
}
