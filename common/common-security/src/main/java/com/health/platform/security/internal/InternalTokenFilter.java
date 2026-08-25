package com.health.platform.security.internal;

import com.health.platform.core.exception.AuthenticationException;
import com.health.platform.core.result.CommonErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * /internal/** 仅允许服务间调用：校验共享内部令牌 X-Internal-Token。
 * 内部令牌通过环境变量 INTERNAL_TOKEN 注入。
 */
public class InternalTokenFilter extends OncePerRequestFilter {

    public static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";
    public static final String INTERNAL_TOKEN_PROPERTY = "${INTERNAL_TOKEN:dev-internal-token}";

    private final String expectedToken;

    public InternalTokenFilter(String expectedToken) {
        this.expectedToken = expectedToken;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = request.getHeader(INTERNAL_TOKEN_HEADER);
        if (!StringUtils.hasText(token) || !expectedToken.equals(token)) {
            throw new AuthenticationException(CommonErrorCode.UNAUTHORIZED, "内部接口调用未授权");
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/internal");
    }
}
