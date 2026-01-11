package jh.exp.auth.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量菜单状态变更请求对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchMenuStatusReq {

    /**
     * 菜单ID列表
     */
    @NotEmpty(message = "菜单ID列表不能为空")
    private List<Long> menuIds;

    /**
     * 状态（ENABLED-启用，DISABLED-停用）
     */
    @NotBlank(message = "状态不能为空")
    private String status;
}