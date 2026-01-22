package jh.exp.gateway.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "exp.gateway.jwt")
public class JwtProperties {

    /**
     * JWT 对称加密秘钥
     */
    private String secret;

    /**
     * 过期时间（分钟）
     */
    private long expireMinutes = 120;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpireMinutes() {
        return expireMinutes;
    }

    public void setExpireMinutes(long expireMinutes) {
        this.expireMinutes = expireMinutes;
    }
}

