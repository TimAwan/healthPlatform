package com.health.platform.system.controller;

import com.health.platform.core.result.PageQuery;
import com.health.platform.core.result.PageResult;
import com.health.platform.core.result.Result;
import com.health.platform.security.permission.RequirePermission;
import com.health.platform.system.dto.IdsRequest;
import com.health.platform.system.dto.SysRoleDTO;
import com.health.platform.system.entity.SysRole;
import com.health.platform.system.service.SysRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/system/roles")
@RequiredArgsConstructor
public class RoleController {

    private final SysRoleService sysRoleService;

    @GetMapping
    @RequirePermission("ROLE_VIEW")
    public Result<PageResult<SysRole>> page(PageQuery query, @RequestParam(required = false) String keyword) {
        return Result.ok(sysRoleService.page(query, keyword));
    }

    @GetMapping("/all")
    @RequirePermission("ROLE_VIEW")
    public Result<List<SysRole>> listAll() {
        return Result.ok(sysRoleService.listAll());
    }

    @PostMapping
    @RequirePermission("ROLE_CREATE")
    public Result<Long> create(@Valid @RequestBody SysRoleDTO dto) {
        return Result.ok(sysRoleService.create(dto).getId());
    }

    @PutMapping("/{id}")
    @RequirePermission("ROLE_UPDATE")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody SysRoleDTO dto) {
        sysRoleService.update(id, dto);
        return Result.ok();
    }

    @GetMapping("/{id}/permissions")
    @RequirePermission("ROLE_VIEW")
    public Result<List<Long>> getPermissionIds(@PathVariable Long id) {
        return Result.ok(sysRoleService.getPermissionIds(id));
    }

    @PutMapping("/{id}/permissions")
    @RequirePermission("ROLE_ASSIGN")
    public Result<Void> assignPermissions(@PathVariable Long id, @Valid @RequestBody IdsRequest request) {
        sysRoleService.assignPermissions(id, request.getIds());
        return Result.ok();
    }

    @GetMapping("/{id}/menus")
    @RequirePermission("ROLE_VIEW")
    public Result<List<Long>> getMenuIds(@PathVariable Long id) {
        return Result.ok(sysRoleService.getMenuIds(id));
    }

    @PutMapping("/{id}/menus")
    @RequirePermission("ROLE_ASSIGN")
    public Result<Void> assignMenus(@PathVariable Long id, @Valid @RequestBody IdsRequest request) {
        sysRoleService.assignMenus(id, request.getIds());
        return Result.ok();
    }
}
