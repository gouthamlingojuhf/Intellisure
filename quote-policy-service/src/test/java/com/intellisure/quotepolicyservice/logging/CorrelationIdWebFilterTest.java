package com.intellisure.quotepolicyservice.logging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("CorrelationIdWebFilter")
class CorrelationIdWebFilterTest {

    private CorrelationIdWebFilter filter;
    private WebFilterChain chain;
    private AtomicReference<ServerWebExchange> seenExchange;

    @BeforeEach
    void setUp() {
        filter = new CorrelationIdWebFilter();
        chain = mock(WebFilterChain.class);
        seenExchange = new AtomicReference<>();

        when(chain.filter(any(ServerWebExchange.class))).thenAnswer(
                invocation -> {
                    seenExchange.set(invocation.getArgument(0));
                    return Mono.empty();
                }
        );
    }

    @Test
    @DisplayName("reuses an inbound correlation id")
    void reusesInboundCorrelationId() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/quotes")
                        .header(
                                CorrelationConstants.HEADER,
                                "corr-123"
                        )
        );

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertEquals(
                "corr-123",
                seenExchange.get()
                        .getRequest()
                        .getHeaders()
                        .getFirst(CorrelationConstants.HEADER)
        );
        assertEquals(
                "corr-123",
                exchange.getResponse()
                        .getHeaders()
                        .getFirst(CorrelationConstants.HEADER)
        );
    }

    @Test
    @DisplayName("generates a correlation id when the header is absent")
    void generatesCorrelationId() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/quotes")
        );

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        String generated = seenExchange.get()
                .getRequest()
                .getHeaders()
                .getFirst(CorrelationConstants.HEADER);

        assertNotNull(generated);
        assertNotEquals("", generated);
        assertEquals(
                generated,
                exchange.getResponse()
                        .getHeaders()
                        .getFirst(CorrelationConstants.HEADER)
        );
    }

    @Test
    @DisplayName("generates a correlation id when the header is blank")
    void generatesCorrelationIdForBlankHeader() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/quotes")
                        .header(CorrelationConstants.HEADER, "   ")
        );

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        String generated = seenExchange.get()
                .getRequest()
                .getHeaders()
                .getFirst(CorrelationConstants.HEADER);

        assertNotNull(generated);
        assertTrue(
                UUID.fromString(generated).toString()
                        .equals(generated)
        );
    }

    @Test
    @DisplayName("generates a distinct id for each request without a header")
    void generatesDistinctIds() {
        String first = captureGeneratedId();
        String second = captureGeneratedId();

        assertNotEquals(first, second);
    }

    private String captureGeneratedId() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/quotes")
        );

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        return exchange.getResponse()
                .getHeaders()
                .getFirst(CorrelationConstants.HEADER);
    }

    @Test
    @DisplayName("publishes the correlation id into the reactor context")
    void publishesContextValue() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/quotes")
                        .header(CorrelationConstants.HEADER, "ctx-1")
        );

        when(chain.filter(any(ServerWebExchange.class))).thenAnswer(
                invocation -> Mono.deferContextual(context -> {
                    assertEquals(
                            "ctx-1",
                            context.get(CorrelationConstants
                                    .CONTEXT_KEY)
                    );
                    return Mono.empty();
                })
        );

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();
    }

    @Test
    @DisplayName("propagates a successful response status")
    void completesSuccessfully() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/actuator/health")
        );

        when(chain.filter(any(ServerWebExchange.class))).thenAnswer(
                invocation -> {
                    invocation.<ServerWebExchange>getArgument(0)
                            .getResponse()
                            .setStatusCode(HttpStatus.OK);
                    return Mono.empty();
                }
        );

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertEquals(
                HttpStatus.OK,
                exchange.getResponse().getStatusCode()
        );
        assertNotNull(
                exchange.getResponse()
                        .getHeaders()
                        .getFirst(CorrelationConstants.HEADER)
        );
    }

    @Test
    @DisplayName("propagates downstream errors unchanged")
    void propagatesErrors() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/quotes")
        );

        when(chain.filter(any(ServerWebExchange.class))).thenReturn(
                Mono.error(new IllegalStateException("boom"))
        );

        StepVerifier.create(filter.filter(exchange, chain))
                .expectError(IllegalStateException.class)
                .verify();

        assertNotNull(
                exchange.getResponse()
                        .getHeaders()
                        .getFirst(CorrelationConstants.HEADER)
        );
    }

    @Test
    @DisplayName("does not mutate the original exchange request")
    void leavesOriginalRequestUntouched() {
        var request = MockServerHttpRequest
                .get("/api/v1/quotes")
                .header(HttpHeaders.ACCEPT, "application/json");

        MockServerWebExchange exchange = MockServerWebExchange.from(
                request
        );

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertNotEquals(
                seenExchange.get().getRequest(),
                exchange.getRequest()
        );
    }
}
