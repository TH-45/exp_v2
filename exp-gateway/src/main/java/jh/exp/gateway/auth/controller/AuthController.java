package jh.exp.gateway.auth.controller;

import com.exp.common.api.ApiResponse;
import com.exp.common.auth.client.AuthInternalClient;
import com.exp.common.auth.dto.LoginRequest;
import com.exp.common.auth.dto.LoginResult;
import com.exp.common.auth.dto.LoginUserInfo;
import jakarta.validation.Valid;
import jh.exp.gateway.auth.service.JwtTokenService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 网关对前端暴露的登录接口：
 *
 * POST /exp/auth/login
 *   -> 调用 auth 服务内部接口校验账号密码
 *   -> 生成 JWT
 *   -> 按照 ApiResponse<LoginResult> 结构返回给前端
 */
@RestController
@RequestMapping("/exp/auth")
public class AuthController {

    private final AuthInternalClient authInternalClient;
    private final JwtTokenService jwtTokenService;

    public AuthController(AuthInternalClient authInternalClient, JwtTokenService jwtTokenService) {
        this.authInternalClient = authInternalClient;
        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResult> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginUserInfo userInfo = authInternalClient.login(request);
            return buildSuccessResponse(userInfo);
        } catch (Exception ex) {
            // 这里统一返回业务失败结构，后续可以根据 FeignException 的状态码区分 401/403 等
            return ApiResponse.fail("AUTH_INVALID_CREDENTIALS", "账号或密码错误");
        }
    }

    private ApiResponse<LoginResult> buildSuccessResponse(LoginUserInfo userInfo) {
        String token = jwtTokenService.generateToken(userInfo);

        LoginResult result = new LoginResult();
        result.setToken(token);
        result.setUserId(userInfo.getUserId());
        result.setUsername(userInfo.getUsername());
        result.setRoles(userInfo.getRoles());
        result.setPermissions(userInfo.getPermissions());

        return ApiResponse.success(result);
    }
}

