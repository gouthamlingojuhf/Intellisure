package com.intellisure.workflownotificationservice.security;

import com.intellisure.workflownotificationservice.exception.AccessDeniedBusinessException;
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

class NotificationOwnershipSecurityTest {

    private final SecurityActorService service = new SecurityActorService();

    @Test
    void authenticatedUserCanAccessOnlyOwnNotifications() {
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        JwtAuthenticationToken authentication = authentication(userId);

        StepVerifier.create(service.assertUserAccess(userId)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(
                                Mono.just(new SecurityContextImpl(authentication)))))
                .verifyComplete();

        StepVerifier.create(service.assertUserAccess(otherUserId)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(
                                Mono.just(new SecurityContextImpl(authentication)))))
                .expectError(AccessDeniedBusinessException.class)
                .verify();
    }

    @Test
    void policyholderCanCreateOnlyOwnNotifications() {
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        JwtAuthenticationToken authentication = authentication(userId);

        StepVerifier.create(service.assertNotificationCreationAccess(userId)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(
                                Mono.just(new SecurityContextImpl(authentication)))))
                .verifyComplete();

        StepVerifier.create(service.assertNotificationCreationAccess(otherUserId)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(
                                Mono.just(new SecurityContextImpl(authentication)))))
                .expectError(AccessDeniedBusinessException.class)
                .verify();
    }

    @Test
    void staffCanCreateNotificationForAnotherRecipient() {
        UUID staffUserId = UUID.randomUUID();
        UUID recipientUserId = UUID.randomUUID();
        JwtAuthenticationToken authentication = authentication(
                staffUserId,
                "ROLE_CLAIMS_MANAGER"
        );

        StepVerifier.create(service.assertNotificationCreationAccess(recipientUserId)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(
                                Mono.just(new SecurityContextImpl(authentication)))))
                .verifyComplete();
    }

    private JwtAuthenticationToken authentication(UUID userId) {
        return authentication(userId, "ROLE_POLICYHOLDER");
    }

    private JwtAuthenticationToken authentication(UUID userId, String role) {
        Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(300),
                Map.of("alg", "HS256"), Map.of("sub", userId.toString(), "role", role));
        return new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority(role)), userId.toString());
    }
}
