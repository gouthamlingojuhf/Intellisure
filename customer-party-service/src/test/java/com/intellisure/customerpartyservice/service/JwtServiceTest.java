package com.intellisure.customerpartyservice.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    @Test
    void generatesTokenWithSubjectRoleAndConfiguredExpiry() {
        SecretKey key = Keys.hmacShaKeyFor(
                "test-secret-key-that-is-at-least-32-bytes".getBytes()
        );
        JwtService jwtService = new JwtService(key);
        ReflectionTestUtils.setField(jwtService, "expirationTime", 60_000L);
        UUID userId = UUID.randomUUID();

        String token = jwtService.generateToken(userId, "POLICYHOLDER");

        var claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        assertThat(token).isNotBlank();
        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        assertThat(claims.get("role", String.class)).isEqualTo("POLICYHOLDER");
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }
}
