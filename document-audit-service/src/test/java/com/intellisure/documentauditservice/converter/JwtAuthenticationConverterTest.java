package com.intellisure.documentauditservice.converter;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationConverterTest {
    private final JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

    @Test
    void convertsCollectionAndDelimitedRoles() {
        JwtAuthenticationToken collection = converter.convert(jwt(Map.of(
                "roles", List.of("POLICYHOLDER", "ROLE_ADMIN")))).block();
        JwtAuthenticationToken delimited = converter.convert(jwt(Map.of(
                "roles", "underwriter, ROLE_ADMIN,  "))).block();

        assertThat(collection.getAuthorities()).extracting("authority")
                .containsExactly("ROLE_POLICYHOLDER", "ROLE_ADMIN");
        assertThat(delimited.getAuthorities()).extracting("authority")
                .containsExactly("ROLE_UNDERWRITER", "ROLE_ADMIN");
    }

    @Test
    void supportsSingleRoleAndNoRole() {
        JwtAuthenticationToken single = converter.convert(jwt(Map.of("role", " claims_manager "))).block();
        JwtAuthenticationToken missing = converter.convert(jwt(Map.of("sub", "actor"))).block();
        JwtAuthenticationToken blank = converter.convert(jwt(Map.of("role", "   "))).block();

        assertThat(single.getAuthorities()).extracting("authority").containsExactly("ROLE_CLAIMS_MANAGER");
        assertThat(missing.getAuthorities()).isEmpty();
        assertThat(blank.getAuthorities()).isEmpty();
    }

    private Jwt jwt(Map<String, Object> claims) {
        return new Jwt("token", Instant.now(), Instant.now().plusSeconds(300), Map.of("alg", "HS256"), claims);
    }
}
