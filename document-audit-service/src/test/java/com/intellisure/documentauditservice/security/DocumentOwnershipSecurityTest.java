package com.intellisure.documentauditservice.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentOwnershipSecurityTest {

    private final SecurityActorService actorService = new SecurityActorService();

    @Test
    void policyholderCustomerIdComesFromJwt() {
        String customerId = "11111111-1111-1111-1111-111111111111";

        StepVerifier.create(actorService.currentCustomerId()
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(
                                Mono.just(new SecurityContextImpl(token("POLICYHOLDER", customerId))))))
                .assertNext(id -> assertEquals(customerId, id.toString()))
                .verifyComplete();
    }

    @Test
    void documentStaffRoleIsRecognized() {
        StepVerifier.create(actorService.hasAnyDocumentStaffRole()
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(
                                Mono.just(new SecurityContextImpl(token("CLAIMS_ADJUSTER", null))))))
                .assertNext(value -> assertTrue(value))
                .verifyComplete();
    }

    @Test
    void missingCustomerClaimIsRejected() {
        StepVerifier.create(actorService.currentCustomerId()
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(
                                Mono.just(new SecurityContextImpl(token("POLICYHOLDER", null))))))
                .expectErrorMessage("Authenticated customer ID is unavailable")
                .verify();
    }

    @Test
    void policyholderCanAccessAnEntityOwnedByTheJwtCustomer() {
        String customerId = "11111111-1111-1111-1111-111111111111";
        DocumentSecurityService securityService = new DocumentSecurityService(
                actorService, (entityId, entityType) -> Mono.just(java.util.UUID.fromString(customerId)));

        StepVerifier.create(securityService.assertEntityAccess(
                        java.util.UUID.randomUUID(), "POLICY")
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(
                                Mono.just(new SecurityContextImpl(token("POLICYHOLDER", customerId))))))
                .verifyComplete();
    }

    @Test
    void policyholderCannotAccessAnEntityOwnedByAnotherCustomer() {
        String customerId = "11111111-1111-1111-1111-111111111111";
        String otherCustomerId = "33333333-3333-3333-3333-333333333333";
        DocumentSecurityService securityService = new DocumentSecurityService(
                actorService, (entityId, entityType) -> Mono.just(java.util.UUID.fromString(otherCustomerId)));

        StepVerifier.create(securityService.assertEntityAccess(
                        java.util.UUID.randomUUID(), "CLAIM")
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(
                                Mono.just(new SecurityContextImpl(token("POLICYHOLDER", customerId))))))
                .expectErrorMessage("You are not authorized to access this document entity")
                .verify();
    }

    private static JwtAuthenticationToken token(String role, String customerId) {
        Map<String, Object> claims = customerId == null
                ? Map.of("sub", "22222222-2222-2222-2222-222222222222", "role", role)
                : Map.of("sub", "22222222-2222-2222-2222-222222222222", "role", role, "customerId", customerId);
        Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(300), Map.of("alg", "HS256"), claims);
        return new JwtAuthenticationToken(jwt,
                List.of(new SimpleGrantedAuthority("ROLE_" + role)), jwt.getSubject());
    }
}
