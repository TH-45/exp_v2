package jh.exp.gateway.service.auth.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * 网关访问日志过滤器：
 * - 记录每个请求的入口和完成情况（方法、URI、状态码、耗时、用户信息等）；
 * - 与 JwtAuthenticationFilter 协同工作，尽量在用户信息就绪后记录日志。
 */
@Component
public class AccessLogFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger("jh.exp.gateway.AccessLog");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String method = request.getMethod() != null ? request.getMethod().name() : "UNKNOWN";
        String uri = request.getURI().getRawPath();
        String query = request.getURI().getRawQuery();
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        String clientIp = remoteAddress != null ? remoteAddress.getAddress().getHostAddress() : "unknown";

        // 简单生成一个 traceId，可替换为更复杂的链路追踪方案
        String traceId = UUID.randomUUID().toString().replace("-", "");

        // 从 Header 中获取用户信息（由 JwtAuthenticationFilter 透传）
        String userId = request.getHeaders().getFirst("X-User-Id");
        String userName = request.getHeaders().getFirst("X-User-Name");

        // 记录请求进入日志（可按需关闭）
        log.info("[ACCESS][REQUEST] traceId={} method={} uri={} query={} ip={} userId={} userName={}",
                traceId, method, uri, query, clientIp, userId, userName);

        Instant start = Instant.now();

        return chain.filter(exchange)
                .doOnSuccess(aVoid -> logResponse(exchange, traceId, method, uri, userId, userName, start))
                .doOnError(throwable -> logResponse(exchange, traceId, method, uri, userId, userName, start));
    }

    private void logResponse(ServerWebExchange exchange,
                             String traceId,
                             String method,
                             String uri,
                             String userId,
                             String userName,
                             Instant start) {
        ServerHttpResponse response = exchange.getResponse();
        int status = response.getStatusCode() != null ? response.getStatusCode().value() : 0;
        long costMs = Duration.between(start, Instant.now()).toMillis();

        log.info("[ACCESS][RESPONSE] traceId={} method={} uri={} status={} cost={}ms userId={} userName={}",
                traceId, method, uri, status, costMs, userId, userName);
    }

    @Override
    public int getOrder() {
        // 放在 JwtAuthenticationFilter 之后，尽量在用户信息就绪后记录
        return -90;
    }
}


