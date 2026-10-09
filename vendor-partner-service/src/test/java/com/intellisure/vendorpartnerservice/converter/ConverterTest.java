package com.intellisure.vendorpartnerservice.converter;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ConverterTest {
    @Test
    void uuidAndJsonConvertersHandleRoundTripsAndNulls() {
        UUID id = UUID.randomUUID();
        assertEquals(id, new BytesToUuidConverter().convert(new UuidToBytesConverter().convert(id)));
        assertNull(new BytesToUuidConverter().convert(null)); assertNull(new BytesToUuidConverter().convert(new byte[15]));
        assertNull(new UuidToBytesConverter().convert(null));
        List<String> values = List.of("A", "B");
        String json = new ListToJsonConverter().convert(values);
        assertEquals(values, new JsonToListConverter().convert(json));
        assertEquals("[]", new ListToJsonConverter().convert(null)); assertTrue(new JsonToListConverter().convert(null).isEmpty());
        assertTrue(new JsonToListConverter().convert("not-json").isEmpty());
    }

    @Test
    void jwtConverterCoversCollectionStringSingleAndMissingRoleClaims() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        assertEquals(List.of("ROLE_ADMIN", "ROLE_USER"), converter.convert(jwt(Map.of("roles", List.of("admin", "ROLE_USER")))).block().getAuthorities().stream().map(a -> a.getAuthority()).toList());
        assertEquals(List.of("ROLE_ADMIN", "ROLE_USER"), converter.convert(jwt(Map.of("roles", "admin, ROLE_USER, "))).block().getAuthorities().stream().map(a -> a.getAuthority()).toList());
        assertEquals("ROLE_VENDOR", converter.convert(jwt(Map.of("role", "vendor"))).block().getAuthorities().iterator().next().getAuthority());
        assertTrue(converter.convert(jwt(Map.of())).block().getAuthorities().isEmpty());
    }

    private Jwt jwt(Map<String, Object> extra) {
        Map<String, Object> claims = new java.util.HashMap<>(Map.of("sub", UUID.randomUUID().toString())); claims.putAll(extra);
        return new Jwt("token", Instant.now(), Instant.now().plusSeconds(300), Map.of("alg", "HS256"), claims);
    }
}
