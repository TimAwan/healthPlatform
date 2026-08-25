package com.health.platform.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.health.platform.core.constant.RedisKeyConstants;
import com.health.platform.core.constant.SecurityConstants;
import com.health.platform.core.exception.AuthenticationException;
import com.health.platform.core.jwt.JwtPayload;
import com.health.platform.core.jwt.JwtUtils;
import com.health.platform.core.result.ErrorCode;
import com.health.platform.core.result.Result;
import com.health.platform.gateway.GatewaySecurityProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * Gateway 统一 JWT 校验：
 * 1. 无条件剥离外部伪造的 X-User-* 身份头；
 * 2. 白名单直接放行；
 * 3. 校验 Bearer Token 签名与有效期；
 * 4. 校验 Redis 黑名单（注销）；
 * 5. 将用户身份写入请求头透传下游。
 */
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final JwtUtils jwtUtils;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final GatewaySecurityProperties properties;

    public AuthGlobalFilter(JwtUtils jwtUtils,
                            ReactiveStringRedisTemplate redisTemplate,
                            GatewaySecurityProperties properties) {
        this.jwtUtils = jwtUtils;
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerWebExchange stripped = exchange.mutate()
                .request(builder -> {
                    builder.headers(headers -> {
                        headers.remove(SecurityConstants.USER_ID_HEADER);
                        headers.remove(SecurityConstants.USERNAME_HEADER);
                    });
                })
                .build();

        String path = stripped.getRequest().getPath().value();
        if (isWhitelisted(path)) {
            return chain.filter(stripped);
        }

        String authorization = stripped.getRequest().getHeaders().getFirst(SecurityConstants.AUTHORIZATION_HEADER);
        if (!StringUtils.hasText(authorization) || !authorization.startsWith(SecurityConstants.BEARER_PREFIX)) {
            return unauthorized(stripped, com.health.platform.core.result.CommonErrorCode.UNAUTHORIZED);
        }

        JwtPayload payload;
        try {
            payload = jwtUtils.parseAccessToken(authorization.substring(SecurityConstants.BEARER_PREFIX.length()));
        } catch (AuthenticationException e) {
            return unauthorized(stripped, e.getCode(), e.getMessage());
        }

        String blacklistKey = RedisKeyConstants.TOKEN_BLACKLIST + payload.jti();
        return redisTemplate.hasKey(blacklistKey)
                .flatMap(blacklisted -> {
                    if (Boolean.TRUE.equals(blacklisted)) {
                        return unauthorized(stripped, com.health.platform.core.result.CommonErrorCode.TOKEN_INVALID);
                    }
                    ServerWebExchange withUser = stripped.mutate()
                            .request(builder -> builder
                                    .header(SecurityConstants.USER_ID_HEADER, String.valueOf(payload.userId()))
                                    .header(SecurityConstants.USERNAME_HEADER, payload.username()))
                            .build();
                    return chain.filter(withUser);
                });
    }

    private boolean isWhitelisted(String path) {
        return properties.getWhitelist().stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, ErrorCode errorCode) {
        return unauthorized(exchange, errorCode.getCode(), errorCode.getMessage());
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, int code, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] body;
        try {
            body = OBJECT_MAPPER.writeValueAsBytes(Result.fail(code, message));
        } catch (Exception e) {
            body = "{\"code\":401001,\"message\":\"未登录或凭证无效\",\"data\":null}".getBytes(StandardCharsets.UTF_8);
        }
        DataBuffer buffer = response.bufferFactory().wrap(body);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
