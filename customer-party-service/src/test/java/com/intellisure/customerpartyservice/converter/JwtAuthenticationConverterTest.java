package com.intellisure.customerpartyservice.converter;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationConverterTest {

    private final JwtAuthenticationConverter converter =
            new JwtAuthenticationConverter();

    @Test
    void convertsRoleClaimToGrantedAuthority() {
        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "HS256"),
                Map.of("role", "UNDERWRITER")
        );

        StepVerifier.create(converter.convert(jwt))
                .assertNext(authentication -> {
                    assertThat(authentication.getToken()).isSameAs(jwt);
                    assertThat(authentication.getAuthorities())
                            .extracting("authority")
                            .containsExactly("ROLE_UNDERWRITER");
                })
                .verifyComplete();
    }
}
