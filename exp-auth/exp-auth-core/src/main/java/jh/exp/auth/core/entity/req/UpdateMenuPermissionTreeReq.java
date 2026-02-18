package jh.exp.auth.core.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMenuPermissionTreeReq {
    /**
     * 角色id
     */
    @NotNull(message = "角色id不能为空")
    private Long roleId;

    /**
     * 菜单节点列表
     */
    private List<MenuNode> menuNodes;

    /**
     * 菜单修改节点
     */
    @Data
    @NoArgsConstructor
    public static class MenuNode {
        /**
         * 菜单编码
         */
        private String menuCode;

        /**
         * 权限等级
         */
        private String permLevel;

    }



}
