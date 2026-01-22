package jh.exp.auth.core.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 菜单状态变更请求对象
 */
@Data
public class MenuStatusReq {

    /**
     * 菜单ID
     */
    @NotNull(message = "菜单ID不能为空")
    private Long menuId;

    /**
     * 状态
     */
    @NotBlank(message = "状态不能为空")
    private String status;
}