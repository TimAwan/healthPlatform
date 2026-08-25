package com.health.platform.core.jwt;

import com.health.platform.core.exception.AuthenticationException;
import com.health.platform.core.result.CommonErrorCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtUtilsTest {

    private JwtUtils jwtUtils(long ttlSeconds) {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("test-secret-key-0123456789-0123456789-0123456789");
        properties.setAccessTtlSeconds(ttlSeconds);
        return new JwtUtils(properties);
    }

    @Test
    void signAndParseShouldRoundTrip() {
        JwtUtils utils = jwtUtils(600);
        String token = utils.signAccessToken(42L, "admin");
        JwtPayload payload = utils.parseAccessToken(token);
        assertEquals(42L, payload.userId());
        assertEquals("admin", payload.username());
        assertNotNull(payload.jti());
    }

    @Test
    void expiredTokenShouldThrowTokenExpired() {
        JwtUtils utils = jwtUtils(-10);
        String token = utils.signAccessToken(1L, "admin");
        AuthenticationException e = assertThrows(AuthenticationException.class, () -> utils.parseAccessToken(token));
        assertEquals(CommonErrorCode.TOKEN_EXPIRED.getCode(), e.getCode());
    }

    @Test
    void tamperedTokenShouldThrowTokenInvalid() {
        JwtUtils utils = jwtUtils(600);
        String token = utils.signAccessToken(1L, "admin");
        String[] parts = token.split("\\.");
        char flipped = parts[2].charAt(0) == 'A' ? 'B' : 'A';
        String tampered = parts[0] + "." + parts[1] + "." + flipped + parts[2].substring(1);
        AuthenticationException e = assertThrows(AuthenticationException.class, () -> utils.parseAccessToken(tampered));
        assertEquals(CommonErrorCode.TOKEN_INVALID.getCode(), e.getCode());
    }

    @Test
    void wrongKeyTokenShouldThrowTokenInvalid() {
        String token = jwtUtils(600).signAccessToken(1L, "admin");
        JwtProperties other = new JwtProperties();
        other.setSecret("another-secret-key-9876543210-9876543210-9876543210");
        other.setAccessTtlSeconds(600);
        AuthenticationException e = assertThrows(AuthenticationException.class,
                () -> new JwtUtils(other).parseAccessToken(token));
        assertEquals(CommonErrorCode.TOKEN_INVALID.getCode(), e.getCode());
    }
}
