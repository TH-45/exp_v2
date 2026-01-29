package jh.exp.sys.core.req.dic;

import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

import java.util.List;

@Data
public class IdsReq {
    private Long id;
    private List<Long> ids;

    @AssertTrue(message = "id 或 ids 不能为空")
    public boolean isValidIds() {
        return id != null || (ids != null && !ids.isEmpty());
    }
}
