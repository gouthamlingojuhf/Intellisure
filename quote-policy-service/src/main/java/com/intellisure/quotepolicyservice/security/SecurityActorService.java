package com.intellisure.quotepolicyservice.security;

import com.intellisure.quotepolicyservice.exception.AccessDeniedBusinessException;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class SecurityActorService {

    public Mono<UUID> currentUserId() {
        return currentToken()
                .map(token ->
                        UUID.fromString(
                                token.getToken().getSubject()
                        )
                );
    }

    public Mono<UUID> currentCustomerId() {
        return currentToken()
                .flatMap(token -> {
                    String customerId =
                            token.getToken()
                                    .getClaimAsString(
                                            "customerId"
                                    );

                    if (customerId == null
                            || customerId.isBlank()) {
                        return Mono.error(
                                new AccessDeniedBusinessException(
                                        "A business customer profile "
                                                + "is required before "
                                                + "creating a quote"
                                )
                        );
                    }

                    try {
                        return Mono.just(
                                UUID.fromString(customerId)
                        );
                    } catch (IllegalArgumentException exception) {
                        return Mono.error(
                                new AccessDeniedBusinessException(
                                        "The authenticated token contains "
                                                + "an invalid customerId"
                                )
                        );
                    }
                });
    }

    public Mono<Boolean> hasRole(String role) {
        String authority =
                role.startsWith("ROLE_")
                        ? role
                        : "ROLE_" + role;

        return ReactiveSecurityContextHolder
                .getContext()
                .map(context ->
                        context.getAuthentication()
                                .getAuthorities()
                                .stream()
                                .anyMatch(grantedAuthority ->
                                        grantedAuthority
                                                .getAuthority()
                                                .equals(authority)
                                )
                )
                .defaultIfEmpty(false);
    }

    public Mono<Void> assertCustomerAccess(UUID customerId) {
        return hasAnyRole(
                "UNDERWRITER",
                "ADMIN",
                "SYSTEM_ADMINISTRATOR",
                "CLAIMS_ADJUSTER",
                "CLAIMS_MANAGER",
                "CLAIMS_SERVICE",
                "SYSTEM"
        ).flatMap(isStaff -> {
            if (Boolean.TRUE.equals(isStaff)) {
                return Mono.empty();
            }

            return currentCustomerId()
                    .filter(customerId::equals)
                    .switchIfEmpty(Mono.error(
                            new AccessDeniedBusinessException(
                                    "The authenticated customer does not own this resource"
                            )
                    ))
                    .then();
        });
    }

    public Mono<Boolean> hasAnyRole(String... roles) {
        return ReactiveSecurityContextHolder
                .getContext()
                .map(context -> context.getAuthentication()
                        .getAuthorities()
                        .stream()
                        .anyMatch(authority -> {
                            for (String role : roles) {
                                String expected = role.startsWith("ROLE_")
                                        ? role
                                        : "ROLE_" + role;
                                if (expected.equals(authority.getAuthority())) {
                                    return true;
                                }
                            }
                            return false;
                        }))
                .defaultIfEmpty(false);
    }

    private Mono<JwtAuthenticationToken> currentToken() {
        return ReactiveSecurityContextHolder
                .getContext()
                .map(context ->
                        context.getAuthentication()
                )
                .filter(authentication ->
                        authentication
                                instanceof JwtAuthenticationToken
                )
                .cast(JwtAuthenticationToken.class)
                .switchIfEmpty(
                        Mono.error(
                                new AccessDeniedBusinessException(
                                        "Authenticated JWT identity "
                                                + "is required"
                                )
                        )
                );
    }
}
