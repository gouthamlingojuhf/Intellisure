package com.intellisure.customerpartyservice.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtAuthenticationConverterTest {

    private JwtAuthenticationConverter converter;

    @BeforeEach
    void setUp() {
        converter =
                new JwtAuthenticationConverter();
    }

    @Test
    void shouldConvertRolesListToAuthorities() {
        UUID userId = UUID.randomUUID();

        Jwt jwt = jwt(
                userId,
                Map.of(
                        "roles",
                        List.of(
                                "POLICYHOLDER",
                                "ADMIN"
                        )
                )
        );

        StepVerifier.create(
                        converter.convert(jwt)
                )
                .assertNext(authentication -> {
                    assertEquals(
                            userId.toString(),
                            authentication.getName()
                    );

                    assertTrue(
                            authentication
                                    .getAuthorities()
                                    .stream()
                                    .anyMatch(authority ->
                                            authority
                                                    .getAuthority()
                                                    .equals(
                                                            "ROLE_POLICYHOLDER"
                                                    )
                                    )
                    );

                    assertTrue(
                            authentication
                                    .getAuthorities()
                                    .stream()
                                    .anyMatch(authority ->
                                            authority
                                                    .getAuthority()
                                                    .equals(
                                                            "ROLE_ADMIN"
                                                    )
                                    )
                    );
                })
                .verifyComplete();
    }

    @Test
    void shouldSupportSingularRoleClaim() {
        Jwt jwt = jwt(
                UUID.randomUUID(),
                Map.of(
                        "role",
                        "UNDERWRITER"
                )
        );

        StepVerifier.create(
                        converter.convert(jwt)
                )
                .assertNext(authentication ->
                        assertTrue(
                                authentication
                                        .getAuthorities()
                                        .stream()
                                        .anyMatch(authority ->
                                                authority
                                                        .getAuthority()
                                                        .equals(
                                                                "ROLE_UNDERWRITER"
                                                        )
                                        )
                        )
                )
                .verifyComplete();
    }

    @Test
    void shouldNotDuplicateRolePrefix() {
        Jwt jwt = jwt(
                UUID.randomUUID(),
                Map.of(
                        "roles",
                        List.of("ROLE_ADMIN")
                )
        );

        StepVerifier.create(
                        converter.convert(jwt)
                )
                .assertNext(authentication ->
                        assertTrue(
                                authentication
                                        .getAuthorities()
                                        .stream()
                                        .anyMatch(authority ->
                                                authority
                                                        .getAuthority()
                                                        .equals(
                                                                "ROLE_ADMIN"
                                                        )
                                        )
                        )
                )
                .verifyComplete();
    }

    private Jwt jwt(
            UUID userId,
            Map<String, Object> additionalClaims
    ) {
        Instant now = Instant.now();

        Jwt.Builder builder =
                Jwt.withTokenValue("test-token")
                        .header("alg", "HS256")
                        .subject(userId.toString())
                        .issuedAt(now)
                        .expiresAt(now.plusSeconds(3600));

        additionalClaims.forEach(
                builder::claim
        );

        return builder.build();
    }
}