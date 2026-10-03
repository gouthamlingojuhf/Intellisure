package com.intellisure.apigateway.config;

import com.intellisure.apigateway.converter.JwtAuthenticationConverter;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import javax.crypto.SecretKey;

@Configuration
public class SecurityConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    /*
     * Same secret used by Customer & Party Service
     * to sign JWTs.
     */
    @Bean
    public SecretKey jwtSecretKey() {

        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(jwtSecret)
        );
    }

    /*
     * Gateway validates JWT signatures.
     */
    @Bean
    public ReactiveJwtDecoder jwtDecoder(
            SecretKey jwtSecretKey) {

        return NimbusReactiveJwtDecoder
                .withSecretKey(jwtSecretKey)
                .build();
    }

    /*
     * Gateway security rules.
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter) {

        return http

                .csrf(
                        ServerHttpSecurity.CsrfSpec::disable
                )

                .authorizeExchange(exchange -> exchange

                        /*
                         * Public endpoints
                         */
                        .pathMatchers(
                                "/actuator/health",
                                "/actuator/info"
                        ).permitAll()

                        .pathMatchers(
                                "/api/auth/register",
                                "/api/auth/login"
                        ).permitAll()


                        /*
                         * ==============================
                         * CUSTOMER / POLICYHOLDER
                         * ==============================
                         */

                        // Policyholder creates a quote
                        .pathMatchers(
                                HttpMethod.POST,
                                "/api/quotes"
                        )
                        .hasAnyRole("POLICYHOLDER", "USER")


                        // Policyholder views quotes
                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/quotes/**"
                        )
                        .hasAnyRole("POLICYHOLDER", "USER", "UNDERWRITER")


                        /*
                         * ==============================
                         * UNDERWRITER
                         * ==============================
                         */

                        .pathMatchers(
                                "/api/underwriting/**"
                        )
                        .hasRole("UNDERWRITER")


                        /*
                         * ==============================
                         * RISK ENGINEER
                         * ==============================
                         */

                        .pathMatchers(
                                "/api/risks/**"
                        )
                        .hasRole("RISK_ENGINEER")


                        /*
                         * ==============================
                         * CLAIMS
                         * ==============================
                         */

                        .pathMatchers(
                                "/api/claims/**"
                        )
                        .hasAnyRole(
                                "CLAIMS_ADJUSTER",
                                "CLAIMS_MANAGER"
                        )


                        /*
                         * ==============================
                         * VENDORS
                         * ==============================
                         */

                        .pathMatchers(
                                "/api/vendors/**",
                                "/api/vendor-assignments/**",
                                "/api/partners/**"
                        )
                        .hasAnyRole(
                                "VENDOR_MANAGER",
                                "SYSTEM_ADMINISTRATOR"
                        )


                        /*
                         * ==============================
                         * RECOVERY
                         * ==============================
                         */

                        .pathMatchers(
                                "/api/recovery/**",
                                "/api/business-continuity/**"
                        )
                        .authenticated()


                        /*
                         * ==============================
                         * WORKFLOW
                         * ==============================
                         */

                        .pathMatchers(
                                "/api/workflows/**",
                                "/api/notifications/**"
                        )
                        .authenticated()


                        /*
                         * ==============================
                         * DOCUMENT / AUDIT
                         * ==============================
                         */

                        .pathMatchers(
                                "/api/documents/**",
                                "/api/audit/**",
                                "/api/audit-events/**"
                        )
                        .authenticated()


                        /*
                         * ==============================
                         * ANALYTICS
                         * ==============================
                         */

                        .pathMatchers(
                                "/api/analytics/**",
                                "/api/intelligence/**",
                                "/api/risks/**",
                                "/api/risk-assessments/**"
                        )
                        .hasAnyRole(
                                "SYSTEM_ADMINISTRATOR",
                                "UNDERWRITER",
                                "CLAIMS_MANAGER",
                                "RISK_ENGINEER"
                        )


                        /*
                         * Customer endpoints
                         */
                        .pathMatchers(
                                "/api/users/**"
                        )
                        .authenticated()


                        /*
                         * Anything else
                         */
                        .anyExchange()
                        .authenticated()

                )


                /*
                 * JWT Bearer authentication.
                 */
                .oauth2ResourceServer(
                        oauth2 -> oauth2
                                .jwt(
                                        jwt -> jwt
                                                .jwtAuthenticationConverter(
                                                        jwtAuthenticationConverter
                                                )
                                )
                )

                .build();
    }
}
