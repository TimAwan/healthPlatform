package com.health.platform.auth.service;

import com.health.platform.auth.dto.LoginRequest;
import com.health.platform.auth.dto.TokenVO;
import com.health.platform.auth.entity.RefreshToken;
import com.health.platform.auth.mapper.RefreshTokenMapper;
import com.health.platform.core.exception.AuthenticationException;
import com.health.platform.core.exception.BizException;
import com.health.platform.core.jwt.JwtProperties;
import com.health.platform.core.jwt.JwtUtils;
import com.health.platform.core.result.CommonErrorCode;
import com.health.platform.core.result.Result;
import com.health.platform.security.internal.SystemInternalClient;
import com.health.platform.security.internal.UserCredentialsDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private SystemInternalClient systemInternalClient;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private RefreshTokenMapper refreshTokenMapper;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret("unit-test-secret-0123456789-0123456789-0123456789");
        authService = new AuthService(systemInternalClient,
                new BCryptPasswordEncoder(),
                redisTemplate,
                new JwtUtils(jwtProperties),
                jwtProperties,
                refreshTokenMapper);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    private LoginRequest loginRequest() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("Admin@123456");
        request.setCaptchaId("ID");
        request.setCaptchaCode("abcd");
        return request;
    }

    private UserCredentialsDTO credentials(String status, String rawPassword) {
        UserCredentialsDTO dto = new UserCredentialsDTO();
        dto.setUserId(1L);
        dto.setUsername("admin");
        dto.setPasswordHash(new BCryptPasswordEncoder().encode(rawPassword));
        dto.setStatus(status);
        dto.setRealName("系统管理员");
        return dto;
    }

    @Test
    void loginSuccessShouldIssueTokensAndClearFailCount() {
        when(valueOperations.get("health:auth:captcha:ID")).thenReturn("ABCD");
        when(valueOperations.get("health:auth:fail:admin")).thenReturn(null);
        when(systemInternalClient.getUserCredentials("admin"))
                .thenReturn(Result.ok(credentials("ENABLED", "Admin@123456")));

        TokenVO vo = authService.login(loginRequest());

        assertNotNull(vo.getAccessToken());
        assertNotNull(vo.getRefreshToken());
        assertEquals("Bearer", vo.getTokenType());
        verify(redisTemplate).delete("health:auth:fail:admin");
        verify(refreshTokenMapper).insert(any(RefreshToken.class));
        verify(systemInternalClient).markLoginSuccess(1L);
    }

    @Test
    void loginWithWrongCaptchaShouldFailBeforePasswordCheck() {
        when(valueOperations.get("health:auth:captcha:ID")).thenReturn(null);
        BizException e = assertThrows(BizException.class, () -> authService.login(loginRequest()));
        assertEquals(CommonErrorCode.CAPTCHA_ERROR.getCode(), e.getCode());
        verify(systemInternalClient, never()).getUserCredentials(anyString());
    }

    @Test
    void loginWithWrongPasswordShouldCountFailure() {
        when(valueOperations.get("health:auth:captcha:ID")).thenReturn("ABCD");
        when(valueOperations.get("health:auth:fail:admin")).thenReturn(null);
        when(systemInternalClient.getUserCredentials("admin"))
                .thenReturn(Result.ok(credentials("ENABLED", "RightPassword1")));

        BizException e = assertThrows(BizException.class, () -> authService.login(loginRequest()));
        assertEquals(CommonErrorCode.LOGIN_FAILED.getCode(), e.getCode());
        verify(valueOperations).increment("health:auth:fail:admin");
    }

    @Test
    void loginWithUnknownUserShouldFailAsLoginFailed() {
        when(valueOperations.get("health:auth:captcha:ID")).thenReturn("ABCD");
        when(valueOperations.get("health:auth:fail:admin")).thenReturn(null);
        when(systemInternalClient.getUserCredentials("admin")).thenReturn(Result.ok(null));

        BizException e = assertThrows(BizException.class, () -> authService.login(loginRequest()));
        assertEquals(CommonErrorCode.LOGIN_FAILED.getCode(), e.getCode());
    }

    @Test
    void loginShouldRejectWhenAccountLocked() {
        when(valueOperations.get("health:auth:captcha:ID")).thenReturn("ABCD");
        when(valueOperations.get("health:auth:fail:admin")).thenReturn("5");

        BizException e = assertThrows(BizException.class, () -> authService.login(loginRequest()));
        assertEquals(CommonErrorCode.LOGIN_LOCKED.getCode(), e.getCode());
        verify(systemInternalClient, never()).getUserCredentials(anyString());
    }

    @Test
    void loginShouldRejectDisabledUser() {
        when(valueOperations.get("health:auth:captcha:ID")).thenReturn("ABCD");
        when(valueOperations.get("health:auth:fail:admin")).thenReturn(null);
        when(systemInternalClient.getUserCredentials("admin"))
                .thenReturn(Result.ok(credentials("DISABLED", "Admin@123456")));

        BizException e = assertThrows(BizException.class, () -> authService.login(loginRequest()));
        assertEquals(CommonErrorCode.LOGIN_LOCKED.getCode(), e.getCode());
        assertEquals("账号已停用，请联系管理员", e.getMessage());
    }

    @Test
    void refreshShouldRotateToken() {
        RefreshToken stored = new RefreshToken();
        stored.setId(9L);
        stored.setUserId(1L);
        stored.setToken("old-token");
        stored.setStatus(RefreshToken.STATUS_ACTIVE);
        stored.setExpiresAt(LocalDateTime.now().plusDays(1));
        when(refreshTokenMapper.selectOne(any())).thenReturn(stored);
        when(systemInternalClient.getUserCredentialsById(1L))
                .thenReturn(Result.ok(credentials("ENABLED", "x")));

        TokenVO vo = authService.refresh("old-token");

        assertNotNull(vo.getAccessToken());
        assertNotEquals("old-token", vo.getRefreshToken());
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenMapper).updateById(captor.capture());
        assertEquals(9L, captor.getValue().getId());
        assertEquals(RefreshToken.STATUS_REVOKED, captor.getValue().getStatus());
        verify(refreshTokenMapper).insert(any(RefreshToken.class));
    }

    @Test
    void refreshWithRevokedTokenShouldThrow() {
        RefreshToken stored = new RefreshToken();
        stored.setId(9L);
        stored.setUserId(1L);
        stored.setToken("old-token");
        stored.setStatus(RefreshToken.STATUS_REVOKED);
        stored.setExpiresAt(LocalDateTime.now().plusDays(1));
        when(refreshTokenMapper.selectOne(any())).thenReturn(stored);

        AuthenticationException e = assertThrows(AuthenticationException.class,
                () -> authService.refresh("old-token"));
        assertEquals(CommonErrorCode.TOKEN_EXPIRED.getCode(), e.getCode());
    }

    @Test
    void refreshWithExpiredTokenShouldThrow() {
        RefreshToken stored = new RefreshToken();
        stored.setId(9L);
        stored.setUserId(1L);
        stored.setToken("old-token");
        stored.setStatus(RefreshToken.STATUS_ACTIVE);
        stored.setExpiresAt(LocalDateTime.now().minusDays(1));
        when(refreshTokenMapper.selectOne(any())).thenReturn(stored);

        assertThrows(AuthenticationException.class, () -> authService.refresh("old-token"));
    }
}
