package jh.exp.auth.core.entity.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 更新菜单树权限请求（角色勾选的菜单 ID 列表，用于保存角色-菜单权限）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateMenuTreePermissionReq {

    /**
     * 角色ID
     */
    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    /**
     * 更改的权限的等级（如 0-无权、1-查看、2-编辑、3-管理）
     */
    private String perLevel;

    /**
     * 选中的菜单ID列表（与权限树勾选节点对应）
     */
    private List<Long> menuIds;
}
