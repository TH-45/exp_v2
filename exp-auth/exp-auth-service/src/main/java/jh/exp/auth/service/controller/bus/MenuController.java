package jh.exp.auth.service.controller.bus;





import jh.exp.auth.core.entity.req.*;
import jh.exp.auth.core.entity.res.MenuDetailRes;
import jh.exp.auth.core.entity.res.MenuListRes;
import jh.exp.auth.core.entity.res.MenuPermissionTreeRes;
import jh.exp.auth.core.entity.res.MenuTreeRes;
import jh.exp.auth.service.service.bus.MenuService;
import jh.exp.common.core.annotation.RequiresMenuLevel;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
@RequiresMenuLevel(code = "system:menu", level = 1)
public class MenuController {

    private final MenuService menuService;

    /**
     * 分页查询菜单列表
     */
    @PostMapping("/list")
    public ApiResponse<SimplePageRes<MenuListRes>> list(@RequestBody SimplePageReq<QueryMenuReq> req) {
        req.pageDefault();
        SimplePageRes<MenuListRes> result = menuService.queryMenuList(req);
        return ApiResponse.success(result);
    }

    /**
     * 查询菜单树
     */
    @GetMapping("/tree")
    public ApiResponse<List<MenuTreeRes>> tree(QueryMenuReq req) {
        if (req == null) {
            req = new QueryMenuReq();
        }
        List<MenuTreeRes> result = menuService.queryMenuTree(req);
        return ApiResponse.success(result);
    }

    /**
     * 查询菜单权限树（可传 perLevel 表示要查询/过滤的权限等级）
     */
    @GetMapping("/permissionTree")
    public ApiResponse<List<MenuPermissionTreeRes>> permissionTree(@RequestParam(required = false) Long roleId) {
        List<MenuPermissionTreeRes> result = null;
        try {
            result = menuService.queryMenuPermissionTree(roleId);
        } catch (Exception e) {
            return ApiResponse.fail(e.getMessage());
        }
        return ApiResponse.success(result);
    }

    /**
     * 修改菜单权限树
     */
    @PostMapping("/updatePermissionTree")
    @RequiresMenuLevel(code = "system:role", level = 2)
    public ApiResponse<Void> updatePermissionTree(@RequestBody @Valid UpdateMenuPermissionTreeReq req) {
        if (req.getMenuNodes() == null) {
            return ApiResponse.success( null);
        }
        menuService.updateMenuPermissionTree(req);
        return ApiResponse.success(null);
    }

    /**
     * 根据ID查询菜单详情
     */
    @GetMapping("/detail")
    public ApiResponse<MenuDetailRes> detail(@RequestParam Long menuId) {
        MenuDetailRes result = menuService.getMenuById(menuId);
        return ApiResponse.success(result);
    }

    /**
     * 创建菜单
     */
    @PostMapping("/create")
    @RequiresMenuLevel(code = "system:menu", level = 2)
    public ApiResponse<MenuDetailRes> create(@RequestBody @Valid CreateMenuReq req) {
        MenuDetailRes result = menuService.createMenu(req);
        return ApiResponse.success(result);
    }

    /**
     * 更新菜单
     */
    @PostMapping("/update")
    @RequiresMenuLevel(code = "system:menu", level = 2)
    public ApiResponse<MenuDetailRes> update(@RequestBody @Valid UpdateMenuReq req) {
        MenuDetailRes result = menuService.updateMenu(req);
        return ApiResponse.success(result);
    }

    /**
     * 删除菜单
     */
    @PostMapping("/delete")
    @RequiresMenuLevel(code = "system:menu", level = 3)
    public ApiResponse<Void> delete(@RequestBody @Valid DeleteMenuReq req) {
        menuService.deleteMenu(req.getMenuId());
        return ApiResponse.success(null);
    }

    /**
     * 批量删除菜单
     */
    @PostMapping("/batchDelete")
    @RequiresMenuLevel(code = "system:menu", level = 3)
    public ApiResponse<Void> batchDelete(@RequestBody @Valid BatchDeleteMenuReq req) {
        menuService.batchDeleteMenus(req);
        return ApiResponse.success(null);
    }

    /**
     * 更改菜单状态
     */
    @PostMapping("/status")
    @RequiresMenuLevel(code = "system:menu", level = 2)
    public ApiResponse<MenuDetailRes> updateStatus(@RequestBody @Valid MenuStatusReq req) {
        MenuDetailRes result = menuService.updateMenuStatus(req);
        return ApiResponse.success(result);
    }

    /**
     * 批量更改菜单状态
     */
    @PostMapping("/batchStatus")
    @RequiresMenuLevel(code = "system:menu", level = 2)
    public ApiResponse<Void> batchUpdateStatus(@RequestBody @Valid BatchMenuStatusReq req) {
        menuService.batchUpdateMenuStatus(req);
        return ApiResponse.success(null);
    }

    /**
     * 检查菜单编码是否存在
     */
    @GetMapping("/checkMenuCode")
    public ApiResponse<Boolean> checkMenuCode(@RequestParam String menuCode,
                                             @RequestParam(required = false) Long excludeMenuId) {
        boolean exists = menuService.checkMenuCodeExists(menuCode, excludeMenuId);
        return ApiResponse.success(exists);
    }

}