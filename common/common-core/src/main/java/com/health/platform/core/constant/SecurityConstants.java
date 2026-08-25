package com.health.platform.core.constant;

public final class SecurityConstants {

    private SecurityConstants() {
    }

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    /**
     * Gateway 校验 JWT 后透传给下游服务的用户身份头。
     * Gateway 必须剥离外部请求携带的同名头，防止身份伪造。
     */
    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USERNAME_HEADER = "X-Username";

    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_USERNAME = "username";
}
