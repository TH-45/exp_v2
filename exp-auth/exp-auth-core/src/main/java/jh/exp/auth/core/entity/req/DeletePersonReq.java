package jh.exp.auth.core.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 删除人员请求对象
 */
@Data
public class DeletePersonReq {

    /**
     * 人员ID
     */
    @NotNull(message = "人员ID不能为空")
    private Long personId;
}
