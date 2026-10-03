package com.intellisure.riskunderwritingservice.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Component
public class JwtAuthenticationConverter
        implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    @Override
    public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
        List<GrantedAuthority> authorities = extractAuthorities(jwt);

        AbstractAuthenticationToken authentication =
                new JwtAuthenticationToken(
                        jwt,
                        authorities,
                        jwt.getSubject()
                );

        return Mono.just(authentication);
    }

    private List<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Object rolesClaim = jwt.getClaims().get("roles");

        /*
         * Preferred JWT format:
         *
         * "roles": [
         *     "UNDERWRITER"
         * ]
         */
        if (rolesClaim instanceof Collection<?> roles) {
            return roles.stream()
                    .map(Object::toString)
                    .map(this::normalizeRole)
                    .map(SimpleGrantedAuthority::new)
                    .map(authority -> (GrantedAuthority) authority)
                    .toList();
        }

        /*
         * Also support:
         *
         * "roles": "UNDERWRITER,RISK_ENGINEER"
         */
        if (rolesClaim instanceof String rolesStr && !rolesStr.isBlank()) {
            return Arrays.stream(rolesStr.split(","))
                    .map(String::trim)
                    .filter(role -> !role.isEmpty())
                    .map(this::normalizeRole)
                    .map(SimpleGrantedAuthority::new)
                    .map(authority -> (GrantedAuthority) authority)
                    .toList();
        }

        return List.of();
    }

    private String normalizeRole(String role) {
        String cleaned = role.toUpperCase();
        if (!cleaned.startsWith("ROLE_")) {
            return "ROLE_" + cleaned;
        }
        return cleaned;
    }
}
