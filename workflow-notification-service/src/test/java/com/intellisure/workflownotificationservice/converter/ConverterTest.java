package com.intellisure.workflownotificationservice.converter;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ConverterTest {

    @Test
    void uuidConvertersRoundTripAndRejectInvalidBytes() {
        UUID source = UUID.randomUUID();
        UuidToBytesConverter writer = new UuidToBytesConverter();
        BytesToUuidConverter reader = new BytesToUuidConverter();
        assertEquals(source, reader.convert(writer.convert(source)));
        assertNull(writer.convert(null));
        assertNull(reader.convert(null));
        assertNull(reader.convert(new byte[15]));
    }

    @Test
    void jwtConverterNormalizesCollectionStringAndSingleRoleClaims() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        Jwt collection = jwt(Map.of("roles", List.of("admin", "ROLE_USER")));
        assertEquals(List.of("ROLE_ADMIN", "ROLE_USER"), converter.convert(collection).block()
                .getAuthorities().stream().map(a -> a.getAuthority()).toList());

        Jwt text = jwt(Map.of("roles", "admin, ROLE_USER, "));
        assertEquals(List.of("ROLE_ADMIN", "ROLE_USER"), converter.convert(text).block()
                .getAuthorities().stream().map(a -> a.getAuthority()).toList());

        Jwt single = jwt(Map.of("role", "operator"));
        assertEquals("ROLE_OPERATOR", converter.convert(single).block().getAuthorities().iterator().next().getAuthority());
        assertTrue(converter.convert(jwt(Map.of())).block().getAuthorities().isEmpty());
    }

    private Jwt jwt(Map<String, Object> claims) {
        return new Jwt("token", Instant.now(), Instant.now().plusSeconds(300), Map.of("alg", "HS256"),
                new java.util.HashMap<>(Map.of("sub", UUID.randomUUID().toString())) {{ putAll(claims); }});
    }
}
