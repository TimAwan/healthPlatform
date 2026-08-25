package com.health.platform.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.health.platform.auth.dto.LoginRequest;
import com.health.platform.auth.dto.TokenVO;
import com.health.platform.auth.entity.RefreshToken;
import com.health.platform.auth.mapper.RefreshTokenMapper;
import com.health.platform.core.constant.RedisKeyConstants;
import com.health.platform.core.constant.SecurityConstants;
import com.health.platform.core.exception.AuthenticationException;
import com.health.platform.core.exception.BizException;
import com.health.platform.core.result.CommonErrorCode;
import com.health.platform.core.result.Result;
import com.health.platform.security.context.UserContext;
import com.health.platform.security.internal.SystemInternalClient;
import com.health.platform.security.internal.UserCredentialsDTO;
import com.health.platform.core.jwt.JwtPayload;
import com.health.platform.core.jwt.JwtProperties;
import com.health.platform.core.jwt.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private static final int MAX_LOGIN_FAILURES = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(10);

    private final SystemInternalClient systemInternalClient;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;
    private final JwtUtils jwtUtils;
    private final JwtProperties jwtProperties;
    private final RefreshTokenMapper refreshTokenMapper;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(SystemInternalClient systemInternalClient,
                       PasswordEncoder passwordEncoder,
                       StringRedisTemplate redisTemplate,
                       JwtUtils jwtUtils,
                       JwtProperties jwtProperties,
                       RefreshTokenMapper refreshTokenMapper) {
        this.systemInternalClient = systemInternalClient;
        this.passwordEncoder = passwordEncoder;
        this.redisTemplate = redisTemplate;
        this.jwtUtils = jwtUtils;
        this.jwtProperties = jwtProperties;
        this.refreshTokenMapper = refreshTokenMapper;
    }

    public TokenVO login(LoginRequest request) {
        captchaMustPass(request);
        assertNotLocked(request.getUsername());

        UserCredentialsDTO credentials = loadCredentials(request.getUsername());
        if (credentials == null || !passwordEncoder.matches(request.getPassword(), credentials.getPasswordHash())) {
            recordFailure(request.getUsername());
            throw new BizException(CommonErrorCode.LOGIN_FAILED);
        }
        if (!"ENABLED".equals(credentials.getStatus())) {
            throw new BizException(CommonErrorCode.LOGIN_LOCKED, "账号已停用，请联系管理员");
        }
        redisTemplate.delete(RedisKeyConstants.LOGIN_FAIL_COUNT + request.getUsername());
        notifyLoginSuccess(credentials.getUserId());
        return issueTokens(credentials.getUserId(), credentials.getUsername());
    }

    public TokenVO refresh(String refreshTokenString) {
        RefreshToken stored = refreshTokenMapper.selectOne(new LambdaQueryWrapper<RefreshToken>()
                .eq(RefreshToken::getToken, refreshTokenString));
        if (stored == null || !RefreshToken.STATUS_ACTIVE.equals(stored.getStatus())
                || stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AuthenticationException(CommonErrorCode.TOKEN_EXPIRED);
        }
        // 轮换：旧刷新令牌立即失效
        revokeById(stored.getId());
        UserCredentialsDTO credentials = loadCredentialsById(stored.getUserId());
        if (credentials == null || !"ENABLED".equals(credentials.getStatus())) {
            throw new AuthenticationException(CommonErrorCode.UNAUTHORIZED);
        }
        return issueTokens(credentials.getUserId(), credentials.getUsername());
    }

    public void logout(String accessToken) {
        if (accessToken != null && accessToken.startsWith(SecurityConstants.BEARER_PREFIX)) {
            JwtPayload payload;
            try {
                payload = jwtUtils.parseAccessToken(accessToken.substring(SecurityConstants.BEARER_PREFIX.length()));
            } catch (AuthenticationException e) {
                payload = null;
            }
            if (payload != null) {
                long remaining = payload.expiresAtEpochSeconds() - System.currentTimeMillis() / 1000;
                if (remaining > 0) {
                    redisTemplate.opsForValue().set(RedisKeyConstants.TOKEN_BLACKLIST + payload.jti(),
                            "1", Duration.ofSeconds(remaining));
                }
            }
        }
        Long userId = UserContext.currentUserIdOrNull();
        if (userId != null) {
            refreshTokenMapper.update(null, new LambdaUpdateWrapper<RefreshToken>()
                    .eq(RefreshToken::getUserId, userId)
                    .eq(RefreshToken::getStatus, RefreshToken.STATUS_ACTIVE)
                    .set(RefreshToken::getStatus, RefreshToken.STATUS_REVOKED));
        }
    }

    private void captchaMustPass(LoginRequest request) {
        String stored = redisTemplate.opsForValue().get(RedisKeyConstants.CAPTCHA + request.getCaptchaId());
        if (stored == null || !stored.equalsIgnoreCase(request.getCaptchaCode())) {
            throw new BizException(CommonErrorCode.CAPTCHA_ERROR);
        }
        redisTemplate.delete(RedisKeyConstants.CAPTCHA + request.getCaptchaId());
    }

    private void assertNotLocked(String username) {
        String count = redisTemplate.opsForValue().get(RedisKeyConstants.LOGIN_FAIL_COUNT + username);
        if (count != null && Integer.parseInt(count) >= MAX_LOGIN_FAILURES) {
            throw new BizException(CommonErrorCode.LOGIN_LOCKED);
        }
    }

    private void recordFailure(String username) {
        String key = RedisKeyConstants.LOGIN_FAIL_COUNT + username;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, LOCK_DURATION);
        }
    }

    private UserCredentialsDTO loadCredentials(String username) {
        Result<UserCredentialsDTO> response = systemInternalClient.getUserCredentials(username);
        return response == null ? null : response.getData();
    }

    private UserCredentialsDTO loadCredentialsById(Long userId) {
        Result<UserCredentialsDTO> response = systemInternalClient.getUserCredentialsById(userId);
        return response == null ? null : response.getData();
    }

    private void notifyLoginSuccess(Long userId) {
        try {
            systemInternalClient.markLoginSuccess(userId);
        } catch (Exception e) {
            log.warn("通知登录成功失败 userId={}: {}", userId, e.getMessage());
        }
    }

    private TokenVO issueTokens(Long userId, String username) {
        String accessToken = jwtUtils.signAccessToken(userId, username);
        String refreshToken = newRefreshTokenValue();

        RefreshToken entity = new RefreshToken();
        entity.setUserId(userId);
        entity.setToken(refreshToken);
        entity.setStatus(RefreshToken.STATUS_ACTIVE);
        entity.setExpiresAt(LocalDateTime.now().plusSeconds(jwtProperties.getRefreshTtlSeconds()));
        refreshTokenMapper.insert(entity);

        TokenVO vo = new TokenVO();
        vo.setAccessToken(accessToken);
        vo.setRefreshToken(refreshToken);
        vo.setExpiresIn(jwtProperties.getAccessTtlSeconds());
        return vo;
    }

    private void revokeById(Long id) {
        RefreshToken update = new RefreshToken();
        update.setId(id);
        update.setStatus(RefreshToken.STATUS_REVOKED);
        refreshTokenMapper.updateById(update);
    }

    private String newRefreshTokenValue() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
