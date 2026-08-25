package com.health.platform.core.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "health.jwt")
public class JwtProperties {

    /** HS256 签名密钥，必须通过环境变量 JWT_SECRET 注入，禁止硬编码；此处默认值仅供本地开发 */
    private String secret = "${JWT_SECRET:dev-only-jwt-secret-change-me-please-0123456789}";

    private long accessTtlSeconds = 7200;

    private long refreshTtlSeconds = 604800;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getAccessTtlSeconds() {
        return accessTtlSeconds;
    }

    public void setAccessTtlSeconds(long accessTtlSeconds) {
        this.accessTtlSeconds = accessTtlSeconds;
    }

    public long getRefreshTtlSeconds() {
        return refreshTtlSeconds;
    }

    public void setRefreshTtlSeconds(long refreshTtlSeconds) {
        this.refreshTtlSeconds = refreshTtlSeconds;
    }
}
