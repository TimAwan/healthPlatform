package com.health.platform.system.controller;

import com.health.platform.core.result.Result;
import com.health.platform.security.context.CurrentUser;
import com.health.platform.security.context.UserContext;
import com.health.platform.system.entity.SysUser;
import com.health.platform.system.service.RbacService;
import com.health.platform.system.service.SysUserService;
import com.health.platform.system.vo.MeVO;
import com.health.platform.system.vo.SysMenuVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/system/me")
@RequiredArgsConstructor
public class MeController {

    private final RbacService rbacService;
    private final SysUserService sysUserService;

    @GetMapping
    public Result<MeVO> me() {
        CurrentUser current = UserContext.require();
        Set<String> permissions = rbacService.getUserPermissionCodes(current.userId());
        var menus = rbacService.getUserMenuTree(current.userId());

        MeVO vo = new MeVO();
        vo.setUserId(current.userId());
        vo.setUsername(current.username());
        SysUser user = sysUserService.getById(current.userId());
        vo.setRealName(user == null ? current.username() : user.getRealName());
        vo.setPermissions(permissions);
        vo.setMenus(menus);
        return Result.ok(vo);
    }
}
