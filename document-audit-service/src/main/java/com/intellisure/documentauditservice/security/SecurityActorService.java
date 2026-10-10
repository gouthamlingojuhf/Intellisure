package com.intellisure.documentauditservice.security;

import com.intellisure.documentauditservice.exception.AccessDeniedBusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class SecurityActorService {

    private static final String[] DOCUMENT_STAFF_ROLES = {
            "UNDERWRITER",
            "CLAIMS_ADJUSTER",
            "CLAIMS_MANAGER",
            "VENDOR_MANAGER",
            "RISK_ENGINEER",
            "SYSTEM_ADMINISTRATOR",
            "ADMIN"
    };

    public Mono<UUID> currentUserId() {
        return authentication()
                .map(Authentication::getName)
                .flatMap(this::parseUuid)
                .switchIfEmpty(Mono.error(new AccessDeniedBusinessException("Authenticated user ID is unavailable")));
    }

    public Mono<UUID> currentCustomerId() {
        return jwtAuthentication()
                .flatMap(auth -> {
                    String customerId = auth.getToken().getClaimAsString("customerId");
                    return customerId == null || customerId.isBlank()
                            ? Mono.empty()
                            : Mono.just(customerId);
                })
                .flatMap(this::parseUuid)
                .switchIfEmpty(Mono.error(new AccessDeniedBusinessException("Authenticated customer ID is unavailable")));
    }

    public Mono<Boolean> hasAnyDocumentStaffRole() {
        return authentication()
                .map(authentication -> authentication.getAuthorities().stream()
                        .map(authority -> authority.getAuthority().replaceFirst("^ROLE_", ""))
                        .anyMatch(this::isDocumentStaffRole))
                .defaultIfEmpty(false);
    }

    private boolean isDocumentStaffRole(String role) {
        for (String staffRole : DOCUMENT_STAFF_ROLES) {
            if (staffRole.equals(role)) {
                return true;
            }
        }
        return false;
    }

    private Mono<Authentication> authentication() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(authentication -> authentication != null && authentication.isAuthenticated());
    }

    private Mono<JwtAuthenticationToken> jwtAuthentication() {
        return authentication()
                .filter(JwtAuthenticationToken.class::isInstance)
                .cast(JwtAuthenticationToken.class);
    }

    private Mono<UUID> parseUuid(String value) {
        try {
            return Mono.just(UUID.fromString(value));
        } catch (IllegalArgumentException ex) {
            return Mono.error(new AccessDeniedBusinessException("Authenticated identity is invalid"));
        }
    }
}
