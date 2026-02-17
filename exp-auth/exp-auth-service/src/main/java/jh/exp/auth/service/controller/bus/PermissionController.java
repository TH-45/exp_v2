package jh.exp.auth.service.controller.bus;

import jh.exp.auth.core.entity.req.UpdateMenuTreePermissionReq;
import jh.exp.auth.service.service.bus.PermissionService;
import jh.exp.common.core.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 权限管理（菜单树权限保存等）
 */
@RestController
@RequestMapping("/permission")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * 更新菜单树权限：根据角色ID和选中的菜单ID列表，更新角色在 auth 模块下的菜单权限（exp_role_permission_rel）
     */
    @PostMapping("/update/menu/treePermission")
    public ApiResponse<Void> updateMenuTreePermission(@RequestBody @Valid UpdateMenuTreePermissionReq req) {
        try {
            permissionService.updateMenuTreePermission(req);
            return ApiResponse.success(null);
        } catch (Exception e) {
            return ApiResponse.fail(e.getMessage());
        }
    }
}
