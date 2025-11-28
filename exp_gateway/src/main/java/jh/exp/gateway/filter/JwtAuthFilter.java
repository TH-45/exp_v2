package jh.exp.gateway.filter;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @Value("${security.jwt.header:Authorization}")
    private String headerName;

    @Value("${security.jwt.prefix:Bearer }")
    private String tokenPrefix;

    /**
     * 需要放行的路径前缀（登录、验证码等）
     */
    @Value("${security.jwt.ignore-paths:/rest/system/auth/login}")
    private String ignorePathsProp;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // 预检请求直接放行（CORS）
        if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod())) {
            return chain.filter(exchange);
        }

        // 登录等白名单接口直接放行
        List<String> ignorePaths = Arrays.asList(ignorePathsProp.split(","));
        for (String ignore : ignorePaths) {
            String trimmed = ignore.trim();
            if (!trimmed.isEmpty() && path.startsWith(trimmed)) {
                return chain.filter(exchange);
            }
        }

        // 从 Header 中获取 JWT
        String authHeader = exchange.getRequest().getHeaders().getFirst(headerName);
        if (authHeader == null || !authHeader.startsWith(tokenPrefix)) {
            return this.unauthorized(exchange, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(tokenPrefix.length());

        try {
            // 仅做签名校验和基本有效性校验，后续鉴权交给各业务服务处理
            Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token);
        } catch (JwtException ex) {
            return this.unauthorized(exchange, "Invalid token: " + ex.getMessage());
        }

        // 校验通过，继续后续过滤器链
        return chain.filter(exchange);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        String body = "{\"code\":401,\"message\":\"" + message + "\"}";
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
                .bufferFactory()
                .wrap(bytes)));
    }

    @Override
    public int getOrder() {
        // 在大多数业务过滤器之前执行
        return -100;
    }
}


