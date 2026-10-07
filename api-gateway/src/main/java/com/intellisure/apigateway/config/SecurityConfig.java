package com.intellisure.apigateway.config;

import com.intellisure.apigateway.converter.JwtAuthenticationConverter;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:4200");
        config.addAllowedOrigin("http://localhost:4201");
        config.addAllowedOrigin("http://localhost:4202");
        config.addAllowedOrigin("http://localhost:4203");
        config.addAllowedOrigin("http://localhost:4205");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

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

                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .csrf(
                        ServerHttpSecurity.CsrfSpec::disable
                )
                .formLogin(
                        ServerHttpSecurity.FormLoginSpec::disable
                )
                .httpBasic(
                        ServerHttpSecurity.HttpBasicSpec::disable
                )

                .authorizeExchange(exchange -> exchange

                        /*
                         * Public endpoints
                         */
                        .matchers(ex -> org.springframework.web.cors.reactive.CorsUtils.isPreFlightRequest(ex.getRequest())
                                ? org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher.MatchResult.match()
                                : org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher.MatchResult.notMatch()
                        ).permitAll()
                        .pathMatchers(
                                HttpMethod.OPTIONS
                        ).permitAll()
                        .pathMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()

                        .pathMatchers(
                                "/actuator/health",
                                "/actuator/health/**",
                                "/actuator/info",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/webjars/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**"
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

                        .pathMatchers(
                                "/api/quotes/**"
                        )
                        .hasAnyRole("POLICYHOLDER", "UNDERWRITER", "SYSTEM_ADMINISTRATOR", "ADMIN")

                        .pathMatchers(
                                "/api/policies/**"
                        )
                        .hasAnyRole("POLICYHOLDER", "UNDERWRITER", "CLAIMS_ADJUSTER", "CLAIMS_MANAGER", "SYSTEM_ADMINISTRATOR", "ADMIN")


                        /*
                         * ==============================
                         * UNDERWRITER / RISK ENGINEER
                         * ==============================
                         */

                        .pathMatchers(
                                "/api/underwriting/**",
                                "/api/risk-assessments/**",
                                "/api/referrals/**",
                                "/api/subjectivities/**",
                                "/api/decisions/**",
                                "/api/risk-rules/**"
                        )
                        .hasAnyRole("UNDERWRITER", "RISK_ENGINEER", "SYSTEM_ADMINISTRATOR", "ADMIN")


                        /*
                         * ==============================
                         * CLAIMS
                         * ==============================
                         */

                        .pathMatchers(
                                HttpMethod.POST,
                                "/api/claims"
                        )
                        .hasAnyRole(
                                "POLICYHOLDER",
                                "USER",
                                "CLAIMS_ADJUSTER",
                                "CLAIMS_MANAGER"
                        )

                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/claims",
                                "/api/claims/*"
                        )
                        .hasAnyRole(
                                "POLICYHOLDER",
                                "USER",
                                "CLAIMS_ADJUSTER",
                                "CLAIMS_MANAGER"
                        )

                        .pathMatchers(
                                "/api/claims/**"
                        )
                        .hasAnyRole(
                                "CLAIMS_ADJUSTER",
                                "CLAIMS_MANAGER",
                                "SYSTEM_ADMINISTRATOR",
                                "ADMIN"
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
                                "SYSTEM_ADMINISTRATOR",
                                "ADMIN"
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
                                "ADMIN",
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
