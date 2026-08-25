package com.health.platform.core.jwt;

public record JwtPayload(Long userId, String username, String jti, long expiresAtEpochSeconds) {
}
