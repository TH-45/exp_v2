package jh.exp.gateway.auth.service;

import com.exp.common.auth.dto.LoginUserInfo;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * 负责生成 JWT，密钥与 JwtAuthFilter 使用同一配置。
 */
@Service
public class JwtTokenService {

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    /**
     * 令牌有效期（单位：分钟），可根据需要调整。
     */
    @Value("${security.jwt.expire-minutes:120}")
    private long expireMinutes;

    public String generateToken(LoginUserInfo userInfo) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();
        Instant expireAt = now.plus(expireMinutes, ChronoUnit.MINUTES);

        return Jwts.builder()
                .setSubject(userInfo.getUserId())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expireAt))
                .claim("username", userInfo.getUsername())
                .claim("roles", userInfo.getRoles())
                .claim("permissions", userInfo.getPermissions())
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}

