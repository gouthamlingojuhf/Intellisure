package com.intellisure.analyticsintelligenceservice.converter;

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
    void convertsCollectionRoles() {
        JwtAuthenticationToken authentication = converter.convert(jwt(Map.of(
                "roles", List.of("POLICYHOLDER", "ROLE_ADMIN")))).block();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getAuthorities()).extracting("authority")
                .containsExactly("ROLE_POLICYHOLDER", "ROLE_ADMIN");
    }

    @Test
    void parsesCommaSeparatedRolesAndIgnoresBlanks() {
        JwtAuthenticationToken authentication = converter.convert(jwt(Map.of(
                "roles", "underwriter, ROLE_ADMIN,   "))).block();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getAuthorities()).extracting("authority")
                .containsExactly("ROLE_UNDERWRITER", "ROLE_ADMIN");
    }

    @Test
    void supportsSingleRoleAndMissingRole() {
        JwtAuthenticationToken single = converter.convert(jwt(Map.of("role", " risk_engineer "))).block();
        JwtAuthenticationToken missing = converter.convert(jwt(Map.of("sub", "user"))).block();
        assertThat(single.getAuthorities()).extracting("authority").containsExactly("ROLE_RISK_ENGINEER");
        assertThat(missing.getAuthorities()).isEmpty();
    }

    private Jwt jwt(Map<String, Object> claims) {
        return new Jwt("token", Instant.now(), Instant.now().plusSeconds(300), Map.of("alg", "HS256"), claims);
    }
}
