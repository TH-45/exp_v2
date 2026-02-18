package jh.exp.auth.service.service.bus.Impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jh.exp.auth.core.constant.AuthConstant;
import jh.exp.auth.core.entity.Menu;
import jh.exp.auth.core.entity.Permission;
import jh.exp.auth.core.entity.Role;
import jh.exp.auth.core.entity.exp.PermissionExp;
import jh.exp.auth.core.entity.middle.RoleMenuRel;
import jh.exp.auth.core.entity.middle.RolePermissionRel;
import jh.exp.auth.core.entity.node.MenuNode;
import jh.exp.auth.core.entity.req.*;
import jh.exp.auth.core.entity.res.*;
import jh.exp.auth.core.mapper.PermissionMapper;
import jh.exp.auth.core.mapper.middle.RoleMenuRelMapper;
import jh.exp.auth.core.util.MenuTreeUtil;
import jh.exp.auth.core.util.PermParserUtil;
import jh.exp.auth.service.service.bus.MenuService;




import jh.exp.auth.core.mapper.MenuMapper;
import jh.exp.auth.core.mapper.RoleMapper;

import jh.exp.common.core.auth.CurrentUserHolder;
import jh.exp.common.core.auth.dto.CurrentUser;
import jh.exp.common.core.exception.BizException;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 菜单服务实现类
 */
