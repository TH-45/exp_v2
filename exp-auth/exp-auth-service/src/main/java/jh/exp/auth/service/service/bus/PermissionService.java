package jh.exp.auth.service.service.bus;

import jh.exp.auth.core.entity.req.UpdateMenuTreePermissionReq;

/**
 * 权限服务（菜单权限树保存等）
 */
public interface PermissionService {

    /**
     * 更新角色菜单树权限：根据角色ID和选中的菜单ID列表，更新 exp_role_permission_rel（先删该角色下 auth 模块 MENU 类型关联，再按菜单插入）
     */
    void updateMenuTreePermission(UpdateMenuTreePermissionReq req);
}
