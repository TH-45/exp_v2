package jh.exp.auth.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 删除菜单请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteMenuReq {

    /**
     * 菜单ID
     */
    @NotNull(message = "菜单ID不能为空")
    private Long menuId;

}