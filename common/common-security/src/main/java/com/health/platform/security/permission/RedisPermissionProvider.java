package com.health.platform.security.permission;

import com.health.platform.core.constant.RedisKeyConstants;
import com.health.platform.core.result.Result;
import com.health.platform.security.internal.SystemInternalClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

/**
 * 权限码来源：Redis 缓存，未命中时经 Feign 调 system-service 加载并缓存 1 小时。
 * 角色权限变更时由 system-service 主动删除缓存键（M3 实现）。
 * Feign 客户端必须延迟到首次鉴权时解析：本类由 WebMvcConfigurer 链装配，
 * 启动期立即创建 Feign 代理会与 WebMvcAutoConfiguration 形成循环依赖。
 */
public class RedisPermissionProvider implements PermissionProvider {

    private static final Duration CACHE_TTL = Duration.ofHours(1);

    private final StringRedisTemplate redisTemplate;
    private final ObjectProvider<SystemInternalClient> systemInternalClient;

    public RedisPermissionProvider(StringRedisTemplate redisTemplate,
                                   ObjectProvider<SystemInternalClient> systemInternalClient) {
        this.redisTemplate = redisTemplate;
        this.systemInternalClient = systemInternalClient;
    }

    @Override
    public Set<String> getPermissions(Long userId) {
        String key = RedisKeyConstants.USER_PERMISSIONS + userId;
        Set<String> cached = redisTemplate.opsForSet().members(key);
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }
        Result<Set<String>> response = systemInternalClient.getObject().getUserPermissions(userId);
        Set<String> permissions = response != null && response.getData() != null ? response.getData() : new HashSet<>();
        if (!permissions.isEmpty()) {
            redisTemplate.opsForSet().add(key, permissions.toArray(new String[0]));
            redisTemplate.expire(key, CACHE_TTL);
            return permissions;
        }
        // 无权限不缓存，等待授权后生效
        return permissions;
    }
}
