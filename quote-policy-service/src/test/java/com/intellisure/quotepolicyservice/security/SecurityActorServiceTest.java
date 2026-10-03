package com.intellisure.quotepolicyservice.security;

import com.intellisure.quotepolicyservice.exception.AccessDeniedBusinessException;
import com.intellisure.quotepolicyservice.testsupport.TestAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("SecurityActorService")
class SecurityActorServiceTest {

    private final SecurityActorService service = new SecurityActorService();

    private static JwtAuthenticationToken jwtToken(
            Map<String, Object> claims
    ) {
        Jwt.Builder builder = Jwt.withTokenValue("token")
                .header("alg", "none");

        claims.forEach(builder::claim);

        return new JwtAuthenticationToken(builder.build());
    }

    @Nested
    @DisplayName("currentUserId")
    class CurrentUserId {

        @Test
        @DisplayName("returns the JWT subject")
        void returnsSubject() {
            UUID subject = UUID.randomUUID();

            StepVerifier.create(
                            service.currentUserId().contextWrite(
                                    ReactiveSecurityContextHolder
                                            .withAuthentication(
                                                    jwtToken(
                                                            Map.of(
                                                                    "sub",
                                                                    subject
                                                                    .toString()
                                                            )
                                                    )
                                            )
                            )
                    )
                    .expectNext(subject)
                    .verifyComplete();
        }

