package jh.exp.auth.service.service.bus.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jh.exp.auth.core.entity.Menu;
import jh.exp.auth.core.entity.Permission;
import jh.exp.auth.core.entity.middle.RolePermissionRel;
import jh.exp.auth.core.entity.req.UpdateMenuTreePermissionReq;
import jh.exp.auth.core.mapper.MenuMapper;
import jh.exp.auth.core.mapper.PermissionMapper;
import jh.exp.auth.core.mapper.RolePermissionRelMapper;
import jh.exp.auth.service.service.bus.PermissionService;
import jh.exp.common.core.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private static final String MODULE_AUTH = "auth";
    private static final String PERM_TYPE_MENU = "MENU";
    private static final String MENU_PREFIX = "MENU_";

    private final MenuMapper menuMapper;
    private final PermissionMapper permissionMapper;
    private final RolePermissionRelMapper rolePermissionRelMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMenuTreePermission(UpdateMenuTreePermissionReq req) {
        Long roleId = req.getRoleId();
        if (roleId == null) {
            throw new BizException("角色ID不能为空");
        }

        // 1. 查 auth 模块下 MENU 类型的所有权限 ID
        List<Permission> menuPerms = permissionMapper.selectList(
                new LambdaQueryWrapper<Permission>()
                        .eq(Permission::getModuleCode, MODULE_AUTH)
                        .eq(Permission::getPermType, PERM_TYPE_MENU)
        );
        List<Long> menuPermIds = menuPerms.stream().map(Permission::getPermId).collect(Collectors.toList());

        // 2. 删除该角色下上述菜单权限的关联
        if (!menuPermIds.isEmpty()) {
            rolePermissionRelMapper.delete(
                    new LambdaQueryWrapper<RolePermissionRel>()
                            .eq(RolePermissionRel::getRoleId, roleId)
                            .in(RolePermissionRel::getPermId, menuPermIds)
            );
        }

        // 3. 若选中的菜单 ID 为空，则仅清空后返回
        List<Long> menuIds = req.getMenuIds();
        if (CollectionUtils.isEmpty(menuIds)) {
            return;
        }

        // 4. 根据菜单 ID 查菜单，得到 menuCode 列表
        List<Menu> menus = menuMapper.selectList(
                new LambdaQueryWrapper<Menu>().in(Menu::getMenuId, menuIds));
        if (menus == null || menus.isEmpty()) {
            return;
        }
        List<String> menuCodes = menus.stream().map(Menu::getMenuCode).collect(Collectors.toList());

        // 5. 根据 perm_code = MENU_ + menuCode 查权限 ID，并插入角色-权限关联
        List<RolePermissionRel> toInsert = new ArrayList<>();
        for (String menuCode : menuCodes) {
            String permCode = MENU_PREFIX + menuCode;
            Permission perm = permissionMapper.selectOne(
                    new LambdaQueryWrapper<Permission>()
                            .eq(Permission::getPermCode, permCode)
                            .eq(Permission::getModuleCode, MODULE_AUTH)
                            .eq(Permission::getPermType, PERM_TYPE_MENU)
            );
            if (perm != null) {
                RolePermissionRel rel = new RolePermissionRel();
                rel.setRoleId(roleId);
                rel.setPermId(perm.getPermId());
                if (req.getPerLevel() != null) {
                    rel.setGrantType(req.getPerLevel());
                }
                toInsert.add(rel);
            }
        }
        for (RolePermissionRel rel : toInsert) {
            rolePermissionRelMapper.insert(rel);
        }
    }
}
