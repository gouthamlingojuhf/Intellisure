package com.intellisure.apigateway.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthenticationConverter
        implements Converter<Jwt,
        Mono<? extends JwtAuthenticationToken>> {

    @Override
    public Mono<? extends JwtAuthenticationToken> convert(
            Jwt jwt) {

        String role = jwt.getClaimAsString("role");

        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority(
                        "ROLE_" + role
                );

        JwtAuthenticationToken authentication =
                new JwtAuthenticationToken(
                        jwt,
                        List.of(authority)
                );

        return Mono.just(authentication);
    }
}
