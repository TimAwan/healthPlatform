package com.health.platform.system.controller;

import com.health.platform.core.result.Result;
import com.health.platform.security.context.UserContext;
import com.health.platform.security.permission.RequirePermission;
import com.health.platform.system.entity.SysMenu;
import com.health.platform.system.service.RbacService;
import com.health.platform.system.service.SysMenuService;
import com.health.platform.system.vo.SysMenuVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/system/menus")
@RequiredArgsConstructor
public class MenuController {

    private final SysMenuService sysMenuService;
    private final RbacService rbacService;

    /** 当前登录用户可见菜单（动态菜单，登录后前端拉取） */
    @GetMapping("/me")
    @RequirePermission("MENU_VIEW")
    public Result<List<SysMenuVO>> myMenus() {
        return Result.ok(rbacService.getUserMenuTree(UserContext.require().userId()));
    }

    @GetMapping
    @RequirePermission("MENU_VIEW")
    public Result<List<SysMenuVO>> tree() {
        return Result.ok(sysMenuService.tree());
    }

    @PostMapping
    @RequirePermission("MENU_UPDATE")
    public Result<Long> create(@RequestBody SysMenu menu) {
        sysMenuService.create(menu);
        return Result.ok(menu.getId());
    }

    @PutMapping("/{id}")
    @RequirePermission("MENU_UPDATE")
    public Result<Void> update(@RequestBody SysMenu menu) {
        sysMenuService.update(menu);
        return Result.ok();
    }
}
