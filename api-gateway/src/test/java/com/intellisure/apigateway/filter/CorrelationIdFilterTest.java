package com.intellisure.apigateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void preservesCallerCorrelationIdAndAddsResponseHeader() {
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(org.mockito.ArgumentMatchers.any())).thenReturn(Mono.empty());
        MockServerWebExchange exchange = exchangeWithCorrelationId("caller-correlation-id");

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getHeaders().getFirst("X-Correlation-ID"))
                .isEqualTo("caller-correlation-id");
        verify(chain).filter(same(exchange));
    }

    @Test
    void createsUuidWhenCallerDoesNotProvideCorrelationId() {
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(org.mockito.ArgumentMatchers.any())).thenReturn(Mono.empty());
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/quotes").build());

        filter.filter(exchange, chain).block();

        String correlationId = exchange.getResponse().getHeaders()
                .getFirst("X-Correlation-ID");
        assertThat(correlationId).isNotBlank();
        assertThatCodeCanParseUuid(correlationId);
    }

    @Test
    void createsUuidWhenCallerProvidesBlankCorrelationId() {
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(org.mockito.ArgumentMatchers.any())).thenReturn(Mono.empty());
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/quotes")
                        .header("X-Correlation-ID", "   ")
                        .build());

        filter.filter(exchange, chain).block();

        assertThatCodeCanParseUuid(exchange.getResponse().getHeaders()
                .getFirst("X-Correlation-ID"));
    }

    @Test
    void exposesHighestPrecedenceOrder() {
        assertThat(filter.getOrder()).isEqualTo(Integer.MIN_VALUE);
    }

    private MockServerWebExchange exchangeWithCorrelationId(String correlationId) {
        return MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/quotes")
                        .header("X-Correlation-ID", correlationId)
                        .build());
    }

    private void assertThatCodeCanParseUuid(String value) {
        assertThat(UUID.fromString(value)).isNotNull();
    }
}
