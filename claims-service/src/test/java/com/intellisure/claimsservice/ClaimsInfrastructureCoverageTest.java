package com.intellisure.claimsservice;

import com.intellisure.claimsservice.client.PolicyOwnershipClient;
import com.intellisure.claimsservice.converter.*;
import com.intellisure.claimsservice.exception.*;
import com.intellisure.claimsservice.filter.CorrelationIdFilter;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.server.context.SecurityContextServerWebExchange;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClaimsInfrastructureCoverageTest {

    @Test
    void uuidConvertersHandleNullInvalidAndValidValues() {
        BytesToUuidConverter reader = new BytesToUuidConverter();
        UuidToBytesConverter writer = new UuidToBytesConverter();
        UUID id = UUID.randomUUID();
        assertNull(reader.convert(null));
        assertNull(reader.convert(new byte[2]));
        assertEquals(id, reader.convert(writer.convert(id)));
        assertNull(writer.convert(null));
        assertEquals(16, writer.convert(id).length);
    }

    @Test
    void jwtConverterNormalizesCollectionDelimitedAndSingleRoles() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        StepVerifier.create(converter.convert(jwt(Map.of("roles", List.of("claims_adjuster", "ROLE_ADMIN")))))
                .assertNext(token -> assertEquals(List.of("ROLE_CLAIMS_ADJUSTER", "ROLE_ADMIN"), token.getAuthorities().stream().map(a -> a.getAuthority()).toList()))
                .verifyComplete();
        StepVerifier.create(converter.convert(jwt(Map.of("roles", "claims_adjuster, ,admin"))))
                .assertNext(token -> assertEquals(2, token.getAuthorities().size())).verifyComplete();
        StepVerifier.create(converter.convert(jwt(Map.of("role", "vendor"))))
                .assertNext(token -> assertEquals("ROLE_VENDOR", token.getAuthorities().iterator().next().getAuthority())).verifyComplete();
        StepVerifier.create(converter.convert(jwt(Map.of("other", "value")))).assertNext(token -> assertTrue(token.getAuthorities().isEmpty())).verifyComplete();
        StepVerifier.create(converter.convert(jwt(Map.of("role", " ")))).assertNext(token -> assertTrue(token.getAuthorities().isEmpty())).verifyComplete();
    }

    @Test
    void policyOwnershipClientEnforcesHttpAndCustomerOwnership() {
        AtomicReference<Mono<ClientResponse>> response = new AtomicReference<>();
        ExchangeFunction exchange = request -> response.get();
        PolicyOwnershipClient client = new PolicyOwnershipClient(WebClient.builder().exchangeFunction(exchange));
        UUID customer = UUID.randomUUID();
        response.set(Mono.just(ClientResponse.create(HttpStatus.OK).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body("{\"customerId\":\"" + customer + "\"}").build()));
        StepVerifier.create(client.assertPolicyOwnership(UUID.randomUUID(), customer)).verifyComplete();
        response.set(Mono.just(ClientResponse.create(HttpStatus.OK).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body("{\"customerId\":\"" + UUID.randomUUID() + "\"}").build()));
        StepVerifier.create(client.assertPolicyOwnership(UUID.randomUUID(), customer)).expectError(AccessDeniedBusinessException.class).verify();
        response.set(Mono.just(ClientResponse.create(HttpStatus.UNAUTHORIZED).build()));
        StepVerifier.create(client.assertPolicyOwnership(UUID.randomUUID(), customer)).expectError(AccessDeniedBusinessException.class).verify();
        response.set(Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build()));
        StepVerifier.create(client.assertPolicyOwnership(UUID.randomUUID(), customer)).expectError().verify();
        response.set(Mono.just(ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR).build()));
        StepVerifier.create(client.assertPolicyOwnership(UUID.randomUUID(), customer)).expectError().verify();
        response.set(Mono.just(ClientResponse.create(HttpStatus.OK).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body("").build()));
        StepVerifier.create(client.assertPolicyOwnership(UUID.randomUUID(), customer)).expectError().verify();
    }

    @Test
    void correlationFilterPreservesOrCreatesCorrelationId() {
        CorrelationIdFilter filter = new CorrelationIdFilter();
        WebFilterChain chain = exchange -> Mono.empty();
        MockServerWebExchange existing = MockServerWebExchange.from(MockServerHttpRequest.get("/").header(CorrelationIdFilter.H, "known").build());
        StepVerifier.create(filter.filter(existing, chain)).verifyComplete();
        assertEquals("known", existing.getResponse().getHeaders().getFirst(CorrelationIdFilter.H));
        MockServerWebExchange missing = MockServerWebExchange.from(MockServerHttpRequest.get("/").build());
        StepVerifier.create(filter.filter(missing, chain)).verifyComplete();
        assertNotNull(missing.getResponse().getHeaders().getFirst(CorrelationIdFilter.H));
        MockServerWebExchange blank = MockServerWebExchange.from(MockServerHttpRequest.get("/").header(CorrelationIdFilter.H, " ").build());
        StepVerifier.create(filter.filter(blank, chain)).verifyComplete();
        assertNotNull(blank.getResponse().getHeaders().getFirst(CorrelationIdFilter.H));
    }

    @Test
    void exceptionHandlerReturnsContractStatuses() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        assertEquals(404, handler.handleResourceNotFound(new ResourceNotFoundException("missing")).getStatusCode().value());
        assertEquals(400, handler.handleBusinessException(new BusinessException("bad")).getStatusCode().value());
        assertEquals(403, handler.handleAccessDenied(new AccessDeniedBusinessException("denied")).getStatusCode().value());
        assertEquals(409, handler.handleDuplicateResource(new DuplicateResourceException("duplicate")).getStatusCode().value());
        assertEquals(401, handler.handleInvalidCredentials(new InvalidCredentialsException("invalid")).getStatusCode().value());
        assertEquals(500, handler.handleGeneric(new RuntimeException("boom")).getStatusCode().value());
        assertEquals("missing", handler.handleResourceNotFound(new ResourceNotFoundException("missing")).getBody().message());
    }

    private static Jwt jwt(Map<String, Object> claims) {
        return new Jwt("token", Instant.now(), Instant.now().plusSeconds(60), Map.of("alg", "none"),
                claims.isEmpty() ? Map.of("sub", "subject") : claims);
    }
}
