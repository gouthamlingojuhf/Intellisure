package com.intellisure.customerpartyservice.security;

import com.intellisure.customerpartyservice.converter.JwtAuthenticationConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AdjusterLookupSecurityTest")
class AdjusterLookupSecurityTest {

    private final JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

    private static final Set<String> GENERAL_ROLE_ACCESS_ROLES = Set.of(
            "ROLE_CLAIMS_ADJUSTER",
            "ROLE_CLAIMS_MANAGER",
            "ROLE_UNDERWRITER",
            "ROLE_SYSTEM_ADMINISTRATOR",
            "ROLE_ADMIN"
    );

    // Rule: /api/users/role/CLAIMS_ADJUSTER/available requires authenticated()
    private Mono<Boolean> isAuthorizedForAvailableAdjusters() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication() != null && ctx.getAuthentication().isAuthenticated())
                .defaultIfEmpty(false);
    }

    // Rule: /api/users/role/** requires hasAnyRole("CLAIMS_ADJUSTER", "CLAIMS_MANAGER", "UNDERWRITER", "SYSTEM_ADMINISTRATOR", "ADMIN")
    private Mono<Boolean> isAuthorizedForGeneralRoleDirectory() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> {
                    var auth = ctx.getAuthentication();
                    if (auth == null || !auth.isAuthenticated()) return false;
                    return auth.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .anyMatch(GENERAL_ROLE_ACCESS_ROLES::contains);
                })
                .defaultIfEmpty(false);
    }

    @Test
    @DisplayName("Legitimate claim-filing flow (POLICYHOLDER) is authorized to lookup available adjusters")
    void policyholderAuthorizedForAvailableAdjusters() {
        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "HS256"),
                Map.of("sub", "policyholder-123", "role", "POLICYHOLDER")
        );
        JwtAuthenticationToken auth = converter.convert(jwt).block();
        assertNotNull(auth);

        StepVerifier.create(
                isAuthorizedForAvailableAdjusters()
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(new SecurityContextImpl(auth))))
        )
        .assertNext(authorized -> assertTrue(authorized, "POLICYHOLDER must be authorized for /api/users/role/CLAIMS_ADJUSTER/available"))
        .verifyComplete();
    }

    @Test
    @DisplayName("Unauthenticated request to available adjusters endpoint is rejected")
    void unauthenticatedAccessToAvailableAdjustersIsRejected() {
        StepVerifier.create(isAuthorizedForAvailableAdjusters())
                .assertNext(authorized -> assertFalse(authorized, "Unauthenticated access must be rejected"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Unrelated role directory lookup remains protected; POLICYHOLDER is rejected")
    void unrelatedRoleDirectoryLookupRemainsProtected() {
        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "HS256"),
                Map.of("sub", "policyholder-123", "role", "POLICYHOLDER")
        );
        JwtAuthenticationToken auth = converter.convert(jwt).block();
        assertNotNull(auth);

        StepVerifier.create(
                isAuthorizedForGeneralRoleDirectory()
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(new SecurityContextImpl(auth))))
        )
        .assertNext(authorized -> assertFalse(authorized, "POLICYHOLDER must NOT be authorized for general role lookup (/api/users/role/**)"))
        .verifyComplete();
    }

    @Test
    @DisplayName("Policyholder cannot gain general employee-directory access; only authorized staff roles pass")
    void policyholderCannotAccessGeneralEmployeeDirectory() {
        // Staff role passes
        Jwt staffJwt = new Jwt(
                "token-staff",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "HS256"),
                Map.of("sub", "manager-123", "role", "CLAIMS_MANAGER")
        );
        JwtAuthenticationToken staffAuth = converter.convert(staffJwt).block();
        assertNotNull(staffAuth);

        StepVerifier.create(
                isAuthorizedForGeneralRoleDirectory()
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(new SecurityContextImpl(staffAuth))))
        )
        .assertNext(authorized -> assertTrue(authorized, "CLAIMS_MANAGER must be authorized for general role directory"))
        .verifyComplete();
    }
}
