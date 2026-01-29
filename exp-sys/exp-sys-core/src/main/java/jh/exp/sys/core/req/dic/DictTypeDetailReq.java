package jh.exp.sys.core.req.dic;

import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

@Data
public class DictTypeDetailReq {
    private Long id;
    private String dictCode;

    @AssertTrue(message = "id 或 dictCode 不能为空")
    public boolean isValidQuery() {
        return id != null || (dictCode != null && !dictCode.isBlank());
    }
}
