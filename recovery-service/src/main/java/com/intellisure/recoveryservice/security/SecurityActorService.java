package com.intellisure.recoveryservice.security;

import com.intellisure.recoveryservice.exception.AccessDeniedBusinessException;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class SecurityActorService {

    private static final String[] RECOVERY_STAFF_ROLES = {
            "CLAIMS_ADJUSTER", "CLAIMS_MANAGER", "SYSTEM_ADMINISTRATOR", "ADMIN"
    };

    public Mono<UUID> currentUserId() {
        return currentToken()
                .map(token -> parseUuid(token.getToken().getSubject(), "Authenticated user ID is invalid"));
    }

    public Mono<UUID> currentCustomerId() {
        return currentToken().map(token -> {
            String customerId = token.getToken().getClaimAsString("customerId");
            if (customerId != null && !customerId.isBlank()) {
                return parseUuid(customerId, "Authenticated customer ID is invalid");
            }
            return parseUuid(token.getToken().getSubject(), "Authenticated user ID is invalid");
        });
    }

    public Mono<Boolean> hasAnyRole(String... roles) {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> {
                    var authentication = context.getAuthentication();
                    if (authentication == null) return false;
                    for (String role : roles) {
                        String expected = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                        if (authentication.getAuthorities().stream()
                                .anyMatch(authority -> expected.equals(authority.getAuthority()))) {
                            return true;
                        }
                    }
                    return false;
                })
                .defaultIfEmpty(false);
    }

    public Mono<Void> assertCustomerAccess(UUID customerId) {
        if (customerId == null) {
            return Mono.error(new AccessDeniedBusinessException("Recovery case customer ownership is unavailable"));
        }
        return hasAnyRole(RECOVERY_STAFF_ROLES)
                .flatMap(isStaff -> {
                    if (Boolean.TRUE.equals(isStaff)) return Mono.empty();
                    return currentCustomerId()
                            .filter(customerId::equals)
                            .switchIfEmpty(Mono.error(new AccessDeniedBusinessException(
                                    "You are not authorized to access this recovery case")))
                            .then();
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

    private UUID parseUuid(String value, String message) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            throw new AccessDeniedBusinessException(message);
        }
    }
}
