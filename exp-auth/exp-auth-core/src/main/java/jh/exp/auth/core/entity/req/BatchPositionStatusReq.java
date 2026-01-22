package jh.exp.auth.core.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量岗位状态变更请求对象
 */
@Data
public class BatchPositionStatusReq {

    /**
     * 岗位ID列表
     */
    @NotEmpty(message = "岗位ID列表不能为空")
    private List<Long> postIds;

    /**
     * 状态
     */
    @NotBlank(message = "状态不能为空")
    private String status;
}
