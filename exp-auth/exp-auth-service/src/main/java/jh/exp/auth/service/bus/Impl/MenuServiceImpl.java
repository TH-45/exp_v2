package jh.exp.auth.service.bus.Impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jh.exp.auth.entity.ExpMenu;
import jh.exp.auth.entity.Role;
import jh.exp.auth.entity.middle.ExpRoleMenuRel;
import jh.exp.auth.entity.node.MenuNode;
import jh.exp.auth.entity.res.MenusRes;
import jh.exp.auth.mapper.MenuMapper;
import jh.exp.auth.mapper.RoleMapper;
import jh.exp.auth.service.bus.MenuService;
import jh.exp.auth.mapper.middle.ExpRoleMenuRelMapper;
import jh.exp.common.auth.dto.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MenuServiceImpl implements MenuService {
    @Autowired
    private MenuMapper menuMapper;

    @Autowired
    private ExpRoleMenuRelMapper expRoleMenuRelMapper;

    @Autowired
    private RoleMapper roleMapper;

    //获取菜单
    @Override
    public MenusRes getMenus(CurrentUser currentUser) {
        List<String> roles = currentUser.getRoles();
        if(CollUtil.isEmpty(roles)){throw new IllegalArgumentException("当前用户没有角色");}

        QueryWrapper<Role> roleQw = new QueryWrapper<>();
        roleQw.in("role_code", roles);
        List<Long> roleIds= roleMapper.selectList(roleQw).stream().map(Role::getRoleId).toList();

        QueryWrapper<ExpRoleMenuRel> middleQw = new QueryWrapper<>();
        middleQw.in("role_id", roleIds);
        List<Long> menuIds = expRoleMenuRelMapper.selectList(middleQw).stream().map(ExpRoleMenuRel::getMenuId).toList();

        QueryWrapper<ExpMenu> menuQw = new QueryWrapper<>();
        menuQw.in("menu_id", menuIds);
        menuQw.orderByAsc("parent_menu_id", "sort_no");
        List<ExpMenu> expMenus = menuMapper.selectList(menuQw);


        //拼接树形结构
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
        // 目前是“按用户角色集合”合并后的菜单树，不绑定单一角色信息
        return new MenusRes(roleIds, roles, null, menus);
    }

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
