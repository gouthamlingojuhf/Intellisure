package com.intellisure.recoveryservice.security;

import com.intellisure.recoveryservice.exception.AccessDeniedBusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

class RecoveryOwnershipSecurityTest {

    private final SecurityActorService service = new SecurityActorService();

    @Test
    void policyholderCanAccessOnlyTheCustomerInTheJwt() {
        UUID customerId = UUID.randomUUID();
        UUID otherCustomerId = UUID.randomUUID();

        StepVerifier.create(service.assertCustomerAccess(customerId)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(
                                Mono.just(new SecurityContextImpl(policyholder(customerId))))))
                .verifyComplete();

        StepVerifier.create(service.assertCustomerAccess(otherCustomerId)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(
                                Mono.just(new SecurityContextImpl(policyholder(customerId))))))
                .expectError(AccessDeniedBusinessException.class)
                .verify();
    }

    @Test
    void claimsStaffCanAccessRecoveryCasesAcrossCustomers() {
        UUID customerId = UUID.randomUUID();
        StepVerifier.create(service.assertCustomerAccess(customerId)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(
                                Mono.just(new SecurityContextImpl(staff())))))
                .verifyComplete();
    }

    @Test
    void currentIdsPreferCustomerClaimAndRejectMissingOrMalformedTokens() {
        UUID userId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        JwtAuthenticationToken auth = token(userId, customerId, "ROLE_POLICYHOLDER");
        StepVerifier.create(service.currentUserId().contextWrite(ReactiveSecurityContextHolder.withSecurityContext(
                        Mono.just(new SecurityContextImpl(auth)))))
                .expectNext(userId).verifyComplete();
        StepVerifier.create(service.currentCustomerId().contextWrite(ReactiveSecurityContextHolder.withSecurityContext(
                        Mono.just(new SecurityContextImpl(auth)))))
                .expectNext(customerId).verifyComplete();
        StepVerifier.create(service.currentCustomerId().contextWrite(ReactiveSecurityContextHolder.withSecurityContext(
                        Mono.just(new SecurityContextImpl(token(userId, null, "ROLE_POLICYHOLDER"))))))
                .expectNext(userId).verifyComplete();
        StepVerifier.create(service.currentUserId()).expectError(AccessDeniedBusinessException.class).verify();

        Jwt invalid = new Jwt("token", Instant.now(), Instant.now().plusSeconds(300), Map.of("alg", "HS256"), Map.of("sub", "bad"));
        StepVerifier.create(service.currentUserId().contextWrite(ReactiveSecurityContextHolder.withSecurityContext(
                        Mono.just(new SecurityContextImpl(new JwtAuthenticationToken(invalid, List.of(), "bad"))))))
                .expectError(AccessDeniedBusinessException.class).verify();
    }

    @Test
    void roleChecksSupportUnprefixedRolesAndNullCustomerIsDenied() {
        UUID userId = UUID.randomUUID();
        StepVerifier.create(service.hasAnyRole("ADMIN")
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(
                                Mono.just(new SecurityContextImpl(token(userId, null, "ROLE_ADMIN"))))))
                .expectNext(true).verifyComplete();
        StepVerifier.create(service.hasAnyRole("CLAIMS_MANAGER"))
                .expectNext(false).verifyComplete();
        StepVerifier.create(service.assertCustomerAccess(null))
                .expectError(AccessDeniedBusinessException.class).verify();
    }

    private JwtAuthenticationToken policyholder(UUID customerId) {
        return token(UUID.randomUUID(), customerId, "ROLE_POLICYHOLDER");
    }

    private JwtAuthenticationToken staff() {
        return token(UUID.randomUUID(), null, "ROLE_CLAIMS_ADJUSTER");
    }

    private JwtAuthenticationToken token(UUID userId, UUID customerId, String role) {
        Map<String, Object> claims = customerId == null
                ? Map.of("sub", userId.toString(), "role", role)
                : Map.of("sub", userId.toString(), "customerId", customerId.toString(), "role", role);
        Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(300),
                Map.of("alg", "HS256"), claims);
        return new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority(role)), userId.toString());
    }
}
