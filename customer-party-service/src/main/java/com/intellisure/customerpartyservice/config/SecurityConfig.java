package com.intellisure.customerpartyservice.config;

import com.intellisure.customerpartyservice.converter.JwtAuthenticationConverter;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import javax.crypto.SecretKey;

@Configuration
public class SecurityConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    /*
     * Creates the SecretKey used by both:
     * 1. JwtService -> signing JWTs
     * 2. ReactiveJwtDecoder -> validating JWTs
     */
    @Bean
    public SecretKey jwtSecretKey() {

        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(jwtSecret)
        );
    }

    /*
     * Reactive JWT decoder used by Spring Security
     * to validate incoming Bearer tokens.
     */
    @Bean
    public ReactiveJwtDecoder jwtDecoder(
            SecretKey jwtSecretKey) {

        return NimbusReactiveJwtDecoder
                .withSecretKey(jwtSecretKey)
                .build();
    }

    /*
     * BCrypt password encoder used for
     * password hashing and verification.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    /*
     * Spring Security WebFlux configuration.
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter) {

        return http

                /*
                 * REST API is stateless.
                 * CSRF is not required for our Bearer-token API.
                 */
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                .authorizeExchange(exchange -> exchange

                        /*
                         * Public endpoints.
                         */
                        .pathMatchers(
                                "/api/auth/register",
                                "/api/auth/login"
                        ).permitAll()

                        /*
                         * Only POLICYHOLDER can access
                         * the current-user endpoint.
                         */
                        .pathMatchers("/api/users/me")
                        .hasRole("POLICYHOLDER")

                        /*
                         * Every other endpoint requires
                         * a valid authenticated JWT.
                         */
                        .anyExchange()
                        .authenticated()
                )

                /*
                 * Configure JWT Bearer authentication.
                 */
                .oauth2ResourceServer(
                        oauth2 -> oauth2
                                .jwt(jwt -> jwt
                                        .jwtAuthenticationConverter(
                                                jwtAuthenticationConverter
                                        )
                                )
                )

                .build();
    }
}
