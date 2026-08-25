package com.health.platform.auth.service;

import com.health.platform.auth.dto.CaptchaVO;
import com.health.platform.core.exception.BizException;
import com.health.platform.core.result.CommonErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaptchaServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private CaptchaService captchaService;

    @BeforeEach
    void setUp() {
        captchaService = new CaptchaService(redisTemplate);
    }

    @Test
    void generateShouldStoreTextAndReturnBase64Image() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        CaptchaVO vo = captchaService.generate();
        assertTrue(vo.getCaptchaId().length() == 32);
        assertTrue(vo.getImage().startsWith("data:image/png;base64,"));
        verify(valueOperations).set(anyString(), anyString(), any(java.time.Duration.class));
    }

    @Test
    void verifyShouldPassCaseInsensitiveAndConsume() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("health:auth:captcha:ID")).thenReturn("ABCD");
        captchaService.verify("ID", "abcd");
        verify(redisTemplate).delete("health:auth:captcha:ID");
    }

    @Test
    void verifyWrongCodeShouldThrowCaptchaError() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn("ABCD");
        BizException e = assertThrows(BizException.class, () -> captchaService.verify("ID", "WXYZ"));
        assertEquals(CommonErrorCode.CAPTCHA_ERROR.getCode(), e.getCode());
    }

    @Test
    void verifyExpiredCaptchaShouldThrowCaptchaError() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        BizException e = assertThrows(BizException.class, () -> captchaService.verify("ID", "ABCD"));
        assertEquals(CommonErrorCode.CAPTCHA_ERROR.getCode(), e.getCode());
    }
}
