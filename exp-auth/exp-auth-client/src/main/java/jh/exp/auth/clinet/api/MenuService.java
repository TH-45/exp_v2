package jh.exp.auth.clinet.api;


import jh.exp.auth.core.entity.req.*;
import jh.exp.auth.core.entity.res.MenuDetailRes;
import jh.exp.auth.core.entity.res.MenuListRes;
import jh.exp.auth.core.entity.res.MenuTreeRes;
import jh.exp.auth.core.entity.res.MenusRes;
import jh.exp.common.core.auth.dto.CurrentUser;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;

import java.util.List;

public interface MenuService {

    /**
     * 分页查询菜单列表
     */
    SimplePageRes<MenuListRes> queryMenuList(SimplePageReq<QueryMenuReq> req);

    /**
     * 查询菜单树
     */
    List<MenuTreeRes> queryMenuTree(QueryMenuReq req);

    /**
     * 根据ID查询菜单详情
     */
    MenuDetailRes getMenuById(Long menuId);

    /**
     * 创建菜单
     */
    MenuDetailRes createMenu(CreateMenuReq req);

    /**
     * 更新菜单
     */
    MenuDetailRes updateMenu(UpdateMenuReq req);

    /**
     * 删除菜单
     */
    void deleteMenu(Long menuId);

    /**
     * 批量删除菜单
     */
    void batchDeleteMenus(BatchDeleteMenuReq req);

    /**
     * 更改菜单状态
     */
    MenuDetailRes updateMenuStatus(MenuStatusReq req);

    /**
     * 批量更改菜单状态
     */
    void batchUpdateMenuStatus(BatchMenuStatusReq req);

    /**
     * 检查菜单编码是否存在
     */
    boolean checkMenuCodeExists(String menuCode, Long excludeMenuId);

    /**
     * 获取用户菜单权限（原有方法）
     */
    MenusRes getMenus(CurrentUser currentUser);

}
