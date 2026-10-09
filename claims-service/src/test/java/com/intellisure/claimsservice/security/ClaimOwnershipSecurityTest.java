package com.intellisure.claimsservice.security;

import com.intellisure.claimsservice.entity.Claim;
import com.intellisure.claimsservice.exception.AccessDeniedBusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@DisplayName("ClaimOwnershipSecurityTest")
class ClaimOwnershipSecurityTest {

    private final SecurityActorService securityActorService = new SecurityActorService();

    @Test
    @DisplayName("Policyholder cannot access another policyholder's claim (IDOR defense)")
    void policyholderCannotAccessAnotherPolicyholdersClaim() {
        UUID ownerCustomerId = UUID.randomUUID();
        UUID attackerCustomerId = UUID.randomUUID();

        Claim claim = Claim.builder()
                .claimId(UUID.randomUUID())
                .customerId(ownerCustomerId)
                .status("OPEN")
                .build();

        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "HS256"),
                Map.of("sub", attackerCustomerId.toString(), "customerId", attackerCustomerId.toString(), "role", "POLICYHOLDER")
        );
        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_POLICYHOLDER")), attackerCustomerId.toString());

        StepVerifier.create(
                securityActorService.assertClaimAccess(claim)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(reactor.core.publisher.Mono.just(new SecurityContextImpl(auth))))
        )
        .expectError(AccessDeniedBusinessException.class)
        .verify();
    }

    @Test
    @DisplayName("Policyholder can access their own claim")
    void policyholderCanAccessOwnClaim() {
        UUID ownerCustomerId = UUID.randomUUID();

        Claim claim = Claim.builder()
                .claimId(UUID.randomUUID())
                .customerId(ownerCustomerId)
                .status("OPEN")
                .build();

        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "HS256"),
                Map.of("sub", ownerCustomerId.toString(), "customerId", ownerCustomerId.toString(), "role", "POLICYHOLDER")
        );
        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_POLICYHOLDER")), ownerCustomerId.toString());

        StepVerifier.create(
                securityActorService.assertClaimAccess(claim)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(reactor.core.publisher.Mono.just(new SecurityContextImpl(auth))))
        )
        .verifyComplete();
    }

    @Test
    @DisplayName("Claims adjuster can access any customer's claim for processing")
    void claimsAdjusterCanAccessClaim() {
        UUID ownerCustomerId = UUID.randomUUID();
        UUID adjusterId = UUID.randomUUID();

        Claim claim = Claim.builder()
                .claimId(UUID.randomUUID())
                .customerId(ownerCustomerId)
                .status("OPEN")
                .build();

        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "HS256"),
                Map.of("sub", adjusterId.toString(), "role", "CLAIMS_ADJUSTER")
        );
        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_CLAIMS_ADJUSTER")), adjusterId.toString());

        StepVerifier.create(
                securityActorService.assertClaimAccess(claim)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(reactor.core.publisher.Mono.just(new SecurityContextImpl(auth))))
        )
        .verifyComplete();
    }

    @Test
    @DisplayName("Claims manager can access any customer's claim for oversight")
    void claimsManagerCanAccessClaim() {
        UUID ownerCustomerId = UUID.randomUUID();
        UUID managerId = UUID.randomUUID();

        Claim claim = Claim.builder()
                .claimId(UUID.randomUUID())
                .customerId(ownerCustomerId)
                .status("OPEN")
                .build();

        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "HS256"),
                Map.of("sub", managerId.toString(), "role", "CLAIMS_MANAGER")
        );
        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_CLAIMS_MANAGER")), managerId.toString());

        StepVerifier.create(
                securityActorService.assertClaimAccess(claim)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(reactor.core.publisher.Mono.just(new SecurityContextImpl(auth))))
        )
        .verifyComplete();
    }

    @Test
    @DisplayName("Admin retains administrative access to any claim")
    void adminCanAccessClaim() {
        UUID ownerCustomerId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        Claim claim = Claim.builder()
                .claimId(UUID.randomUUID())
                .customerId(ownerCustomerId)
                .status("OPEN")
                .build();

        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "HS256"),
                Map.of("sub", adminId.toString(), "role", "ADMIN")
        );
        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")), adminId.toString());

        StepVerifier.create(
                securityActorService.assertClaimAccess(claim)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(reactor.core.publisher.Mono.just(new SecurityContextImpl(auth))))
        )
        .verifyComplete();
    }

    @Test
    @DisplayName("Unauthenticated request cannot access any claim")
    void unauthenticatedRequestCannotAccessClaim() {
        Claim claim = Claim.builder()
                .claimId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .status("OPEN")
                .build();

        StepVerifier.create(securityActorService.assertClaimAccess(claim))
                .expectError(AccessDeniedBusinessException.class)
        .verify();
    }

    @Test
    @DisplayName("Customer identity falls back to subject when customerId claim is blank or malformed")
    void currentCustomerIdFallsBackToSubject() {
        UUID subject = UUID.randomUUID();
        SecurityActorService service = new SecurityActorService();
        for (String customerClaim : List.of("", "not-a-uuid")) {
            Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(300), Map.of("alg", "HS256"),
                    Map.of("sub", subject.toString(), "customerId", customerClaim));
            JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, List.of(), subject.toString());
            StepVerifier.create(service.currentCustomerId()
                            .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(reactor.core.publisher.Mono.just(new SecurityContextImpl(auth)))))
                    .expectNext(subject).verifyComplete();
        }
    }

    @Test
    @DisplayName("Claim ownership accepts a matching subject when customer id is unavailable")
    void claimOwnershipFallsBackToSubject() {
        UUID subject = UUID.randomUUID();
        Claim claim = Claim.builder().claimId(UUID.randomUUID()).customerId(subject).build();
        Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(300), Map.of("alg", "HS256"),
                Map.of("sub", subject.toString(), "customerId", UUID.randomUUID().toString(), "role", "POLICYHOLDER"));
        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_POLICYHOLDER")), subject.toString());
        StepVerifier.create(new SecurityActorService().assertClaimAccess(claim)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(reactor.core.publisher.Mono.just(new SecurityContextImpl(auth)))))
                .verifyComplete();
    }

    @Test
    void hasAnyRoleHandlesNullAuthenticationAndPrefixedRoleInput() {
        SecurityActorService service = new SecurityActorService();
        SecurityContextImpl emptyContext = new SecurityContextImpl(null);
        StepVerifier.create(service.hasAnyRole("ADMIN")
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(reactor.core.publisher.Mono.just(emptyContext))))
                .expectNext(false).verifyComplete();
        UUID admin = UUID.randomUUID();
        Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(300), Map.of("alg", "HS256"), Map.of("sub", admin.toString()));
        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")), admin.toString());
        StepVerifier.create(service.hasAnyRole("ROLE_ADMIN")
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(reactor.core.publisher.Mono.just(new SecurityContextImpl(auth)))))
                .expectNext(true).verifyComplete();
    }
}
