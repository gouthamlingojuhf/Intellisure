package com.intellisure.claimsservice.security;

import com.intellisure.claimsservice.converter.JwtAuthenticationConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RoleAuthorizationSecurityTest")
class RoleAuthorizationSecurityTest {

    private final JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    private final SecurityActorService securityActorService = new SecurityActorService();

    @Test
    @DisplayName("Valid JWT with single role claim converts to ROLE_ authority")
    void validJwtWithSingleRoleConvertsToAuthority() {
        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "HS256"),
                Map.of("sub", "user-123", "role", "POLICYHOLDER")
        );

        StepVerifier.create(converter.convert(jwt))
                .assertNext(auth -> {
                    assertNotNull(auth);
                    assertTrue(auth.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .anyMatch(a -> a.equals("ROLE_POLICYHOLDER")));
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Valid JWT with multiple roles list converts to all ROLE_ authorities")
    void validJwtWithMultipleRolesConvertsToAuthorities() {
        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "HS256"),
                Map.of("sub", "staff-123", "roles", List.of("CLAIMS_ADJUSTER", "CLAIMS_MANAGER"))
        );

        StepVerifier.create(converter.convert(jwt))
                .assertNext(auth -> {
                    assertNotNull(auth);
                    List<String> authorities = auth.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .toList();
                    assertTrue(authorities.contains("ROLE_CLAIMS_ADJUSTER"));
                    assertTrue(authorities.contains("ROLE_CLAIMS_MANAGER"));
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Valid JWT with wrong role fails staff role authorization check (403 condition)")
    void validJwtWithWrongRoleFailsStaffAuthorization() {
        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "HS256"),
                Map.of("sub", "policyholder-123", "role", "POLICYHOLDER")
        );

        JwtAuthenticationToken auth = converter.convert(jwt).block();

        StepVerifier.create(
                securityActorService.hasAnyRole("CLAIMS_ADJUSTER", "CLAIMS_MANAGER", "ADMIN")
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(reactor.core.publisher.Mono.just(new SecurityContextImpl(auth))))
        )
        .assertNext(isStaff -> assertFalse(isStaff, "Policyholder must NOT have adjuster/manager staff role"))
        .verifyComplete();
    }

    @Test
    @DisplayName("Valid JWT with correct role passes staff role authorization check (allowed condition)")
    void validJwtWithCorrectRolePassesStaffAuthorization() {
        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "HS256"),
                Map.of("sub", "adjuster-123", "role", "CLAIMS_ADJUSTER")
        );

        JwtAuthenticationToken auth = converter.convert(jwt).block();

        StepVerifier.create(
                securityActorService.hasAnyRole("CLAIMS_ADJUSTER", "CLAIMS_MANAGER", "ADMIN")
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(reactor.core.publisher.Mono.just(new SecurityContextImpl(auth))))
        )
        .assertNext(isStaff -> assertTrue(isStaff, "Adjuster MUST have staff role"))
        .verifyComplete();
    }
}
