package com.intellisure.vendorpartnerservice.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;

@Component
public class JwtAuthenticationConverter implements Converter<Jwt, Mono<? extends JwtAuthenticationToken>> {

    @Override
    public Mono<? extends JwtAuthenticationToken> convert(Jwt jwt) {
        List<GrantedAuthority> authorities = extractAuthorities(jwt);
        return Mono.just(new JwtAuthenticationToken(jwt, authorities, jwt.getSubject()));
    }

    private List<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Object rolesClaim = jwt.getClaims().get("roles");

        if (rolesClaim instanceof Collection<?> roles) {
            return roles.stream()
                    .map(Object::toString)
                    .map(this::normalizeRole)
                    .map(SimpleGrantedAuthority::new)
                    .map(authority -> (GrantedAuthority) authority)
                    .toList();
        }

        if (rolesClaim instanceof String rolesText) {
            return List.of(rolesText.split(","))
                    .stream()
                    .map(String::trim)
                    .filter(role -> !role.isBlank())
                    .map(this::normalizeRole)
                    .map(SimpleGrantedAuthority::new)
                    .map(authority -> (GrantedAuthority) authority)
                    .toList();
        }

        String singleRole = jwt.getClaimAsString("role");
        if (singleRole == null || singleRole.isBlank()) {
            return List.of();
        }

        return List.of(new SimpleGrantedAuthority(normalizeRole(singleRole)));
    }

    private String normalizeRole(String role) {
        String normalized = role.trim().toUpperCase();
        if (normalized.startsWith("ROLE_")) {
            return normalized;
        }
        return "ROLE_" + normalized;
    }
}
