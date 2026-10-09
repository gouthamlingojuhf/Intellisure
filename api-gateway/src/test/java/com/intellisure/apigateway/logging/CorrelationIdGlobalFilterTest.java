package com.intellisure.apigateway.logging;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CorrelationIdGlobalFilterTest {

    private final CorrelationIdGlobalFilter filter = new CorrelationIdGlobalFilter();

    @Test
    void propagatesExistingCorrelationIdToRequestAndResponse() {
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
        MockServerWebExchange exchange = exchangeWithCorrelationId("existing-id");

        filter.filter(exchange, chain).block();

        verify(chain).filter(org.mockito.ArgumentMatchers.argThat(updated ->
                "existing-id".equals(updated.getRequest().getHeaders()
                        .getFirst("X-Correlation-ID"))));
        assertThat(exchange.getResponse().getHeaders().getFirst("X-Correlation-ID"))
                .isEqualTo("existing-id");
    }

    @Test
    void createsAndPropagatesUuidWhenHeaderIsBlank() {
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/claims")
                        .header("X-Correlation-ID", "   ")
                        .build());

        filter.filter(exchange, chain).block();

        org.mockito.ArgumentCaptor<ServerWebExchange> captor =
                org.mockito.ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(captor.capture());
        String correlationId = captor.getValue().getRequest().getHeaders()
                .getFirst("X-Correlation-ID");
        assertThat(UUID.fromString(correlationId)).isNotNull();
        assertThat(exchange.getResponse().getHeaders().getFirst("X-Correlation-ID"))
                .isEqualTo(correlationId);
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
}
