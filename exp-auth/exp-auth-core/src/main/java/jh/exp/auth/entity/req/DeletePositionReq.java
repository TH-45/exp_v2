package jh.exp.auth.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 删除岗位请求对象
 */
@Data
public class DeletePositionReq {

    /**
     * 岗位ID
     */
    @NotNull(message = "岗位ID不能为空")
    private Long postId;
}
