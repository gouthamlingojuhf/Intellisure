package com.intellisure.riskunderwritingservice.config;

import com.intellisure.riskunderwritingservice.converter.JwtAuthenticationConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity(useAuthorizationManager = true)
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter
    ) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .authorizeExchange(exchange -> exchange

                        /*
                         * Public operational and API documentation.
                         */
                        .pathMatchers(
                                "/actuator/health",
                                "/actuator/health/**",
                                "/actuator/info",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/webjars/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**"
                        )
                        .permitAll()

                        /*
                         * Quote Service consumes this result.
                         *
                         * SYSTEM is intended for future
                         * service-to-service tokens.
                         */
                        .pathMatchers(
                                "/api/v1/quotes/*/underwriting-result"
                        )
                        .hasAnyRole(
                                "UNDERWRITER",
                                "ADMIN",
                                "SYSTEM"
                        )

                        /*
                         * Rules governance is administrative.
                         */
                        .pathMatchers(
                                "/api/v1/risk-rules/**"
                        )
                        .hasRole("ADMIN")

                        /*
                         * Referrals are handled by underwriting.
                         */
                        .pathMatchers(
                                "/api/v1/referrals/**"
                        )
                        .hasAnyRole(
                                "UNDERWRITER",
                                "ADMIN"
                        )

                        /*
                         * Subjectivity evidence can be reviewed by
                         * underwriting and risk engineering.
                         */
                        .pathMatchers(
                                "/api/v1/subjectivities/**"
                        )
                        .hasAnyRole(
                                "UNDERWRITER",
                                "RISK_ENGINEER",
                                "ADMIN"
                        )

                        /*
                         * Assessment APIs are further protected
                         * through method-level authorization.
                         */
                        .pathMatchers(
                                "/api/v1/risk-assessments/**"
                        )
                        .hasAnyRole(
                                "UNDERWRITER",
                                "RISK_ENGINEER",
                                "ADMIN",
                                "CLAIMS_ADJUSTER",
                                "CLAIMS_MANAGER"
                        )

                        .pathMatchers("/api/**")
                        .authenticated()

                        .anyExchange()
                        .authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter)
                        )
                )
                .build();
    }

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder(
            @Value("${jwt.secret}") String encodedSecret
    ) {
        byte[] secretBytes = Base64.getDecoder().decode(encodedSecret);

        SecretKey secretKey = new SecretKeySpec(
                secretBytes,
                "HmacSHA256"
        );

        return NimbusReactiveJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }
}
