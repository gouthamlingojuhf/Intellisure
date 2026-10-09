package com.intellisure.riskunderwritingservice;

import com.intellisure.riskunderwritingservice.converter.*;
import com.intellisure.riskunderwritingservice.exception.*;
import com.intellisure.riskunderwritingservice.filter.CorrelationIdFilter;
import com.intellisure.riskunderwritingservice.security.SecurityActorService;
import com.intellisure.riskunderwritingservice.config.WebClientConfig;
import com.intellisure.riskunderwritingservice.logging.CorrelationConstants;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class RiskInfrastructureCoverageTest {
    @Test
    void convertersAndJwtAuthoritiesHandleSupportedForms() {
        UUID id = UUID.randomUUID();
        assertEquals(id, new StringToUuidConverter().convert(id.toString()));
        assertEquals(id.toString(), new UuidToStringConverter().convert(id));
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        StepVerifier.create(converter.convert(jwt(Map.of("roles", List.of("UNDERWRITER", "ROLE_ADMIN")))))
                .assertNext(token -> assertEquals(2, token.getAuthorities().size())).verifyComplete();
        StepVerifier.create(converter.convert(jwt(Map.of("roles", "UNDERWRITER, ,RISK_ENGINEER"))))
                .assertNext(token -> assertEquals(2, token.getAuthorities().size())).verifyComplete();
        StepVerifier.create(converter.convert(jwt(Map.of("roles", " "))))
                .assertNext(token -> assertTrue(token.getAuthorities().isEmpty())).verifyComplete();
        assertThrows(IllegalArgumentException.class, () -> new StringToUuidConverter().convert("bad"));
    }

    @Test
    void actorServiceValidatesJwtIdentityAndRoles() {
        SecurityActorService service = new SecurityActorService();
        UUID id = UUID.randomUUID();
        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt(Map.of("sub", id.toString())),
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")), id.toString());
        StepVerifier.create(service.currentUserId().contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth))).expectNext(id).verifyComplete();
        StepVerifier.create(service.hasRole("ADMIN").contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth))).expectNext(true).verifyComplete();
        StepVerifier.create(service.hasRole("ROLE_ADMIN").contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth))).expectNext(true).verifyComplete();
        StepVerifier.create(service.hasRole("UNDERWRITER").contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth))).expectNext(false).verifyComplete();
        StepVerifier.create(service.currentUserId()).expectError(AccessDeniedBusinessException.class).verify();
        JwtAuthenticationToken invalid = new JwtAuthenticationToken(jwt(Map.of("sub", "bad")), List.of(), "bad");
        StepVerifier.create(service.currentUserId().contextWrite(ReactiveSecurityContextHolder.withAuthentication(invalid))).expectError(AccessDeniedBusinessException.class).verify();
        JwtAuthenticationToken blank = new JwtAuthenticationToken(jwt(Map.of("sub", " ")), List.of(), " ");
        StepVerifier.create(service.currentUserId().contextWrite(ReactiveSecurityContextHolder.withAuthentication(blank))).expectError(AccessDeniedBusinessException.class).verify();
        Jwt noSubjectJwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(60), Map.of("alg", "none"), Map.of("other", "value"));
        JwtAuthenticationToken noSubject = new JwtAuthenticationToken(noSubjectJwt, List.of(), "principal");
        StepVerifier.create(service.currentUserId().contextWrite(ReactiveSecurityContextHolder.withAuthentication(noSubject))).expectError(AccessDeniedBusinessException.class).verify();
        StepVerifier.create(service.hasRole("ADMIN").contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(new SecurityContextImpl(null)))))
                .expectError(NullPointerException.class).verify();
    }

    @Test
    void correlationFilterAndExceptionHandlersReturnContractResponses() {
        CorrelationIdFilter filter = new CorrelationIdFilter();
        WebFilterChain chain = exchange -> Mono.empty();
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/").header(CorrelationIdFilter.H, "corr").build());
        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
        assertEquals("corr", exchange.getResponse().getHeaders().getFirst(CorrelationIdFilter.H));
        MockServerWebExchange blank = MockServerWebExchange.from(MockServerHttpRequest.get("/").header(CorrelationIdFilter.H, " ").build());
        StepVerifier.create(filter.filter(blank, chain)).verifyComplete();
        assertNotNull(blank.getResponse().getHeaders().getFirst(CorrelationIdFilter.H));
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        assertEquals(HttpStatus.NOT_FOUND.value(), handler.handleNotFound(new ResourceNotFoundException("missing"), exchange).getStatusCode().value());
        assertEquals(HttpStatus.BAD_REQUEST.value(), handler.handleBusiness(new BusinessException("bad"), exchange).getStatusCode().value());
        assertEquals(HttpStatus.CONFLICT.value(), handler.handleDuplicate(new DuplicateKeyException("dup"), exchange).getStatusCode().value());
        assertEquals(HttpStatus.BAD_REQUEST.value(), handler.handleInput(new org.springframework.web.server.ServerWebInputException("bad"), exchange).getStatusCode().value());
        assertEquals(HttpStatus.FORBIDDEN.value(), handler.handleAccessDeniedBusinessException(new AccessDeniedBusinessException("denied"), exchange).getStatusCode().value());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), handler.handleUnexpected(new RuntimeException("boom"), exchange).getStatusCode().value());
    }

    @Test
    void webClientFiltersPropagateCorrelationAndBearerHeaders() {
        AtomicReference<ClientRequest> captured = new AtomicReference<>();
        ExchangeFunction next = request -> { captured.set(request); return Mono.just(ClientResponse.create(HttpStatus.OK).build()); };
        ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("http://example.test")).build();
        StepVerifier.create(WebClientConfig.bearerTokenPropagationFilter().filter(request, next))
                .expectNextCount(1).verifyComplete();
        assertNull(captured.get().headers().getFirst(HttpHeaders.AUTHORIZATION));
        UUID id = UUID.randomUUID();
        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt(Map.of("sub", id.toString())), List.of(), id.toString());
        StepVerifier.create(WebClientConfig.bearerTokenPropagationFilter().filter(request, next)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth)))
                .expectNextCount(1).verifyComplete();
        assertEquals("Bearer token", captured.get().headers().getFirst(HttpHeaders.AUTHORIZATION));
        ClientRequest existing = ClientRequest.create(HttpMethod.GET, URI.create("http://example.test")).header(HttpHeaders.AUTHORIZATION, "Bearer existing").build();
        StepVerifier.create(WebClientConfig.bearerTokenPropagationFilter().filter(existing, next)).expectNextCount(1).verifyComplete();
        assertEquals("Bearer existing", captured.get().headers().getFirst(HttpHeaders.AUTHORIZATION));
        WebClient client = new WebClientConfig().loadBalancedWebClientBuilder().exchangeFunction(next).build();
        StepVerifier.create(client.get().uri("http://example.test").retrieve().toBodilessEntity()
                        .contextWrite(ctx -> ctx.put(CorrelationConstants.CONTEXT_KEY, "corr-1")))
                .expectNextCount(1).verifyComplete();
        assertEquals("corr-1", captured.get().headers().getFirst(CorrelationConstants.HEADER));
    }

    private static Jwt jwt(Map<String, Object> claims) {
        return new Jwt("token", Instant.now(), Instant.now().plusSeconds(60), Map.of("alg", "none"), claims);
    }
}
