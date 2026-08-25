package com.health.platform.security.context;

import com.health.platform.core.constant.SecurityConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 从 Gateway 透传的信任头恢复当前用户上下文。
 * 依赖 Gateway 已剥离外部伪造的 X-User-* 头（见 gateway-service AuthFilter）。
 */
public class UserContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String userId = request.getHeader(SecurityConstants.USER_ID_HEADER);
        String username = request.getHeader(SecurityConstants.USERNAME_HEADER);
        if (StringUtils.hasText(userId) && StringUtils.hasText(username)) {
            try {
                UserContext.set(new CurrentUser(Long.valueOf(userId), username));
            } catch (NumberFormatException ignored) {
                // 非法头按未登录处理，由权限拦截器统一拒绝
            }
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }
}
