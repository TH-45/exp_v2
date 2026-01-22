package jh.exp.auth.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 岗位状态变更请求对象
 */
@Data
public class PositionStatusReq {

    /**
     * 岗位ID
     */
    @NotNull(message = "岗位ID不能为空")
    private Long postId;

    /**
     * 状态
     */
    @NotBlank(message = "状态不能为空")
    private String status;
}
