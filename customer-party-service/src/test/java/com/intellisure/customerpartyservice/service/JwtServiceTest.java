package com.intellisure.customerpartyservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private SecretKey secretKey;
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        secretKey = Keys.hmacShaKeyFor(
                "01234567890123456789012345678901"
                        .getBytes()
        );

        jwtService =
                new JwtService(secretKey);

        ReflectionTestUtils.setField(
                jwtService,
                "expirationTime",
                3600000L
        );
    }

    @Test
    void shouldGeneratePolicyholderTokenWithCustomerId() {
        UUID userId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        String token =
                jwtService.generateToken(
                        userId,
                        "POLICYHOLDER",
                        customerId
                );

        Claims claims = parseClaims(token);

        assertEquals(
                userId.toString(),
                claims.getSubject()
        );

        assertEquals(
                "POLICYHOLDER",
                claims.get("role")
        );

        assertEquals(
                customerId.toString(),
                claims.get("customerId")
        );

        List<?> roles =
                claims.get("roles", List.class);

        assertEquals(1, roles.size());
        assertEquals("POLICYHOLDER", roles.get(0));

        assertTrue(
                claims.getExpiration()
                        .after(claims.getIssuedAt())
        );
    }

    @Test
    void shouldGenerateInternalUserTokenWithoutCustomerId() {
        UUID userId = UUID.randomUUID();

        String token =
                jwtService.generateToken(
                        userId,
                        "ROLE_UNDERWRITER",
                        null
                );

        Claims claims = parseClaims(token);

        assertEquals(
                userId.toString(),
                claims.getSubject()
        );

        assertEquals(
                "UNDERWRITER",
                claims.get("role")
        );

        assertNull(claims.get("customerId"));

        assertFalse(
                claims.containsKey("customerId")
        );
    }

    @Test
    void shouldReturnExpirationInSeconds() {
        assertEquals(
                3600L,
                jwtService.getExpirationSeconds()
        );
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}