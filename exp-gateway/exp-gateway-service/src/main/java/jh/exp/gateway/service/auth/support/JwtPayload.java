package jh.exp.gateway.service.auth.support;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * @param tokenId     Token的唯一标识ID，用于Token黑名单管理
 * @param userId      用户ID
 * @param username    用户名
 * @param roles       用户角色列表
 * @param permissions 用户权限列表
 * @param issuedAt    Token签发时间
 * @param expiresAt   Token过期时间
 */
public record JwtPayload(String tokenId, String userId, String username, List<String> roles, List<String> permissions,
                         Instant issuedAt, Instant expiresAt) {

    public JwtPayload(String tokenId,
                      String userId,
                      String username,
                      List<String> roles,
                      List<String> permissions,
                      Instant issuedAt,
                      Instant expiresAt) {
        this.tokenId = tokenId;
        this.userId = userId;
        this.username = username;
        this.roles = roles == null ? Collections.emptyList() : Collections.unmodifiableList(roles);
        this.permissions = permissions == null ? Collections.emptyList() : Collections.unmodifiableList(permissions);
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }

    public Duration remainingDuration() {
        if (expiresAt == null) {
            return Duration.ZERO;
        }
        Instant now = Instant.now();
        if (expiresAt.isBefore(now)) {
            return Duration.ZERO;
        }
        return Duration.between(now, expiresAt);
    }
}

