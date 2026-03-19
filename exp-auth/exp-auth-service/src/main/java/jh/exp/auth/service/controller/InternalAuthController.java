package jh.exp.auth.service.controller;

import jh.exp.auth.core.entity.req.PermissionRebuildNotifyReq;
import jh.exp.auth.service.service.LoginAuthService;
import jh.exp.auth.service.service.PermissionRebuildService;
import jh.exp.auth.service.service.UserPermissionProfileService;
import jh.exp.common.core.auth.dto.LoginRequest;
import jh.exp.common.core.auth.dto.LoginUserInfo;
import jh.exp.common.core.auth.dto.PermissionProfileResult;
import jh.exp.common.core.auth.dto.ProfileDetailResult;
import jh.exp.common.core.auth.dto.ProfileResult;
import jh.exp.auth.service.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * 仅供网关调用的内部登录校验接口，不直接对前端暴露。
 */
@RestController
@RequestMapping("/internal/auth")
public class InternalAuthController {
    @Autowired
    private LoginAuthService loginAuthService;
    @Autowired
    private ProfileService profileService;
    @Autowired
    private UserPermissionProfileService userPermissionProfileService;
    @Autowired
    private PermissionRebuildService permissionRebuildService;

    @PostMapping("/login")
    public LoginUserInfo login(@Valid @RequestBody LoginRequest request) {
        return loginAuthService.login(request);
    }

    @GetMapping("/profile")
    public ProfileResult profile(@RequestParam("userId") String userId) {
        return profileService.getProfile(userId);
    }

    @GetMapping("/profile/detail")
    public ProfileDetailResult profileDetail(@RequestParam("userId") String userId) {
        return profileService.getProfileDetail(userId);
    }

    /**
     * 跨服务权限重建通知：其他服务在主体关系变化时调用，触发受影响用户快照失效。
     */
    @PostMapping("/permission/rebuild/notify")
    public void permissionRebuildNotify(@Valid @RequestBody PermissionRebuildNotifyReq req) {
        String eventType = req.getEventType();
        String principalType = req.getPrincipalType();
        var principalIds = req.getPrincipalIds() != null ? req.getPrincipalIds() : List.<Long>of();
        if (principalIds.isEmpty()) return;
        switch (eventType) {
            case "PERSON_ORG_CHANGED" -> {
                if ("PERSON".equals(principalType)) {
                    permissionRebuildService.onPersonOrgChanged(principalIds);
                }
            }
            case "POST_CHANGED", "ORG_CHANGED" -> {
                if ("POST".equals(principalType) || "ORG".equals(principalType)) {
                    permissionRebuildService.onOrgOrPostChanged(principalType, principalIds);
                }
            }
            default -> {
                // 未知事件类型，按 PERSON/POST/ORG 尝试处理
                if ("PERSON".equals(principalType)) {
                    permissionRebuildService.onPersonOrgChanged(principalIds);
                } else if ("POST".equals(principalType) || "ORG".equals(principalType)) {
                    permissionRebuildService.onOrgOrPostChanged(principalType, principalIds);
                }
            }
        }
    }

    /**
     * 权限画像接口：返回完整权限快照（full snapshot，含 menuTree），供前端使用。
     */
    @GetMapping("/permission/profile")
    public PermissionProfileResult permissionProfile(@RequestParam("userId") String userId) {
        Long accountId = Long.parseLong(userId);
        PermissionProfileResult result = userPermissionProfileService.buildFullSnapshot(accountId);
        if (result == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return result;
    }

    /**
     * 轻量权限画像接口：返回 lite snapshot（不含 menuTree），供网关请求鉴权使用。
     */
    @GetMapping("/permission/profile/lite")
    public PermissionProfileResult permissionProfileLite(@RequestParam("userId") String userId) {
        Long accountId = Long.parseLong(userId);
        PermissionProfileResult result = userPermissionProfileService.buildLiteSnapshot(accountId);
        if (result == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return result;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleForbidden(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }
}


