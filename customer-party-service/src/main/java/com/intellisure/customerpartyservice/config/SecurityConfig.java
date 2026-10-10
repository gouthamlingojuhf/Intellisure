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

    @Bean
    public SecretKey jwtSecretKey() {
        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(
                        jwtSecret
                )
        );
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder(
            SecretKey jwtSecretKey
    ) {
        return NimbusReactiveJwtDecoder
                .withSecretKey(jwtSecretKey)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityWebFilterChain
    securityWebFilterChain(
            ServerHttpSecurity http,
            JwtAuthenticationConverter
                    jwtAuthenticationConverter
    ) {
        return http
                .csrf(
                        ServerHttpSecurity
                                .CsrfSpec
                                ::disable
                )
                .formLogin(
                        ServerHttpSecurity
                                .FormLoginSpec
                                ::disable
                )
                .httpBasic(
                        ServerHttpSecurity
                                .HttpBasicSpec
                                ::disable
                )
                .authorizeExchange(exchange ->
                        exchange
                                .pathMatchers(
                                        "/api/auth/register",
                                        "/api/auth/login",
                                        "/actuator/health",
                                        "/actuator/health/**",
                                        "/swagger-ui.html",
                                        "/swagger-ui/**",
                                        "/v3/api-docs",
                                        "/v3/api-docs/**"
                                )
                                .permitAll()
                                .pathMatchers("/api/admin/**")
                                .hasAnyRole("SYSTEM_ADMINISTRATOR", "ADMIN")
                                .pathMatchers("/api/users/available", "/api/users/role/*/available", "/api/users/role/CLAIMS_ADJUSTER/available")
                                .authenticated()
                                .pathMatchers("/api/users/role/**")
                                .hasAnyRole("CLAIMS_ADJUSTER", "CLAIMS_MANAGER", "UNDERWRITER", "RISK_ENGINEER", "VENDOR_MANAGER", "SYSTEM_ADMINISTRATOR", "ADMIN")
                                .anyExchange()
                                .authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(
                                        jwtAuthenticationConverter
                                )
                        )
                )
                .build();
    }
}