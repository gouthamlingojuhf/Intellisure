package com.intellisure.recoveryservice.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder()
                .filter(bearerTokenPropagationFilter());
    }

    public static ExchangeFilterFunction bearerTokenPropagationFilter() {
        return (request, next) -> {
            if (request.headers().getFirst(HttpHeaders.AUTHORIZATION) != null) {
                return next.exchange(request);
            }
            return ReactiveSecurityContextHolder.getContext()
                    .map(SecurityContext::getAuthentication)
                    .filter(JwtAuthenticationToken.class::isInstance)
                    .cast(JwtAuthenticationToken.class)
                    .map(jwtAuth -> ClientRequest.from(request)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtAuth.getToken().getTokenValue())
                            .build())
                    .defaultIfEmpty(request)
                    .flatMap(next::exchange);
        };
    }
}
