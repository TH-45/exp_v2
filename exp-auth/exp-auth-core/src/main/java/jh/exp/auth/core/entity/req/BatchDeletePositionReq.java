package jh.exp.auth.entity.req;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量删除岗位请求对象
 */
@Data
public class BatchDeletePositionReq {

    /**
     * 岗位ID列表
     */
    @NotEmpty(message = "岗位ID列表不能为空")
    private List<Long> postIds;
}
