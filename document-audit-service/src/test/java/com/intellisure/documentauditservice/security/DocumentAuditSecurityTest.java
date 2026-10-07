package com.intellisure.documentauditservice.security;

import com.intellisure.documentauditservice.converter.JwtAuthenticationConverter;
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

@DisplayName("DocumentAuditSecurityTest")
class DocumentAuditSecurityTest {

    private final JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

    private static final Set<String> DOCUMENT_ROLES = Set.of(
            "ROLE_POLICYHOLDER",
            "ROLE_UNDERWRITER",
            "ROLE_CLAIMS_ADJUSTER",
            "ROLE_CLAIMS_MANAGER",
            "ROLE_VENDOR_MANAGER",
            "ROLE_SYSTEM_ADMINISTRATOR",
            "ROLE_ADMIN"
    );

    private static final Set<String> AUDIT_QUERY_ROLES = Set.of(
            "ROLE_SYSTEM_ADMINISTRATOR",
            "ROLE_ADMIN",
            "ROLE_CLAIMS_MANAGER",
            "ROLE_UNDERWRITER"
    );

    private Mono<Boolean> isAuthorizedForDocuments() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> {
                    var auth = ctx.getAuthentication();
                    if (auth == null || !auth.isAuthenticated()) return false;
                    return auth.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .anyMatch(DOCUMENT_ROLES::contains);
                })
                .defaultIfEmpty(false);
    }

    private Mono<Boolean> isAuthorizedToPostAuditEvent() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication() != null && ctx.getAuthentication().isAuthenticated())
                .defaultIfEmpty(false);
    }

    private Mono<Boolean> isAuthorizedForAuditRetrieval() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> {
                    var auth = ctx.getAuthentication();
                    if (auth == null || !auth.isAuthenticated()) return false;
                    return auth.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .anyMatch(AUDIT_QUERY_ROLES::contains);
                })
                .defaultIfEmpty(false);
    }

    @Test
    @DisplayName("Authorized roles are permitted for document operations")
    void authorizedRolesPermittedForDocuments() {
        for (String role : List.of("POLICYHOLDER", "UNDERWRITER", "CLAIMS_ADJUSTER", "CLAIMS_MANAGER", "VENDOR_MANAGER", "ADMIN")) {
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
                    isAuthorizedForDocuments()
                            .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(new SecurityContextImpl(auth))))
            )
            .assertNext(authorized -> assertTrue(authorized, role + " must be authorized for documents"))
            .verifyComplete();
        }
    }

    @Test
    @DisplayName("Audit event creation allows any authenticated service caller")
    void auditCreationAllowedForAuthenticatedCaller() {
        Jwt jwt = new Jwt(
                "token-svc",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "HS256"),
                Map.of("sub", "service-actor", "role", "CLAIMS_ADJUSTER")
        );
        JwtAuthenticationToken auth = converter.convert(jwt).block();
        assertNotNull(auth);

        StepVerifier.create(
                isAuthorizedToPostAuditEvent()
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(new SecurityContextImpl(auth))))
            )
            .assertNext(authorized -> assertTrue(authorized, "Authenticated caller must be authorized to post audit events"))
            .verifyComplete();
    }

    @Test
    @DisplayName("Audit event retrieval is restricted to compliance and management roles")
    void auditRetrievalRestrictedToComplianceRoles() {
        // 1. Staff role (ADMIN / CLAIMS_MANAGER / UNDERWRITER) passes
        Jwt adminJwt = new Jwt(
                "token-admin",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "HS256"),
                Map.of("sub", "admin-1", "role", "ADMIN")
        );
        JwtAuthenticationToken adminAuth = converter.convert(adminJwt).block();
        assertNotNull(adminAuth);

        StepVerifier.create(
                isAuthorizedForAuditRetrieval()
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(new SecurityContextImpl(adminAuth))))
            )
            .assertNext(authorized -> assertTrue(authorized, "ADMIN must be authorized for audit retrieval"))
            .verifyComplete();

        // 2. Policyholder is rejected
        Jwt phJwt = new Jwt(
                "token-ph",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "HS256"),
                Map.of("sub", "ph-1", "role", "POLICYHOLDER")
        );
        JwtAuthenticationToken phAuth = converter.convert(phJwt).block();
        assertNotNull(phAuth);

        StepVerifier.create(
                isAuthorizedForAuditRetrieval()
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(new SecurityContextImpl(phAuth))))
            )
            .assertNext(authorized -> assertFalse(authorized, "POLICYHOLDER must NOT be authorized for audit retrieval"))
            .verifyComplete();
    }
}
