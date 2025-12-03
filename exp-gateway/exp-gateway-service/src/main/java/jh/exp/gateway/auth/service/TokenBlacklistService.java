package jh.exp.gateway.auth.service;

import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class TokenBlacklistService {

    /**
     * Redis中Token黑名单的键前缀，用于存储已失效的Token ID
     */
    private static final String KEY_PREFIX = "exp:gateway:blacklist:";

    /**
     * 响应式Redis模板，用于异步操作Redis存储Token黑名单
     */
    private final ReactiveStringRedisTemplate redisTemplate;

    public TokenBlacklistService(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Mono<Boolean> add(String tokenId, Duration ttl) {
        if (!StringUtils.hasText(tokenId)) {
            return Mono.just(Boolean.TRUE);
        }
        Duration validTtl = (ttl == null || ttl.isNegative() || ttl.isZero())
                ? Duration.ofSeconds(5)
                : ttl;
        return redisTemplate.opsForValue()
                .set(KEY_PREFIX + tokenId, "1", validTtl);
    }

    public Mono<Boolean> isBlacklisted(String tokenId) {
        if (!StringUtils.hasText(tokenId)) {
            return Mono.just(Boolean.TRUE);
        }
        return redisTemplate.hasKey(KEY_PREFIX + tokenId)
                .defaultIfEmpty(false);
    }
}

