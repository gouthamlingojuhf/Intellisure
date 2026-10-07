package com.intellisure.workflownotificationservice.security;

import com.intellisure.workflownotificationservice.converter.JwtAuthenticationConverter;
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

@DisplayName("WorkflowSecurityTest")
class WorkflowSecurityTest {

    private final JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

    private static final Set<String> WORKFLOW_ROLES = Set.of(
            "ROLE_UNDERWRITER",
            "ROLE_RISK_ENGINEER",
            "ROLE_CLAIMS_ADJUSTER",
            "ROLE_CLAIMS_MANAGER",
            "ROLE_VENDOR_MANAGER",
            "ROLE_SYSTEM_ADMINISTRATOR",
            "ROLE_ADMIN"
    );

    private Mono<Boolean> isAuthorizedForWorkflow() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> {
                    var auth = ctx.getAuthentication();
                    if (auth == null || !auth.isAuthenticated()) return false;
                    return auth.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .anyMatch(WORKFLOW_ROLES::contains);
                })
                .defaultIfEmpty(false);
    }

    private Mono<Boolean> isAuthorizedForNotifications() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication() != null && ctx.getAuthentication().isAuthenticated())
                .defaultIfEmpty(false);
    }

    @Test
    @DisplayName("Authorized staff roles are permitted for workflow operations")
    void authorizedStaffRolesPermittedForWorkflow() {
        for (String role : List.of("UNDERWRITER", "CLAIMS_ADJUSTER", "CLAIMS_MANAGER", "VENDOR_MANAGER", "ADMIN")) {
            Jwt jwt = new Jwt(
                    "token-" + role,
                    Instant.now(),
                    Instant.now().plusSeconds(300),
                    Map.of("alg", "HS256"),
                    Map.of("sub", "user-" + role, "role", role)
            );
            JwtAuthenticationToken auth = converter.convert(jwt).block();
            assertNotNull(auth);

            StepVerifier.create(
                    isAuthorizedForWorkflow()
                            .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(new SecurityContextImpl(auth))))
            )
            .assertNext(authorized -> assertTrue(authorized, role + " must be authorized for workflow operations"))
            .verifyComplete();
        }
    }

    @Test
    @DisplayName("POLICYHOLDER role is rejected for workflow operations")
    void policyholderRoleRejectedForWorkflow() {
        Jwt jwt = new Jwt(
                "token-ph",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "HS256"),
                Map.of("sub", "ph-123", "role", "POLICYHOLDER")
        );
        JwtAuthenticationToken auth = converter.convert(jwt).block();
        assertNotNull(auth);

        StepVerifier.create(
                isAuthorizedForWorkflow()
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(new SecurityContextImpl(auth))))
        )
        .assertNext(authorized -> assertFalse(authorized, "POLICYHOLDER must NOT be authorized for workflow operations"))
        .verifyComplete();
    }

    @Test
    @DisplayName("Unauthenticated request is rejected for workflow operations")
    void unauthenticatedRejectedForWorkflow() {
        StepVerifier.create(isAuthorizedForWorkflow())
                .assertNext(authorized -> assertFalse(authorized, "Unauthenticated access must be rejected"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Authenticated users including POLICYHOLDER are permitted for notifications")
    void authenticatedUserPermittedForNotifications() {
        Jwt jwt = new Jwt(
                "token-ph",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "HS256"),
                Map.of("sub", "ph-123", "role", "POLICYHOLDER")
        );
        JwtAuthenticationToken auth = converter.convert(jwt).block();
        assertNotNull(auth);

        StepVerifier.create(
                isAuthorizedForNotifications()
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(new SecurityContextImpl(auth))))
        )
        .assertNext(authorized -> assertTrue(authorized, "Authenticated POLICYHOLDER must be authorized for notifications"))
        .verifyComplete();
    }
}
