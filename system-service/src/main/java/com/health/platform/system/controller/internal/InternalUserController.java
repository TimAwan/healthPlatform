package com.health.platform.system.controller.internal;

import com.health.platform.core.result.Result;
import com.health.platform.security.internal.UserCredentialsDTO;
import com.health.platform.system.entity.SysUser;
import com.health.platform.system.service.SysUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * 服务间内部接口，经 InternalTokenFilter 校验 X-Internal-Token，不对 Gateway 暴露。
 */
@RestController
@RequestMapping("/internal/users")
public class InternalUserController {

    private final SysUserService sysUserService;

    public InternalUserController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @GetMapping("/{username}/credentials")
    public Result<UserCredentialsDTO> getCredentials(@PathVariable("username") String username) {
        SysUser user = sysUserService.getByUsername(username);
        return Result.ok(toCredentials(user));
    }

    @GetMapping("/id/{id}/credentials")
    public Result<UserCredentialsDTO> getCredentialsById(@PathVariable("id") Long id) {
        SysUser user = sysUserService.getById(id);
        return Result.ok(toCredentials(user));
    }

    private UserCredentialsDTO toCredentials(SysUser user) {
        if (user == null) {
            return null;
        }
        UserCredentialsDTO dto = new UserCredentialsDTO();
        dto.setUserId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setPasswordHash(user.getPassword());
        dto.setStatus(user.getStatus());
        dto.setRealName(user.getRealName());
        return dto;
    }

    @GetMapping("/{id}/permissions")
    public Result<Set<String>> getPermissions(@PathVariable("id") Long id) {
        // TODO: M3 接入 RBAC 后按 角色-权限 关联返回真实权限码集合
        return Result.ok(Set.of());
    }

    @PostMapping("/{id}/login-success")
    public Result<Void> markLoginSuccess(@PathVariable("id") Long id) {
        sysUserService.markLoginSuccess(id);
        return Result.ok();
    }
}
