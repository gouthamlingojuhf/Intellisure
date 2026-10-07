package com.intellisure.claimsservice.security;

import com.intellisure.claimsservice.entity.Claim;
import com.intellisure.claimsservice.exception.AccessDeniedBusinessException;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class SecurityActorService {

    public Mono<UUID> currentUserId() {
        return currentToken()
                .map(token -> UUID.fromString(token.getToken().getSubject()));
    }

    public Mono<UUID> currentCustomerId() {
        return currentToken()
                .map(token -> {
                    String customerId = token.getToken().getClaimAsString("customerId");
                    if (customerId != null && !customerId.isBlank()) {
                        try {
                            return UUID.fromString(customerId);
                        } catch (IllegalArgumentException ignored) {}
                    }
                    return UUID.fromString(token.getToken().getSubject());
                });
    }

    public Mono<Boolean> hasAnyRole(String... roles) {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> {
                    var auth = context.getAuthentication();
                    if (auth == null) return false;
                    for (String role : roles) {
                        String expected = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(expected))) {
                            return true;
                        }
                    }
                    return false;
                })
                .defaultIfEmpty(false);
    }

    public Mono<Void> assertClaimAccess(Claim claim) {
        return hasAnyRole("CLAIMS_ADJUSTER", "CLAIMS_MANAGER", "SYSTEM_ADMINISTRATOR", "ADMIN")
                .flatMap(isStaff -> {
                    if (Boolean.TRUE.equals(isStaff)) {
                        return Mono.empty();
                    }
                    return currentCustomerId()
                            .flatMap(customerId -> {
                                if (claim.getCustomerId() != null && claim.getCustomerId().equals(customerId)) {
                                    return Mono.empty();
                                }
                                return currentUserId()
                                        .flatMap(userId -> {
                                            if (claim.getCustomerId() != null && claim.getCustomerId().equals(userId)) {
                                                return Mono.empty();
                                            }
                                            return Mono.error(new AccessDeniedBusinessException(
                                                    "You are not authorized to access claim " + claim.getClaimId()));
                                        });
                            });
                });
    }

    private Mono<JwtAuthenticationToken> currentToken() {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication())
                .filter(JwtAuthenticationToken.class::isInstance)
                .cast(JwtAuthenticationToken.class)
                .switchIfEmpty(Mono.error(new AccessDeniedBusinessException(
                        "No authenticated JWT token present in security context")));
    }
}
