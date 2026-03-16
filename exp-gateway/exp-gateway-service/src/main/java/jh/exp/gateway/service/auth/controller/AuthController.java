package jh.exp.gateway.service.auth.controller;

import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.auth.dto.LoginRequest;
import jh.exp.common.core.auth.dto.LoginResult;
import jh.exp.common.core.auth.dto.LoginUserInfo;
import jh.exp.common.core.auth.dto.ProfileDetailResult;
import jh.exp.common.core.auth.dto.ProfileResult;
import jh.exp.gateway.auth.client.AuthInternalHttpClient;
import jh.exp.gateway.service.auth.service.JwtTokenService;
import jh.exp.gateway.service.auth.service.TokenBlacklistService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
@RequestMapping({"/exp/auth", "/api/exp/auth"})
public class AuthController {

    /**
     * 认证服务内部客户端（Reactive HTTP Interface），用于调用下游认证服务进行用户登录和获取用户信息
     */
    private final AuthInternalHttpClient authInternalClient;

    /**
     * JWT Token服务，用于生成和解析JWT Token
     */
    private final JwtTokenService jwtTokenService;

    /**
     * Token黑名单服务，用于管理已失效的Token（如用户登出后的Token）
     */
    private final TokenBlacklistService tokenBlacklistService;

    public AuthController(AuthInternalHttpClient authInternalClient,
                          JwtTokenService jwtTokenService,
                          TokenBlacklistService tokenBlacklistService) {
        this.authInternalClient = authInternalClient;
        this.jwtTokenService = jwtTokenService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @PostMapping("/login")
    public Mono<ApiResponse<LoginResult>> login(@Valid @RequestBody LoginRequest request) {
        return authInternalClient.login(request)
                .map(this::buildLoginResult)
                .onErrorResume(ex -> Mono.just(ApiResponse.fail("AUTH_LOGIN_FAILED", messageOrDefault(ex, "登录失败"))));
    }

    private ApiResponse<LoginResult> buildLoginResult(LoginUserInfo userInfo) {
        LoginResult result = jwtTokenService.buildLoginResult(userInfo);
        return ApiResponse.success("登录成功", result);
    }

    @PostMapping("/logout")
    public Mono<ApiResponse<Object>> logout(ServerHttpRequest request) {
        String token = jwtTokenService.resolveToken(request.getHeaders());
        if (!StringUtils.hasText(token)) {
            return Mono.just(ApiResponse.success("已退出登录", null));
        }
        return Mono.fromCallable(() -> jwtTokenService.parseToken(token))
                .flatMap(payload -> {
                    Duration ttl = payload.remainingDuration();
                    return tokenBlacklistService.add(payload.tokenId(), ttl)
                            .thenReturn(ApiResponse.success("退出成功", null));
                })
                .onErrorResume(ex -> Mono.just(ApiResponse.fail("AUTH_INVALID_TOKEN", messageOrDefault(ex, "Token 无效或已过期"))));
    }

    @GetMapping("/profile")
    public Mono<ApiResponse<ProfileResult>> profile(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        String token = jwtTokenService.resolveToken(headers);
        if (!StringUtils.hasText(token)) {
            return Mono.just(ApiResponse.fail("AUTH_UNAUTHORIZED", "请先登录"));
        }
        return Mono.fromCallable(() -> jwtTokenService.parseToken(token))
                .flatMap(payload -> authInternalClient.profile(payload.userId())
                        .map(ApiResponse::success))
                .onErrorResume(ex -> Mono.just(ApiResponse.fail("AUTH_INVALID_TOKEN", messageOrDefault(ex, "登录状态失效"))));
    }

    @GetMapping("/profile/detail")
    public Mono<ApiResponse<ProfileDetailResult>> profileDetail(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        String token = jwtTokenService.resolveToken(headers);
        if (!StringUtils.hasText(token)) {
            return Mono.just(ApiResponse.fail("AUTH_UNAUTHORIZED", "请先登录"));
        }
        return Mono.fromCallable(() -> jwtTokenService.parseToken(token))
                .flatMap(payload -> authInternalClient.profileDetail(payload.userId())
                        .map(ApiResponse::success))
                .onErrorResume(ex -> Mono.just(ApiResponse.fail("AUTH_INVALID_TOKEN", messageOrDefault(ex, "登录状态失效"))));
    }

    private String messageOrDefault(Throwable ex, String defaultMessage) {
        String msg = ex == null ? null : ex.getMessage();
        return StringUtils.hasText(msg) ? msg : defaultMessage;
    }
}
