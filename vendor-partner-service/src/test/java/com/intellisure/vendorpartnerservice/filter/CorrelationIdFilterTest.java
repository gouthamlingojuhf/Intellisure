package com.intellisure.vendorpartnerservice.filter;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class CorrelationIdFilterTest {
    private final CorrelationIdFilter filter = new CorrelationIdFilter();
    @Test void preservesExistingCorrelationId() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/").header(CorrelationIdFilter.H, "corr").build());
        AtomicReference<String> seen = new AtomicReference<>();
        filter.filter(exchange, next -> { seen.set(next.getRequest().getHeaders().getFirst(CorrelationIdFilter.H)); return Mono.empty(); }).block();
        assertEquals("corr", seen.get()); assertEquals("corr", exchange.getResponse().getHeaders().getFirst(CorrelationIdFilter.H));
    }
    @Test void generatesMissingCorrelationId() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/").build());
        AtomicReference<String> seen = new AtomicReference<>();
        filter.filter(exchange, next -> { seen.set(next.getRequest().getHeaders().getFirst(CorrelationIdFilter.H)); return Mono.empty(); }).block();
        assertNotNull(seen.get()); assertFalse(seen.get().isBlank());
        MockServerWebExchange blank = MockServerWebExchange.from(MockServerHttpRequest.get("/").header(CorrelationIdFilter.H, " ").build());
        filter.filter(blank, next -> Mono.empty()).block();
        assertNotNull(blank.getResponse().getHeaders().getFirst(CorrelationIdFilter.H));
    }
}
