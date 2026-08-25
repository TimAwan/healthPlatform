package com.health.platform.security.permission;

import com.health.platform.core.exception.PermissionDeniedException;
import com.health.platform.security.context.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.Set;

public class PermissionInterceptor implements HandlerInterceptor {

    private final PermissionProvider permissionProvider;

    public PermissionInterceptor(PermissionProvider permissionProvider) {
        this.permissionProvider = permissionProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        RequirePermission annotation = AnnotatedElementUtils.findMergedAnnotation(
                handlerMethod.getMethod(), RequirePermission.class);
        if (annotation == null) {
            annotation = AnnotatedElementUtils.findMergedAnnotation(
                    handlerMethod.getBeanType(), RequirePermission.class);
        }
        if (annotation == null) {
            return true;
        }
        UserContext.require();
        Set<String> owned = permissionProvider.getPermissions(UserContext.require().userId());
        boolean allowed = annotation.logical() == RequirePermission.Logical.ANY
                ? Arrays.stream(annotation.value()).anyMatch(owned::contains)
                : Arrays.stream(annotation.value()).allMatch(owned::contains);
        if (!allowed) {
            throw new PermissionDeniedException();
        }
        return true;
    }
}
