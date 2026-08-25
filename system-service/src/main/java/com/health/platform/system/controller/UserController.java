package com.health.platform.system.controller;

import com.health.platform.core.result.PageQuery;
import com.health.platform.core.result.PageResult;
import com.health.platform.core.result.Result;
import com.health.platform.security.permission.RequirePermission;
import com.health.platform.system.dto.IdsRequest;
import com.health.platform.system.dto.SysUserDTO;
import com.health.platform.system.dto.SysUserUpdateDTO;
import com.health.platform.system.service.SysUserService;
import com.health.platform.system.vo.SysUserVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final SysUserService sysUserService;

    @GetMapping
    @RequirePermission("USER_VIEW")
    public Result<PageResult<SysUserVO>> page(PageQuery query,
                                              @RequestParam(required = false) String keyword,
                                              @RequestParam(required = false) String status) {
        return Result.ok(sysUserService.page(query, keyword, status));
    }

    @PostMapping
    @RequirePermission("USER_CREATE")
    public Result<Long> create(@Valid @RequestBody SysUserDTO dto) {
        return Result.ok(sysUserService.create(dto));
    }

    @PutMapping("/{id}")
    @RequirePermission("USER_UPDATE")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody SysUserUpdateDTO dto) {
        sysUserService.update(id, dto);
        return Result.ok();
    }

    @PostMapping("/{id}/enable")
    @RequirePermission("USER_DISABLE")
    public Result<Void> enable(@PathVariable Long id) {
        sysUserService.changeStatus(id, true);
        return Result.ok();
    }

    @PostMapping("/{id}/disable")
    @RequirePermission("USER_DISABLE")
    public Result<Void> disable(@PathVariable Long id) {
        sysUserService.changeStatus(id, false);
        return Result.ok();
    }

    @PostMapping("/{id}/password-reset")
    @RequirePermission("USER_RESET_PASSWORD")
    public Result<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordRequest request) {
        sysUserService.resetPassword(id, request.getPassword());
        return Result.ok();
    }

    @PutMapping("/{id}/roles")
    @RequirePermission("USER_ASSIGN_ROLE")
    public Result<Void> assignRoles(@PathVariable Long id, @Valid @RequestBody IdsRequest request) {
        sysUserService.assignRoles(id, request.getIds());
        return Result.ok();
    }

    @Data
    public static class ResetPasswordRequest {

        @NotBlank(message = "新密码不能为空")
        @Size(min = 8, max = 100, message = "密码长度 8-100")
        private String password;
    }
}
