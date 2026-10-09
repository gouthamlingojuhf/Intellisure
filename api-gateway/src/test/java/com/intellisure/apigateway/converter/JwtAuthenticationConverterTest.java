package com.intellisure.apigateway.converter;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
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
                Map.of("sub", "test-user-id")
        );

        JwtAuthenticationToken authentication = converter.convert(jwt).block();

        assertThat(authentication).isNotNull();
        assertThat(authentication.getToken()).isSameAs(jwt);
        assertThat(authentication.getAuthorities()).isEmpty();
    }

    @Test
    void convertsCollectionRoleClaimAndNormalizesEachRole() {
        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "HS256"),
                Map.of("roles", List.of("policyholder", "ROLE_ADMIN"))
        );

        JwtAuthenticationToken authentication = converter.convert(jwt).block();

        assertThat(authentication).isNotNull();
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_POLICYHOLDER", "ROLE_ADMIN");
    }

    @Test
    void trimsAndNormalizesSingleRoleClaim() {
        Jwt jwt = jwtWithRole("  role_underwriter  ");

        JwtAuthenticationToken authentication = converter.convert(jwt).block();

        assertThat(authentication).isNotNull();
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_UNDERWRITER");
    }

    @Test
    void ignoresBlankSingleRoleClaim() {
        Jwt jwt = jwtWithRole("   ");

        JwtAuthenticationToken authentication = converter.convert(jwt).block();

        assertThat(authentication).isNotNull();
        assertThat(authentication.getAuthorities()).isEmpty();
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
