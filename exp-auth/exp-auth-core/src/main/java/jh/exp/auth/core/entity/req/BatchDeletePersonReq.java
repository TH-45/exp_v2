package jh.exp.auth.entity.req;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量删除人员请求对象
 */
@Data
public class BatchDeletePersonReq {

    /**
     * 要删除的人员ID列表
     */
    @NotEmpty(message = "人员ID列表不能为空")
    private List<Long> personIds;
}
