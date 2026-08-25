package com.health.platform.auth.controller;

import com.health.platform.auth.dto.CaptchaVO;
import com.health.platform.auth.dto.LoginRequest;
import com.health.platform.auth.dto.RefreshRequest;
import com.health.platform.auth.dto.TokenVO;
import com.health.platform.auth.service.AuthService;
import com.health.platform.auth.service.CaptchaService;
import com.health.platform.core.constant.SecurityConstants;
import com.health.platform.core.result.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final CaptchaService captchaService;

    public AuthController(AuthService authService, CaptchaService captchaService) {
        this.authService = authService;
        this.captchaService = captchaService;
    }

    @PostMapping("/login")
    public Result<TokenVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public Result<TokenVO> refresh(@Valid @RequestBody RefreshRequest request) {
        return Result.ok(authService.refresh(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public Result<Void> logout(
            @RequestHeader(value = SecurityConstants.AUTHORIZATION_HEADER, required = false) String authorization) {
        authService.logout(authorization);
        return Result.ok();
    }

    @GetMapping("/captcha")
    public Result<CaptchaVO> captcha() {
        return Result.ok(captchaService.generate());
    }
}
