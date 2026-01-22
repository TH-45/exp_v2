package jh.exp.gateway.service.auth.service;

import jh.exp.common.core.auth.dto.LoginResult;
import jh.exp.common.core.auth.dto.LoginUserInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import jh.exp.gateway.service.auth.support.JwtPayload;
import jh.exp.gateway.service.config.JwtProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class JwtTokenService {

    /**
     * Bearer Token前缀，用于从Authorization头中提取Token
     */
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * JWT配置属性，包含密钥和过期时间等配置信息
     */
    private final JwtProperties jwtProperties;
    
    /**
     * JWT签名密钥，用于生成和验证Token的签名
     */
    private final Key signingKey;
    
    /**
     * JWT解析器，用于解析和验证JWT Token
     */
    private final JwtParser jwtParser;

    public JwtTokenService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.signingKey = initSigningKey(jwtProperties.getSecret());
        this.jwtParser = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build();
    }

    public LoginResult buildLoginResult(LoginUserInfo userInfo) {
        String token = generateToken(userInfo);
        LoginResult result = new LoginResult();
        result.setToken(token);
        result.setUserId(userInfo.getUserId());
        result.setUsername(userInfo.getUsername());
        result.setRoles(userInfo.getRoles() == null ? List.of() : userInfo.getRoles());
        result.setPermissions(userInfo.getPermissions() == null ? List.of() : userInfo.getPermissions());
        return result;
    }

    public String generateToken(LoginUserInfo userInfo) {
        Instant now = Instant.now();
        Instant expireAt = now.plus(jwtProperties.getExpireMinutes(), ChronoUnit.MINUTES);
        List<String> roles = userInfo.getRoles() == null ? List.of() : userInfo.getRoles();
        List<String> permissions = userInfo.getPermissions() == null ? List.of() : userInfo.getPermissions();
        Map<String, Object> claims = Map.of(
                "uid", userInfo.getUserId(),
                "uname", userInfo.getUsername(),
                "roles", roles,
                "perms", permissions
        );
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(userInfo.getUsername())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expireAt))
                .addClaims(claims)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public JwtPayload parseToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new IllegalArgumentException("Token 不存在");
        }
        Jws<Claims> jws = jwtParser.parseClaimsJws(token);
        Claims body = jws.getBody();

        List<String> roles = asStringList(body.get("roles"));
        List<String> permissions = asStringList(body.get("perms"));

        return new JwtPayload(
                body.getId(),
                body.get("uid", String.class),
                body.get("uname", String.class),
                roles,
                permissions,
                body.getIssuedAt() == null ? null : body.getIssuedAt().toInstant(),
                body.getExpiration() == null ? null : body.getExpiration().toInstant()
        );
    }

    public String resolveToken(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader)) {
            return null;
        }
        if (authorizationHeader.startsWith(BEARER_PREFIX)) {
            return authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        }
        return authorizationHeader;
    }

    public String resolveToken(HttpHeaders headers) {
        return resolveToken(headers.getFirst(HttpHeaders.AUTHORIZATION));
    }

    public long getExpireMinutes() {
        return jwtProperties.getExpireMinutes();
    }

    private List<String> asStringList(Object value) {
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).collect(Collectors.toList());
        }
        return List.of();
    }

    private Key initSigningKey(String rawSecret) {
        if (!StringUtils.hasText(rawSecret)) {
            throw new IllegalArgumentException("JWT 密钥不能为空");
        }
        byte[] secretBytes = rawSecret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            secretBytes = sha256(secretBytes);
        }
        try {
            return Keys.hmacShaKeyFor(secretBytes);
        } catch (WeakKeyException ex) {
            byte[] stronger = sha256(secretBytes);
            return Keys.hmacShaKeyFor(stronger);
        }
    }

    private byte[] sha256(byte[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("无法初始化 SHA-256", e);
        }
    }
}

