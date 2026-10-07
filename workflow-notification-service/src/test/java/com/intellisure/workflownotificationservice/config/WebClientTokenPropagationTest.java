package com.intellisure.workflownotificationservice.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("WebClientTokenPropagationTest")
class WebClientTokenPropagationTest {

    private final ExchangeFilterFunction filter = WebClientConfig.bearerTokenPropagationFilter();

    @Test
    @DisplayName("Propagates authenticated Bearer token from ReactiveSecurityContext to outbound WebClient request")
    void propagatesBearerTokenFromSecurityContext() {
        AtomicReference<ClientRequest> capturedRequest = new AtomicReference<>();
        ExchangeFunction next = request -> {
            capturedRequest.set(request);
            return Mono.empty();
        };

        ClientRequest outbound = ClientRequest.create(HttpMethod.GET, URI.create("http://document-audit-service/api/audit-events"))
                .build();

        Jwt jwt = new Jwt(
                "workflow-bearer-token",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "HS256"),
                Map.of("sub", "workflow-actor-1", "role", "CLAIMS_ADJUSTER")
        );
        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_CLAIMS_ADJUSTER")), "workflow-actor-1");

        StepVerifier.create(
                filter.filter(outbound, next)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(new SecurityContextImpl(auth))))
        )
        .verifyComplete();

        assertEquals(
                "Bearer workflow-bearer-token",
                capturedRequest.get().headers().getFirst(HttpHeaders.AUTHORIZATION)
        );
    }

    @Test
    @DisplayName("Preserves existing Authorization header if explicitly set")
    void preservesExistingAuthorizationHeader() {
        AtomicReference<ClientRequest> capturedRequest = new AtomicReference<>();
        ExchangeFunction next = request -> {
            capturedRequest.set(request);
            return Mono.empty();
        };

        ClientRequest outbound = ClientRequest.create(HttpMethod.GET, URI.create("http://document-audit-service/api/audit-events"))
                .header(HttpHeaders.AUTHORIZATION, "Bearer custom-token")
                .build();

        Jwt jwt = new Jwt(
                "security-context-token",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "HS256"),
                Map.of("sub", "actor-1")
        );
        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")), "actor-1");

        StepVerifier.create(
                filter.filter(outbound, next)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(new SecurityContextImpl(auth))))
        )
        .verifyComplete();

        assertEquals(
                "Bearer custom-token",
                capturedRequest.get().headers().getFirst(HttpHeaders.AUTHORIZATION)
        );
    }

    @Test
    @DisplayName("Proceeds without Authorization header when no security context is present")
    void proceedsWhenNoSecurityContext() {
        AtomicReference<ClientRequest> capturedRequest = new AtomicReference<>();
        ExchangeFunction next = request -> {
            capturedRequest.set(request);
            return Mono.empty();
        };

        ClientRequest outbound = ClientRequest.create(HttpMethod.GET, URI.create("http://document-audit-service/api/audit-events"))
                .build();

        StepVerifier.create(filter.filter(outbound, next))
                .verifyComplete();

        assertNull(capturedRequest.get().headers().getFirst(HttpHeaders.AUTHORIZATION));
    }
}
