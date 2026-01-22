package jh.exp.auth.service.controller.bus;





import jh.exp.auth.core.entity.req.*;
import jh.exp.auth.core.entity.res.MenuDetailRes;
import jh.exp.auth.core.entity.res.MenuListRes;
import jh.exp.auth.core.entity.res.MenuTreeRes;
import jh.exp.auth.service.service.bus.MenuService;
import jh.exp.common.api.ApiResponse;
import jh.exp.common.req.SimplePageReq;
import jh.exp.common.res.SimplePageRes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理控制器
 */
@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
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
    public ApiResponse<MenuDetailRes> create(@RequestBody @Valid CreateMenuReq req) {
        MenuDetailRes result = menuService.createMenu(req);
        return ApiResponse.success(result);
    }

    /**
     * 更新菜单
     */
    @PostMapping("/update")
    public ApiResponse<MenuDetailRes> update(@RequestBody @Valid UpdateMenuReq req) {
        MenuDetailRes result = menuService.updateMenu(req);
        return ApiResponse.success(result);
    }

    /**
     * 删除菜单
     */
    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody @Valid DeleteMenuReq req) {
        menuService.deleteMenu(req.getMenuId());
        return ApiResponse.success(null);
    }

    /**
     * 批量删除菜单
     */
    @PostMapping("/batchDelete")
    public ApiResponse<Void> batchDelete(@RequestBody @Valid BatchDeleteMenuReq req) {
        menuService.batchDeleteMenus(req);
        return ApiResponse.success(null);
    }

    /**
     * 更改菜单状态
     */
    @PostMapping("/status")
    public ApiResponse<MenuDetailRes> updateStatus(@RequestBody @Valid MenuStatusReq req) {
        MenuDetailRes result = menuService.updateMenuStatus(req);
        return ApiResponse.success(result);
    }

    /**
     * 批量更改菜单状态
     */
    @PostMapping("/batchStatus")
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