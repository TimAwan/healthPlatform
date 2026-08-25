package com.health.platform.core.jwt;

import com.health.platform.core.constant.SecurityConstants;
import com.health.platform.core.exception.AuthenticationException;
import com.health.platform.core.result.CommonErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

public class JwtUtils {

    private final SecretKey key;
    private final JwtProperties properties;

    public JwtUtils(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String signAccessToken(Long userId, String username) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(properties.getAccessTtlSeconds());
        return Jwts.builder()
                .id(UUID.randomUUID().toString().replace("-", ""))
                .subject(String.valueOf(userId))
                .claim(SecurityConstants.CLAIM_USERNAME, username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(key)
                .compact();
    }

    public JwtPayload parseAccessToken(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(token).getPayload();
            return new JwtPayload(
                    Long.valueOf(claims.getSubject()),
                    claims.get(SecurityConstants.CLAIM_USERNAME, String.class),
                    claims.getId(),
                    claims.getExpiration().toInstant().getEpochSecond());
        } catch (ExpiredJwtException e) {
            throw new AuthenticationException(CommonErrorCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new AuthenticationException(CommonErrorCode.TOKEN_INVALID);
        }
    }
}
