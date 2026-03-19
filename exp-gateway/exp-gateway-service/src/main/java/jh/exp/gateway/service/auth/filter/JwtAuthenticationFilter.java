package jh.exp.gateway.service.auth.filter;

import jh.exp.common.core.api.ApiResponse;
import jh.exp.common.core.auth.dto.PermissionProfileResult;
import jh.exp.common.core.constant.ServiceContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jh.exp.gateway.auth.client.AuthInternalHttpClient;
import jh.exp.gateway.service.auth.service.JwtTokenService;
import jh.exp.gateway.service.auth.service.TokenBlacklistService;
import jh.exp.gateway.service.auth.support.JwtPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    /**
     * 白名单路径集合，这些路径不需要进行JWT认证即可访问
     */
    private static final Set<String> WHITE_LIST = Set.of(
            "/exp/auth/login",
            "/exp/zz/**",
            "/actuator/health",
            "/actuator/info",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/doc.html"
    );

    /**
     * 路径匹配器，用于匹配请求路径是否在白名单中（支持通配符匹配）
     */
    private final AntPathMatcher matcher = new AntPathMatcher();
    
    /**
     * JWT Token服务，用于解析和验证JWT Token
     */
    private final JwtTokenService jwtTokenService;
    
    /**
     * Token黑名单服务，用于检查Token是否已被加入黑名单（如用户登出后的Token）
     */
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * 认证服务内部客户端，用于获取权限画像（lite snapshot）
     */
    private final AuthInternalHttpClient authInternalClient;
    
    /**
     * JSON序列化/反序列化工具，用于将响应对象转换为JSON格式
     */
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService,
                                   TokenBlacklistService tokenBlacklistService,
                                   AuthInternalHttpClient authInternalClient,
                                   ObjectMapper objectMapper) {
        this.jwtTokenService = jwtTokenService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.authInternalClient = authInternalClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (skipAuth(request)) {
            log.debug("JWT 鉴权跳过白名单路径，path={}", path);
            return chain.filter(exchange);
        }

        String token = jwtTokenService.resolveToken(request.getHeaders());
        if (!StringUtils.hasText(token)) {
            log.warn("请求未携带 Token，被拒绝，path={}", path);
            return reject(exchange, "AUTH_UNAUTHORIZED", "请先登录");
        }

        JwtPayload payload;
        try {
            payload = jwtTokenService.parseToken(token);
        } catch (Exception ex) {
            log.warn("Token 解析失败，被拒绝，path={}，error={}", path, ex.getMessage());
            return reject(exchange, "AUTH_INVALID_TOKEN", "登录状态已失效");
        }

        JwtPayload finalPayload = payload;
        return tokenBlacklistService.isBlacklisted(payload.tokenId())
                .flatMap(isBlack -> {
                    if (Boolean.TRUE.equals(isBlack)) {
                        log.warn("Token 已在黑名单中，被拒绝，tokenId={}，path={}", finalPayload.tokenId(), path);
                        return reject(exchange, "AUTH_INVALID_TOKEN", "Token 已失效");
                    }
                    return authInternalClient.permissionProfileLite(finalPayload.userId())
                            .flatMap(profile -> {
                                // 客户端版本与快照版本不一致时，视为权限已变更，要求前端刷新后重试
                                String clientVersion = request.getHeaders().getFirst(ServiceContext.PERMISSION_VERSION_HEADER);
                                if (clientVersion != null && !clientVersion.isBlank()) {
                                    Long serverVersion = profile.getPermissionVersion();
                                    if (serverVersion != null) {
                                        try {
                                            long cv = Long.parseLong(clientVersion.trim());
                                            if (cv != serverVersion.longValue()) {
                                                log.info("权限版本不一致，要求刷新，userId={}，client={}，server={}，path={}",
                                                        finalPayload.userId(), cv, serverVersion, path);
                                                return reject(exchange, "AUTH_PERMISSION_CHANGED", "权限已变更，请刷新后重试");
                                            }
                                        } catch (NumberFormatException ignored) {}
                                    }
                                }
                                ServerHttpRequest mutated = buildHeadersWithSnapshot(request, finalPayload, profile);
                                log.info("JWT 鉴权通过，userId={}，path={}", finalPayload.userId(), path);
                                return chain.filter(exchange.mutate().request(mutated).build());
                            })
                            .onErrorResume(ex -> {
                                log.warn("获取权限画像失败，拒绝请求（fail-closed），userId={}，path={}，error={}",
                                        finalPayload.userId(), path, ex.getMessage());
                                return reject(exchange, "AUTH_PERMISSION_CHANGED", "权限服务暂不可用，请稍后重试");
                            });
                });
    }

    @SuppressWarnings("null")
    private boolean skipAuth(ServerHttpRequest request) {
        if (HttpMethod.OPTIONS.equals(request.getMethod())) {
            return true;
        }
        String path = request.getURI().getPath();
        return WHITE_LIST.stream().anyMatch(pattern -> matcher.match(pattern, path));
    }

    @SuppressWarnings("null")
    private Mono<Void> reject(ServerWebExchange exchange, String code, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.setStatusCode(org.springframework.http.HttpStatus.OK);
        ApiResponse<Void> body = ApiResponse.fail(code, message);
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            bytes = ("{\"success\":false,\"code\":\"" + code + "\",\"message\":\"" + message + "\",\"data\":null}")
                    .getBytes(StandardCharsets.UTF_8);
        }
        DataBufferFactory bufferFactory = response.bufferFactory();
        DataBuffer dataBuffer = bufferFactory.wrap(bytes);
        return response.writeWith(Mono.just(dataBuffer));
    }

    private ServerHttpRequest buildHeadersWithSnapshot(ServerHttpRequest req, JwtPayload payload, PermissionProfileResult profile) {
        var builder = req.mutate()
                .header(ServiceContext.USER_ID_HEADER, payload.userId())
                .header(ServiceContext.USER_NAME_HEADER, payload.username())
                .header(ServiceContext.ROLES_HEADER, String.join(",", profile.getRoles() != null ? profile.getRoles() : List.of()))
                .header(ServiceContext.USER_ROLES_HEADER, String.join(",", profile.getRoles() != null ? profile.getRoles() : List.of()));
        List<String> funcPerms = profile.getFuncPermissionSet() != null ? profile.getFuncPermissionSet() : List.of();
        builder.header(ServiceContext.PERMISSIONS_HEADER, String.join(",", funcPerms))
                .header(ServiceContext.USER_PERMISSIONS_HEADER, String.join(",", funcPerms));
        if (profile.getPermissionVersion() != null) {
            builder.header(ServiceContext.PERMISSION_VERSION_HEADER, String.valueOf(profile.getPermissionVersion()));
        }
        if (profile.getMenuLevelMap() != null && !profile.getMenuLevelMap().isEmpty()) {
            try {
                builder.header(ServiceContext.MENU_LEVEL_MAP_HEADER, objectMapper.writeValueAsString(profile.getMenuLevelMap()));
            } catch (JsonProcessingException ignored) {}
        }
        if (profile.getFuncPermissionSet() != null && !profile.getFuncPermissionSet().isEmpty()) {
            try {
                builder.header(ServiceContext.FUNC_PERMISSIONS_HEADER, objectMapper.writeValueAsString(profile.getFuncPermissionSet()));
            } catch (JsonProcessingException ignored) {}
        }
        if (profile.getDataScopeSummary() != null) {
            try {
                builder.header(ServiceContext.DATA_SCOPE_HEADER, objectMapper.writeValueAsString(profile.getDataScopeSummary()));
            } catch (JsonProcessingException ignored) {}
        }
        return builder.build();
    }

    private ServerHttpRequest buildHeadersWithoutSnapshot(ServerHttpRequest req, JwtPayload payload) {
        return req.mutate()
                .header(ServiceContext.USER_ID_HEADER, payload.userId())
                .header(ServiceContext.USER_NAME_HEADER, payload.username())
                .header(ServiceContext.ROLES_HEADER, String.join(",", payload.roles()))
                .header(ServiceContext.PERMISSIONS_HEADER, String.join(",", payload.permissions()))
                .header(ServiceContext.USER_ROLES_HEADER, String.join(",", payload.roles()))
                .header(ServiceContext.USER_PERMISSIONS_HEADER, String.join(",", payload.permissions()))
                .build();
    }

    @Override
    public int getOrder() {
        return -100;
    }
}

