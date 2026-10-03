package com.intellisure.riskunderwritingservice.security;

import com.intellisure.riskunderwritingservice.exception.AccessDeniedBusinessException;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class SecurityActorService {

    public Mono<UUID> currentUserId() {
        return currentToken()
                .flatMap(token -> {
                    String subject = token.getToken().getSubject();

                    if (subject == null || subject.isBlank()) {
                        return Mono.error(
                                new AccessDeniedBusinessException(
                                        "JWT subject is required"
                                )
                        );
                    }

                    try {
                        return Mono.just(
                                UUID.fromString(subject)
                        );
                    } catch (IllegalArgumentException exception) {
                        return Mono.error(
                                new AccessDeniedBusinessException(
                                        "JWT subject is not a valid UUID"
                                )
                        );
                    }
                });
    }

    public Mono<Boolean> hasRole(String role) {
        String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;

        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication()
                        .getAuthorities()
                        .stream()
                        .anyMatch(grantedAuthority ->
                                authority.equals(grantedAuthority.getAuthority())
                        )
                )
                .defaultIfEmpty(false);
    }

    private Mono<JwtAuthenticationToken> currentToken() {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication())
                .filter(authentication -> authentication instanceof JwtAuthenticationToken)
                .cast(JwtAuthenticationToken.class)
                .switchIfEmpty(
                        Mono.error(
                                new AccessDeniedBusinessException(
                                        "Authenticated JWT identity is required"
                                )
                        )
                );
    }
}
