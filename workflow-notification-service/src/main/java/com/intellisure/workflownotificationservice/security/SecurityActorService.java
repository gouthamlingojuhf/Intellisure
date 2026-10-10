package com.intellisure.workflownotificationservice.security;

import com.intellisure.workflownotificationservice.exception.AccessDeniedBusinessException;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class SecurityActorService {

    public Mono<UUID> currentUserId() {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication())
                .filter(JwtAuthenticationToken.class::isInstance)
                .cast(JwtAuthenticationToken.class)
                .map(token -> parseUuid(token.getToken().getSubject()))
                .switchIfEmpty(Mono.error(new AccessDeniedBusinessException(
                        "No authenticated JWT token present in security context")));
    }

    public Mono<Void> assertUserAccess(UUID userId) {
        return currentUserId()
                .filter(userId::equals)
                .switchIfEmpty(Mono.error(new AccessDeniedBusinessException(
                        "You are not authorized to access these notifications")))
                .then();
    }

    public Mono<Void> assertNotificationCreationAccess(UUID recipientUserId) {
        if (recipientUserId == null) {
            return Mono.error(new AccessDeniedBusinessException(
                    "Notification recipient is required"
            ));
        }

        return hasAnyRole(
                "UNDERWRITER",
                "RISK_ENGINEER",
                "CLAIMS_ADJUSTER",
                "CLAIMS_MANAGER",
                "VENDOR_MANAGER",
                "SYSTEM_ADMINISTRATOR",
                "ADMIN",
                "SYSTEM"
        ).flatMap(isStaff -> isStaff
                ? Mono.empty()
                : assertUserAccess(recipientUserId));
    }

    public Mono<String> currentRole() {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> {
                    var auth = context.getAuthentication();
                    if (auth == null || auth.getAuthorities().isEmpty()) return "USER";
                    return auth.getAuthorities().iterator().next().getAuthority().replaceFirst("^ROLE_", "");
                })
                .defaultIfEmpty("USER");
    }

    public Mono<UUID> currentCustomerId() {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication())
                .filter(JwtAuthenticationToken.class::isInstance)
                .cast(JwtAuthenticationToken.class)
                .flatMap(token -> {
                    String custId = token.getToken().getClaimAsString("customerId");
                    if (custId == null || custId.isBlank()) return Mono.empty();
                    try {
                        return Mono.just(UUID.fromString(custId));
                    } catch (Exception e) {
                        return Mono.empty();
                    }
                });
    }

    public Mono<String> currentDisplayName() {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication())
                .filter(JwtAuthenticationToken.class::isInstance)
                .cast(JwtAuthenticationToken.class)
                .map(token -> {
                    String name = token.getToken().getClaimAsString("displayName");
                    if (name != null && !name.isBlank()) return name;
                    String email = token.getToken().getClaimAsString("email");
                    if (email != null && !email.isBlank()) return email;
                    return token.getToken().getSubject();
                })
                .defaultIfEmpty("User");
    }

    public Mono<Boolean> hasAnyRole(String... roles) {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> {
                    var authentication = context.getAuthentication();
                    if (authentication == null) return false;
                    for (String role : roles) {
                        String expected = role.startsWith("ROLE_")
                                ? role
                                : "ROLE_" + role;
                        if (authentication.getAuthorities().stream()
                                .anyMatch(authority -> expected.equals(authority.getAuthority()))) {
                            return true;
                        }
                    }
                    return false;
                })
                .defaultIfEmpty(false);
    }

    private UUID parseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            throw new AccessDeniedBusinessException("Authenticated user ID is invalid");
        }
    }
}
