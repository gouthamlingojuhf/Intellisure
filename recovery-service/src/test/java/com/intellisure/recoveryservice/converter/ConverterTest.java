package com.intellisure.recoveryservice.converter;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ConverterTest {
    @Test
    void uuidConvertersRoundTripAndHandleNullOrInvalidInput() {
        UUID source = UUID.randomUUID();
        assertEquals(source, new BytesToUuidConverter().convert(new UuidToBytesConverter().convert(source)));
        assertNull(new UuidToBytesConverter().convert(null));
        assertNull(new BytesToUuidConverter().convert(null));
        assertNull(new BytesToUuidConverter().convert(new byte[15]));
    }

    @Test
    void jwtConverterNormalizesRoleClaimShapes() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        assertEquals(List.of("ROLE_ADMIN", "ROLE_USER"), converter.convert(jwt(Map.of("roles", List.of("admin", "ROLE_USER")))).block()
                .getAuthorities().stream().map(a -> a.getAuthority()).toList());
        assertEquals(List.of("ROLE_ADMIN", "ROLE_USER"), converter.convert(jwt(Map.of("roles", "admin, ROLE_USER, "))).block()
                .getAuthorities().stream().map(a -> a.getAuthority()).toList());
        assertEquals("ROLE_OPERATOR", converter.convert(jwt(Map.of("role", "operator"))).block().getAuthorities().iterator().next().getAuthority());
        assertTrue(converter.convert(jwt(Map.of())).block().getAuthorities().isEmpty());
    }

    private Jwt jwt(Map<String, Object> extra) {
        java.util.Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("sub", UUID.randomUUID().toString());
        claims.putAll(extra);
        return new Jwt("token", Instant.now(), Instant.now().plusSeconds(300), Map.of("alg", "HS256"), claims);
    }
}