@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuMapper menuMapper;
    private final RoleMenuRelMapper roleMenuRelMapper;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;

    /**
     * 分页查询菜单列表
     */
    @Override
    public SimplePageRes<MenuListRes> queryMenuList(SimplePageReq<QueryMenuReq> req) {
        req.pageDefault();
        Page<MenuListRes> page = new Page<>(req.getPageNum(), req.getPageSize());

        QueryMenuReq queryParam = req.getQueryParam();
        if (queryParam == null) {
            queryParam = new QueryMenuReq();
        }

        IPage<MenuListRes> result = menuMapper.selectMenuList(page,
                queryParam.getMenuCode(),
                queryParam.getMenuName(),
                queryParam.getMenuType(),
                queryParam.getStatus());

        SimplePageRes<MenuListRes> pageRes = new SimplePageRes<>();
        pageRes.setTotal(result.getTotal());
        pageRes.setPage(result.getCurrent());
        pageRes.setSize(result.getSize());
        pageRes.setList(result.getRecords());
        return pageRes;
    }

    /**
     * 查询菜单树
     */
    @Override
    public List<MenuTreeRes> queryMenuTree(QueryMenuReq req) {
        if (req == null) {
            req = new QueryMenuReq();
        }

        List<Menu> allMenus = menuMapper.selectList(new LambdaQueryWrapper<Menu>()
                .like(StringUtils.hasText(req.getMenuCode()), Menu::getMenuCode, req.getMenuCode())
                .like(StringUtils.hasText(req.getMenuName()), Menu::getMenuName, req.getMenuName())
                .eq(StringUtils.hasText(req.getMenuType()), Menu::getMenuType, req.getMenuType())
                .eq(StringUtils.hasText(req.getStatus()), Menu::getStatus, req.getStatus())
                .orderByAsc(Menu::getSortNo));

        return MenuTreeUtil.buildMenuTree(allMenus);
    }

    /**
     * 查询菜单权限树：查权限并结构对应到树（返回 tree + selectedMenuIds；roleId 不为空时查 exp_role_menu_rel 填充已选菜单；perLevel 表示权限等级，可选）
     */
    @Override
    public List<MenuPermissionTreeRes> queryMenuPermissionTree(Long roleId) {
        List<Menu> allMenus = menuMapper.selectList(new LambdaQueryWrapper<Menu>()
                .eq(Menu::getStatus, "ENABLED"));
        if (roleId != null) {
            // 查权限
            List<PermissionExp> permissionExps = permissionMapper.selectPermissionsByRoleId(roleId);
            List<String> permCodeList = permissionExps.stream().map(PermissionExp::getPermCode).toList();
            Map<String, String> permissionMap = PermParserUtil.parseBatch(permCodeList, AuthConstant.MENU);

            List<MenuPermissionTreeRes> menuPermissionTreeRes = MenuTreeUtil.buildMenuTree(
                    allMenus,
                    MenuPermissionTreeRes::new,
                    Menu::getMenuCode,
                    permissionMap,
                    MenuPermissionTreeRes::setPermLevel);

            return menuPermissionTreeRes;
        } else {
            throw new BizException("角色ID不能为空");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMenuPermissionTree(UpdateMenuPermissionTreeReq req) {
        // 1. 获取当前角色的权限
        List<PermissionExp> permissionExps = permissionMapper.selectPermissionsByRoleId(req.getRoleId());
        List<String> permCodeList = permissionExps.stream().map(PermissionExp::getPermCode).toList();
        Map<String, String> permissionMap = PermParserUtil.parseBatch(permCodeList, AuthConstant.MENU);

        // 2. 获取请求中的菜单节点
        List<UpdateMenuPermissionTreeReq.MenuNode> menuNodes = req.getMenuNodes();


        // 3. 构建待更新的权限列表
        List<RolePermissionRel> permissionsToAdd = new ArrayList<>();   // 待新增的权限
        List<String> permissionsToDelete = new ArrayList<>();       // 待删除的权限

        for (UpdateMenuPermissionTreeReq.MenuNode menuNode : menuNodes) {
            String menuCode = menuNode.getMenuCode();
            String permLevel = menuNode.getPermLevel();

            // 判断权限是否发生变化
            if (permissionMap.containsKey(menuCode)) {
                // 数据库中已有该权限
                if (!permLevel.equals(permissionMap.get(menuCode))) {
                    // 权限等级变化，需更新
                    permissionsToDelete.add(menuCode);  // 先删除旧权限
                    permissionsToAdd.add(buildPermission(req.getRoleId(), menuCode, permLevel)); // 再添加新权限
                }
            } else if(permLevel.equals("0")){
                permissionsToDelete.add(menuCode);
            }else {

                // 数据库中无该权限，需新增
                permissionsToAdd.add(buildPermission(req.getRoleId(), menuCode, permLevel));
            }
        }

        // 4. 执行权限更新
        // 删除旧权限
        if (!permissionsToDelete.isEmpty()) {
            permissionMapper.deletePermissionsByRoleId(req.getRoleId(), permissionsToDelete);
        }

        // 新增新权限
        if (!permissionsToAdd.isEmpty()) {
            permissionMapper.insertPermissionsByRoleId(permissionsToAdd);
        }
    }

    /**
     * 构建 PermissionExp 实体对象
     */
    private RolePermissionRel buildPermission(Long roleId,String menuCode, String permLevel) {
        RolePermissionRel  rolePermissionRel = new RolePermissionRel();
        rolePermissionRel.setRoleId(roleId);
        rolePermissionRel.setGrantType(permLevel);
        rolePermissionRel.setCreatedBy(CurrentUserHolder.get().getUserId());
        rolePermissionRel.setCreatedTime(LocalDateTime.now());
        String permCode = PermParserUtil.build(AuthConstant.MENU,menuCode, permLevel);

        Permission permission = permissionMapper.selectOne(new LambdaQueryWrapper<Permission>()
                .eq(Permission::getPermCode, permCode)
                .eq(Permission::getStatus, AuthConstant.ENABLED)
                .eq(Permission::getPermType, AuthConstant.MENU)
        );
        if (permission == null) {
            throw new BizException("权限不存在");
        }

        rolePermissionRel.setPermId(permission.getPermId());
        return rolePermissionRel;
    }



    /**
     * 根据ID查询菜单详情
     */
    @Override
    public MenuDetailRes getMenuById(Long menuId) {
        MenuDetailRes result = menuMapper.selectMenuDetailById(menuId);
        if (result == null) {
            throw new BizException("菜单不存在");
        }
        return result;
    }

    /**
     * 创建菜单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MenuDetailRes createMenu(CreateMenuReq req) {
        // 检查菜单编码是否已存在
        if (checkMenuCodeExists(req.getMenuCode(), null)) {
            throw new BizException("菜单编码已存在");
        }

        // 检查父菜单是否存在
        if (req.getParentMenuId() != null && req.getParentMenuId() > 0) {
            Menu parentMenu = menuMapper.selectById(req.getParentMenuId());
            if (parentMenu == null) {
                throw new BizException("父菜单不存在");
            }
            // 检查父菜单必须是目录或菜单类型
            if (!"MENU".equals(parentMenu.getMenuType())) {
                throw new BizException("父菜单必须是目录或菜单类型");
            }
        }

        // 创建菜单实体
        Menu menu = new Menu();
        BeanUtils.copyProperties(req, menu);
        menu.setStatus("ENABLED");
        if (menu.getVisible() == null) {
            menu.setVisible(1);
        }
        if (menu.getSortNo() == null) {
            menu.setSortNo(0);
        }

        // 保存菜单
        menuMapper.insert(menu);

        return getMenuById(menu.getMenuId());
    }

    /**
     * 更新菜单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MenuDetailRes updateMenu(UpdateMenuReq req) {
        // 检查菜单是否存在
            Menu existingMenu = menuMapper.selectById(req.getMenuId());
        if (existingMenu == null) {
            throw new BizException("菜单不存在");
        }

        // 检查菜单编码是否已存在
        if (checkMenuCodeExists(req.getMenuCode(), req.getMenuId())) {
            throw new BizException("菜单编码已存在");
        }

        // 检查父菜单是否存在
        if (req.getParentMenuId() != null && req.getParentMenuId() > 0) {
            Menu parentMenu = menuMapper.selectById(req.getParentMenuId());
            if (parentMenu == null) {
                throw new BizException("父菜单不存在");
            }
            // 检查父菜单必须是目录或菜单类型
            if ("MENU".equals(parentMenu.getMenuType())) {
                throw new BizException("父菜单必须是目录或菜单类型");
            }
            // 防止将菜单设置为自己的子菜单
            if (existingMenu.getParentMenuId() != null &&
                existingMenu.getParentMenuId().equals(req.getParentMenuId())) {
                throw new BizException("不能将菜单设置为自己的父菜单");
            }
        }

        // 更新菜单信息
        Menu menu = new Menu();
        BeanUtils.copyProperties(req, menu);

        menuMapper.updateById(menu);

        return getMenuById(req.getMenuId());
    }

    /**
     * 删除菜单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(Long menuId) {
        Menu menu = menuMapper.selectById(menuId);
        if (menu == null) {
            throw new BizException("菜单不存在");
        }

        // 检查是否有子菜单
        Long childCount = menuMapper.selectCount(new LambdaQueryWrapper<Menu>()
                .eq(Menu::getParentMenuId, menuId));
        if (childCount > 0) {
            throw new BizException("该菜单下有子菜单，不能删除");
        }

        // 检查是否有角色关联此菜单
        Long roleCount = roleMenuRelMapper.selectCount(new QueryWrapper<RoleMenuRel>()
                .eq("menu_id", menuId));
        if (roleCount > 0) {
            throw new BizException("该菜单已被角色使用，不能删除");
        }

        menuMapper.deleteById(menuId);
    }

    /**
     * 批量删除菜单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteMenus(BatchDeleteMenuReq req) {
        for (Long menuId : req.getMenuIds()) {
            deleteMenuInternal(menuId);
        }
    }

    /**
     * 更改菜单状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MenuDetailRes updateMenuStatus(MenuStatusReq req) {
        Menu menu = menuMapper.selectById(req.getMenuId());
        if (menu == null) {
            throw new BizException("菜单不存在");
        }

        // 验证状态值
        if (!"ENABLED".equals(req.getStatus()) && !"DISABLED".equals(req.getStatus())) {
            throw new BizException("无效的状态值");
        }

        menu.setStatus(req.getStatus());
        menuMapper.updateById(menu);

        return getMenuById(req.getMenuId());
    }

    /**
     * 批量更改菜单状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateMenuStatus(BatchMenuStatusReq req) {
        // 验证状态值
        if (!"ENABLED".equals(req.getStatus()) && !"DISABLED".equals(req.getStatus())) {
            throw new BizException("无效的状态值");
        }

        UpdateWrapper<Menu> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("menu_id", req.getMenuIds())
                    .set("status", req.getStatus());

        menuMapper.update(null, updateWrapper);
    }

    /**
     * 检查菜单编码是否存在
     */
    @Override
    public boolean checkMenuCodeExists(String menuCode, Long excludeMenuId) {
        return menuMapper.countByMenuCode(menuCode, excludeMenuId) > 0;
    }

    /**
     * 获取用户菜单权限（原有方法）
     */
    @Override
    public MenusRes getMenus(CurrentUser currentUser) {
        List<String> roles = currentUser.getRoles();
        if (CollUtil.isEmpty(roles)) {
            throw new IllegalArgumentException("当前用户没有角色");
        }

        QueryWrapper<Role> roleQw = new QueryWrapper<>();
        roleQw.in("role_code", roles);
        List<Long> roleIds = roleMapper.selectList(roleQw).stream().map(Role::getRoleId).toList();

        QueryWrapper<RoleMenuRel> middleQw = new QueryWrapper<>();
        middleQw.in("role_id", roleIds);
        List<Long> menuIds = roleMenuRelMapper.selectList(middleQw).stream().map(RoleMenuRel::getMenuId).toList();

        QueryWrapper<Menu> menuQw = new QueryWrapper<>();
        menuQw.in("menu_id", menuIds);
        menuQw.orderByAsc("parent_menu_id", "sort_no");
        List<Menu> expMenus = menuMapper.selectList(menuQw);

        // 拼接树形结构
        TreeNodeConfig config = new TreeNodeConfig();
        config.setIdKey("menuId");
        config.setParentIdKey("parentMenuId");
        config.setChildrenKey("children");
        config.setWeightKey("sortNo");

        List<Tree<Long>> treeList = TreeUtil.build(
                expMenus,
                0L,     // 根节点 parentId
                config,
                (menu, tree) -> {
                    tree.setId(menu.getMenuId());
                    tree.setParentId(menu.getParentMenuId());
                    tree.setName(menu.getMenuName());
                    tree.putExtra("menuCode", menu.getMenuCode());
                    tree.putExtra("menuName", menu.getMenuName());
                }
        );

        List<MenuNode> menus = treeList.stream().map(this::toMenuNode).toList();
        // 目前是"按用户角色集合"合并后的菜单树，不绑定单一角色信息
        return new MenusRes(roleIds, roles, null, menus);
    }



    /**
     * 内部删除菜单方法（不带事务注解，避免事务嵌套问题）
     */
    private void deleteMenuInternal(Long menuId) {
        Menu menu = menuMapper.selectById(menuId);
        if (menu == null) {
            throw new BizException("菜单不存在");
        }

        // 检查是否有子菜单
        Long childCount = menuMapper.selectCount(new LambdaQueryWrapper<Menu>()
                .eq(Menu::getParentMenuId, menuId));
        if (childCount > 0) {
            throw new BizException("该菜单下有子菜单，不能删除");
        }

        // 检查是否有角色关联此菜单
        Long roleCount = roleMenuRelMapper.selectCount(new QueryWrapper<RoleMenuRel>()
                .eq("menu_id", menuId));
        if (roleCount > 0) {
            throw new BizException("该菜单已被角色使用，不能删除");
        }

        menuMapper.deleteById(menuId);
    }

    /**
     * 转换Tree为MenuNode
     */
    private MenuNode toMenuNode(Tree<Long> tree) {
        MenuNode node = new MenuNode();
        node.menuCode = StrUtil.toString(tree.get("menuCode"));
        node.menuName = StrUtil.toString(tree.get("menuName"));
        List<Tree<Long>> children = tree.getChildren();
        if (CollUtil.isEmpty(children)) {
            node.childMenuNodes = new ArrayList<>();
        } else {
            node.childMenuNodes = children.stream().map(this::toMenuNode).toList();
        }
        return node;
    }
}