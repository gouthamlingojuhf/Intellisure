package com.intellisure.apigateway.converter;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationConverterTest {

    private final JwtAuthenticationConverter converter =
            new JwtAuthenticationConverter();

    @Test
    void convertsRoleClaimToGrantedAuthority() {
        Jwt jwt = jwtWithRole("POLICYHOLDER");

        JwtAuthenticationToken authentication = converter.convert(jwt).block();

        assertThat(authentication).isNotNull();
        assertThat(authentication.getToken()).isSameAs(jwt);
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_POLICYHOLDER");
    }

    @Test
    void preservesJwtWhenRoleClaimIsMissing() {
        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "HS256"),
                Map.of()
        );

        JwtAuthenticationToken authentication = converter.convert(jwt).block();

        assertThat(authentication).isNotNull();
        assertThat(authentication.getToken()).isSameAs(jwt);
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_null");
    }

    private Jwt jwtWithRole(String role) {
        return new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "HS256"),
                Map.of("role", role)
        );
    }
}
