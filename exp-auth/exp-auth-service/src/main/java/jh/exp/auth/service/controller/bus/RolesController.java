package jh.exp.auth.service.controller.bus;





import jh.exp.auth.core.entity.req.*;
import jh.exp.auth.core.entity.res.MenusRes;
import jh.exp.auth.core.entity.res.RoleAssignRes;
import jh.exp.auth.core.entity.res.RoleDetailRes;
import jh.exp.auth.core.entity.res.RoleListRes;
import jh.exp.auth.service.service.bus.MenuService;
import jh.exp.auth.service.service.bus.RoleAssignService;
import jh.exp.auth.service.service.bus.RoleService;
import jh.exp.common.core.annotation.RequiresMenuLevel;
import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.auth.CurrentUserHolder;
import jh.exp.common.core.auth.dto.CurrentUser;
import jh.exp.common.core.req.SimplePageReq;
import jh.exp.common.core.res.SimplePageRes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@RequiresMenuLevel(code = "system:role", level = 1)
public class RolesController {

    private final RoleService roleService;
    private final MenuService menuService;
    private final RoleAssignService roleAssignService;

    /**
     * 分页查询角色列表
     */
    @PostMapping("/list")
    public ApiResponse<SimplePageRes<RoleListRes>> list(@RequestBody SimplePageReq<QueryRoleReq> req) {
        req.pageDefault();
        SimplePageRes<RoleListRes> result = roleService.queryRoleList(req);
        return ApiResponse.success(result);
    }

    /**
     * 根据ID查询角色详情
     */
    @GetMapping("/detail")
    public ApiResponse<RoleDetailRes> detail(@RequestParam Long roleId) {
        RoleDetailRes result = roleService.getRoleById(roleId);
        return ApiResponse.success(result);
    }

    /**
     * 创建角色
     */
    @PostMapping("/create")
    @RequiresMenuLevel(code = "system:role", level = 2)
    public ApiResponse<RoleDetailRes> create(@RequestBody @Valid CreateRoleReq req) {
        RoleDetailRes result = roleService.createRole(req);
        return ApiResponse.success(result);
    }

    /**
     * 更新角色
     */
    @PostMapping("/update")
    @RequiresMenuLevel(code = "system:role", level = 2)
    public ApiResponse<RoleDetailRes> update(@RequestBody @Valid UpdateRoleReq req) {
        RoleDetailRes result = roleService.updateRole(req);
        return ApiResponse.success(result);
    }

    /**
     * 删除角色
     */
    @PostMapping("/delete")
    @RequiresMenuLevel(code = "system:role", level = 3)
    public ApiResponse<Void> delete(@RequestBody @Valid DeleteRoleReq req) {
        roleService.deleteRole(req.getRoleId());
        return ApiResponse.success(null);
    }

    /**
     * 批量删除角色
     */
    @PostMapping("/batchDelete")
    @RequiresMenuLevel(code = "system:role", level = 3)
    public ApiResponse<Void> batchDelete(@RequestBody @Valid BatchDeleteRoleReq req) {
        roleService.batchDeleteRoles(req);
        return ApiResponse.success(null);
    }

    /**
     * 更改角色状态
     */
    @PostMapping("/status")
    @RequiresMenuLevel(code = "system:role", level = 2)
    public ApiResponse<RoleDetailRes> updateStatus(@RequestBody @Valid RoleStatusReq req) {
        RoleDetailRes result = roleService.updateRoleStatus(req);
        return ApiResponse.success(result);
    }

    /**
     * 批量更改角色状态
     */
    @PostMapping("/batchStatus")
    @RequiresMenuLevel(code = "system:role", level = 2)
    public ApiResponse<Void> batchUpdateStatus(@RequestBody @Valid BatchRoleStatusReq req) {
        roleService.batchUpdateRoleStatus(req);
        return ApiResponse.success(null);
    }

    /**
     * 检查角色编码是否存在
     */
    @GetMapping("/checkRoleCode")
    public ApiResponse<Boolean> checkRoleCode(@RequestParam String roleCode,
                                             @RequestParam(required = false) Long excludeRoleId) {
        boolean exists = roleService.checkRoleCodeExists(roleCode, excludeRoleId);
        return ApiResponse.success(exists);
    }

    /**
     * 获取所有启用的角色
     */
    @GetMapping("/enabledList")
    public ApiResponse<List<RoleListRes>> getEnabledList() {
        List<RoleListRes> result = roleService.getAllEnabledRoles();
        return ApiResponse.success(result);
    }

    /**
     * 查询角色授权列表（按主体类型分组）
     */
    @GetMapping("/assigns")
    @RequiresMenuLevel(code = "system:role", level = 2)
    public ApiResponse<RoleAssignRes> listAssigns(@RequestParam Long roleId) {
        RoleAssignRes result = roleAssignService.listByRoleId(roleId);
        return ApiResponse.success(result);
    }

    /**
     * 保存角色授权（替换该角色下所有授权）
     */
    @PostMapping("/assigns/save")
    @RequiresMenuLevel(code = "system:role", level = 2)
    public ApiResponse<Void> saveAssigns(@RequestBody @Valid RoleAssignSaveReq req) {
        roleAssignService.save(req);
        return ApiResponse.success(null);
    }

    /**
     * 获取当前用户的菜单权限（原有接口）
     */
    @GetMapping("/menus")
    public ApiResponse<MenusRes> getMenus() {
        CurrentUser currentUser = CurrentUserHolder.get();
        if (currentUser == null) {
            return ApiResponse.fail("401", "用户未登录");
        }

        try {
            MenusRes menusRes = menuService.getMenus(currentUser);
            return ApiResponse.success(menusRes);
        } catch (Exception e) {
            return ApiResponse.fail("500", e.getMessage());
        }
    }

}
