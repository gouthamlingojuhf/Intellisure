package com.intellisure.documentauditservice.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class SecurityActorServiceTest {
    private final SecurityActorService service = new SecurityActorService();

    @Test
    void readsCurrentUserIdFromJwtSubject() {
        UUID userId = UUID.randomUUID();
        StepVerifier.create(service.currentUserId().contextWrite(context("CLAIMS_MANAGER", userId.toString(), null)))
                .expectNext(userId).verifyComplete();
    }

    @Test
    void rejectsInvalidOrMissingUserIdentity() {
        StepVerifier.create(service.currentUserId().contextWrite(context("ADMIN", "not-a-uuid", null)))
                .expectErrorMessage("Authenticated identity is invalid").verify();
        StepVerifier.create(service.currentUserId())
                .expectErrorMessage("Authenticated user ID is unavailable").verify();
    }

    @Test
    void readsCustomerIdOnlyFromJwtAuthentication() {
        UUID customerId = UUID.randomUUID();
        StepVerifier.create(service.currentCustomerId().contextWrite(
                        context("POLICYHOLDER", UUID.randomUUID().toString(), customerId.toString())))
                .expectNext(customerId).verifyComplete();
        StepVerifier.create(service.currentCustomerId().contextWrite(
                        context("POLICYHOLDER", UUID.randomUUID().toString(), "invalid")))
                .expectErrorMessage("Authenticated identity is invalid").verify();
        StepVerifier.create(service.currentCustomerId().contextWrite(
                        context("POLICYHOLDER", UUID.randomUUID().toString(), "   ")))
                .expectErrorMessage("Authenticated customer ID is unavailable").verify();
    }

    @Test
    void distinguishesStaffAndNonStaffRoles() {
        StepVerifier.create(service.hasAnyDocumentStaffRole().contextWrite(
                        context("VENDOR_MANAGER", UUID.randomUUID().toString(), null)))
                .expectNext(true).verifyComplete();
        StepVerifier.create(service.hasAnyDocumentStaffRole().contextWrite(
                        context("POLICYHOLDER", UUID.randomUUID().toString(), null)))
                .expectNext(false).verifyComplete();
        StepVerifier.create(service.hasAnyDocumentStaffRole())
                .expectNext(false).verifyComplete();
        TestingAuthenticationToken unauthenticated = new TestingAuthenticationToken("actor", "credentials");
        unauthenticated.setAuthenticated(false);
        StepVerifier.create(service.hasAnyDocumentStaffRole().contextWrite(
                        ReactiveSecurityContextHolder.withAuthentication(unauthenticated)))
                .expectNext(false).verifyComplete();
    }

    private reactor.util.context.Context context(String role, String subject, String customerId) {
        Map<String, Object> claims = customerId == null
                ? Map.of("sub", subject, "role", role)
                : Map.of("sub", subject, "role", role, "customerId", customerId);
        Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(300), Map.of("alg", "HS256"), claims);
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt,
                List.of(new SimpleGrantedAuthority("ROLE_" + role)), subject);
        return ReactiveSecurityContextHolder.withSecurityContext(
                Mono.just(new SecurityContextImpl(authentication)));
    }
}