        @Test
        @DisplayName("fails when there is no authentication")
        void failsWithoutAuthentication() {
            StepVerifier.create(service.currentUserId())
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            AccessDeniedBusinessException.class,
                            "Authenticated JWT identity is required",
                            error
                    ))
                    .verify();
        }

        @Test
        @DisplayName("fails when the authentication is not a JWT")
        void failsForNonJwtAuthentication() {
            TestingAuthenticationToken authentication =
                    new TestingAuthenticationToken(
                            "user",
                            "password"
                    );

            StepVerifier.create(
                            service.currentUserId().contextWrite(
                                    ReactiveSecurityContextHolder
                                            .withAuthentication(
                                                    authentication
                                            )
                            )
                    )
                    .expectError(AccessDeniedBusinessException.class)
                    .verify();
        }

        @Test
        @DisplayName("fails when the subject is not a UUID")
        void failsForNonUuidSubject() {
            StepVerifier.create(
                            service.currentUserId().contextWrite(
                                    ReactiveSecurityContextHolder
                                            .withAuthentication(
                                                    jwtToken(
                                                            Map.of(
                                                                    "sub",
                                                                    "not-a-uuid"
                                                            )
                                                    )
                                            )
                            )
                    )
                    .expectError(IllegalArgumentException.class)
                    .verify();
        }
    }

    @Nested
    @DisplayName("currentCustomerId")
    class CurrentCustomerId {

        @Test
        @DisplayName("returns the customerId claim")
        void returnsCustomerId() {
            UUID customerId = UUID.randomUUID();

            StepVerifier.create(
                            service.currentCustomerId().contextWrite(
                                    ReactiveSecurityContextHolder
                                            .withAuthentication(
                                                    jwtToken(
                                                            Map.of(
                                                                    "sub",
                                                                    UUID
                                                                            .randomUUID()
                                                                            .toString(),
                                                                    "customerId",
                                                                    customerId
                                                                            .toString()
                                                            )
                                                    )
                                            )
                            )
                    )
                    .expectNext(customerId)
                    .verifyComplete();
        }

        @Test
        @DisplayName("fails when the customerId claim is missing")
        void failsWhenClaimMissing() {
            StepVerifier.create(
                            service.currentCustomerId().contextWrite(
                                    ReactiveSecurityContextHolder
                                            .withAuthentication(
                                                    jwtToken(
                                                            Map.of(
                                                                    "sub",
                                                                    UUID
                                                                            .randomUUID()
                                                                            .toString()
                                                            )
                                                    )
                                            )
                            )
                    )
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            AccessDeniedBusinessException.class,
                            "A business customer profile is required "
                                    + "before creating a quote",
                            error
                    ))
                    .verify();
        }

        @Test
        @DisplayName("fails when the customerId claim is blank")
        void failsWhenClaimBlank() {
            StepVerifier.create(
                            service.currentCustomerId().contextWrite(
                                    ReactiveSecurityContextHolder
                                            .withAuthentication(
                                                    jwtToken(
                                                            Map.of(
                                                                    "sub",
                                                                    UUID
                                                                            .randomUUID()
                                                                            .toString(),
                                                                    "customerId",
                                                                    "   "
                                                            )
                                                    )
                                            )
                            )
                    )
                    .expectError(AccessDeniedBusinessException.class)
                    .verify();
        }

        @Test
        @DisplayName("fails when the customerId claim is not a UUID")
        void failsWhenClaimMalformed() {
            StepVerifier.create(
                            service.currentCustomerId().contextWrite(
                                    ReactiveSecurityContextHolder
                                            .withAuthentication(
                                                    jwtToken(
                                                            Map.of(
                                                                    "sub",
                                                                    UUID
                                                                            .randomUUID()
                                                                            .toString(),
                                                                    "customerId",
                                                                    "not-a-uuid"
                                                            )
                                                    )
                                            )
                            )
                    )
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            AccessDeniedBusinessException.class,
                            "The authenticated token contains an "
                                    + "invalid customerId",
                            error
                    ))
                    .verify();
        }

        @Test
        @DisplayName("fails when there is no authentication")
        void failsWithoutAuthentication() {
            StepVerifier.create(service.currentCustomerId())
                    .expectError(AccessDeniedBusinessException.class)
                    .verify();
        }
    }

    @Nested
    @DisplayName("hasRole")
    class HasRole {

        @Test
        @DisplayName("returns true for a granted role")
        void returnsTrue() {
            TestingAuthenticationToken authentication =
                    new TestingAuthenticationToken(
                            "user",
                            "password",
                            List.of(
                                    new SimpleGrantedAuthority(
                                            "ROLE_UNDERWRITER"
                                    )
                            )
                    );

            StepVerifier.create(
                            service.hasRole("UNDERWRITER").contextWrite(
                                    ReactiveSecurityContextHolder
                                            .withAuthentication(
                                                    authentication
                                            )
                            )
                    )
                    .expectNext(true)
                    .verifyComplete();
        }

        @Test
        @DisplayName("returns false for a role that was not granted")
        void returnsFalse() {
            TestingAuthenticationToken authentication =
                    new TestingAuthenticationToken(
                            "user",
                            "password",
                            List.of(
                                    new SimpleGrantedAuthority("ROLE_ADMIN")
                            )
                    );

            StepVerifier.create(
                            service.hasRole("UNDERWRITER").contextWrite(
                                    ReactiveSecurityContextHolder
                                            .withAuthentication(
                                                    authentication
                                            )
                            )
                    )
                    .expectNext(false)
                    .verifyComplete();
        }

        @Test
        @DisplayName("accepts an already prefixed role name")
        void acceptsPrefixedRole() {
            TestingAuthenticationToken authentication =
                    new TestingAuthenticationToken(
                            "user",
                            "password",
                            List.of(
                                    new SimpleGrantedAuthority("ROLE_ADMIN")
                            )
                    );

            StepVerifier.create(
                            service.hasRole("ROLE_ADMIN").contextWrite(
                                    ReactiveSecurityContextHolder
                                            .withAuthentication(
                                                    authentication
                                            )
                            )
                    )
                    .expectNext(true)
                    .verifyComplete();
        }

        @Test
        @DisplayName("returns false when there is no security context")
        void returnsFalseWithoutContext() {
            StepVerifier.create(service.hasRole("ADMIN"))
                    .expectNext(false)
                    .verifyComplete();
        }

        @Test
        @DisplayName("returns false when the authentication has no authorities")
        void returnsFalseWithoutAuthorities() {
            TestingAuthenticationToken authentication =
                    new TestingAuthenticationToken(
                            "user",
                            "password"
                    );

            StepVerifier.create(
                            service.hasRole("ADMIN").contextWrite(
                                    ReactiveSecurityContextHolder
                                            .withAuthentication(
                                                    authentication
                                            )
                            )
                    )
                    .expectNext(false)
                    .verifyComplete();
        }

        @Test
        @DisplayName("reads the authorities of a JWT authentication token")
        void readsJwtAuthorities() {
            JwtAuthenticationToken token = new JwtAuthenticationToken(
                    Jwt.withTokenValue("token")
                            .header("alg", "none")
                            .subject(UUID.randomUUID().toString())
                            .build(),
                    List.of(
                            new SimpleGrantedAuthority("ROLE_POLICYHOLDER")
                    )
            );

            StepVerifier.create(
                            service.hasRole("POLICYHOLDER").contextWrite(
                                    ReactiveSecurityContextHolder
                                            .withAuthentication(token)
                            )
                    )
                    .expectNext(true)
                    .verifyComplete();

            StepVerifier.create(
                            service.hasRole("ADMIN").contextWrite(
                                    ReactiveSecurityContextHolder
                                            .withAuthentication(token)
                            )
                    )
                    .expectNext(false)
                    .verifyComplete();
        }
    }

    @Test
    @DisplayName("role matching is case sensitive for the ROLE_ prefix only")
    void rolePrefixHandling() {
        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken(
                        "user",
                        "password",
                        List.of(new SimpleGrantedAuthority("role_admin"))
                );

        StepVerifier.create(
                        service.hasRole("ADMIN").contextWrite(
                                ReactiveSecurityContextHolder
                                        .withAuthentication(
                                                authentication
                                        )
                        )
                )
                .expectNext(false)
                .verifyComplete();

        assertTrue(
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority -> authority.getAuthority()
                                .equals("role_admin"))
        );
        assertEquals(
                1,
                authentication.getAuthorities().size()
        );
    }
}
