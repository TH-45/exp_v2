package jh.exp.auth.service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jh.exp.common.core.auth.dto.PermissionProfileResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 权限快照服务：读写 Redis 缓存，管理 version。
 * <p>
 * 设计方案：权限快照 + 版本号 + 缓存。
 */
@Service
public class PermissionSnapshotService {

    private static final Logger log = LoggerFactory.getLogger(PermissionSnapshotService.class);
    private static final String KEY_PREFIX = "auth:perm:snapshot:";

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    @Value("${auth.permission.snapshot.ttl-minutes:30}")
    private int ttlMinutes;

    /**
     * 从缓存获取 full snapshot。
     */
    public PermissionProfileResult getFromCache(Long userId) {
        if (userId == null) return null;
        String key = KEY_PREFIX + userId;
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, PermissionProfileResult.class);
        } catch (Exception e) {
            log.warn("解析权限快照失败，userId={}", userId, e);
            redisTemplate.delete(key);
            return null;
        }
    }

    /**
     * 写入 full snapshot 到缓存。
     */
    public void putToCache(PermissionProfileResult snapshot) {
        if (snapshot == null || snapshot.getUserId() == null) return;
        String key = KEY_PREFIX + snapshot.getUserId();
        try {
            String json = objectMapper.writeValueAsString(snapshot);
            redisTemplate.opsForValue().set(key, json, ttlMinutes, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.warn("序列化权限快照失败，userId={}", snapshot.getUserId(), e);
        }
    }

    /**
     * 使指定用户快照失效。
     */
    public void invalidate(Long userId) {
        if (userId == null) return;
        redisTemplate.delete(KEY_PREFIX + userId);
    }

    /**
     * 批量使快照失效。
     */
    public void invalidateBatch(java.util.Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return;
        var keys = userIds.stream().map(id -> KEY_PREFIX + id).toList();
        redisTemplate.delete(keys);
    }
}
