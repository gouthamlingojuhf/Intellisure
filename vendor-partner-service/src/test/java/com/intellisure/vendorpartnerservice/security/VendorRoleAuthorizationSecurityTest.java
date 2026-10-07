package com.intellisure.vendorpartnerservice.security;

import com.intellisure.vendorpartnerservice.converter.JwtAuthenticationConverter;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("VendorRoleAuthorizationSecurityTest")
class VendorRoleAuthorizationSecurityTest {

    private final JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

    private static final Set<String> ASSIGNMENT_ROLES = Set.of(
            "ROLE_VENDOR_MANAGER",
            "ROLE_CLAIMS_ADJUSTER",
            "ROLE_CLAIMS_MANAGER",
            "ROLE_SYSTEM_ADMINISTRATOR",
            "ROLE_ADMIN"
    );

    private Mono<Boolean> isAuthorizedForAssignments() {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> {
                    var auth = context.getAuthentication();
                    if (auth == null) return false;
                    return auth.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .anyMatch(ASSIGNMENT_ROLES::contains);
                })
                .defaultIfEmpty(false);
    }

    @Test
    @DisplayName("Unauthorized role (POLICYHOLDER / UNDERWRITER) is rejected for vendor assignment operations")
    void unauthorizedRoleRejectedForVendorAssignments() {
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
                isAuthorizedForAssignments()
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(new SecurityContextImpl(auth))))
        )
        .assertNext(authorized -> assertFalse(authorized, "POLICYHOLDER role must be rejected for vendor assignments"))
        .verifyComplete();
    }

    @Test
    @DisplayName("Underwriter role is rejected for vendor assignment operations")
    void underwriterRoleRejectedForVendorAssignments() {
        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "HS256"),
                Map.of("sub", "underwriter-123", "role", "UNDERWRITER")
        );

        JwtAuthenticationToken auth = converter.convert(jwt).block();
        assertNotNull(auth);

        StepVerifier.create(
                isAuthorizedForAssignments()
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(new SecurityContextImpl(auth))))
        )
        .assertNext(authorized -> assertFalse(authorized, "UNDERWRITER role must be rejected for vendor assignments"))
        .verifyComplete();
    }

    @Test
    @DisplayName("Authorized role (VENDOR_MANAGER / CLAIMS_ADJUSTER) is allowed for vendor assignment operations")
    void authorizedRolesAllowedForVendorAssignments() {
        // 1. VENDOR_MANAGER
        Jwt jwtVm = new Jwt(
                "token-vm",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "HS256"),
                Map.of("sub", "vm-123", "role", "VENDOR_MANAGER")
        );
        JwtAuthenticationToken authVm = converter.convert(jwtVm).block();
        assertNotNull(authVm);

        StepVerifier.create(
                isAuthorizedForAssignments()
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(new SecurityContextImpl(authVm))))
        )
        .assertNext(authorized -> assertTrue(authorized, "VENDOR_MANAGER must be authorized"))
        .verifyComplete();

        // 2. CLAIMS_ADJUSTER
        Jwt jwtAdj = new Jwt(
                "token-adj",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "HS256"),
                Map.of("sub", "adj-123", "role", "CLAIMS_ADJUSTER")
        );
        JwtAuthenticationToken authAdj = converter.convert(jwtAdj).block();
        assertNotNull(authAdj);

        StepVerifier.create(
                isAuthorizedForAssignments()
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(new SecurityContextImpl(authAdj))))
        )
        .assertNext(authorized -> assertTrue(authorized, "CLAIMS_ADJUSTER must be authorized"))
        .verifyComplete();
    }
}
