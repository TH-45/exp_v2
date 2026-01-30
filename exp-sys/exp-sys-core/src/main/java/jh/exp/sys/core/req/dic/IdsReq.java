package jh.exp.sys.core.req.dic;

import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

import java.util.List;

@Data
public class IdsReq {
    private Long id;
    private List<Long> ids;


    /**
     * 验证 id 或 ids 是否至少有一个不为空
     * @return 如果 id 不为 null 或者 ids 列表不为 null 且不为空，则返回 true；否则返回 false
     */
    @AssertTrue(message = "id 或 ids 不能为空")
    public boolean isValidIds() {
        return id != null || (ids != null && !ids.isEmpty());
    }
}
