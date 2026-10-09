package com.intellisure.analyticsintelligenceservice.filter;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CorrelationIdFilterTest {
    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void preservesAndPropagatesExistingCorrelationId() {
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
        MockServerWebExchange exchange = exchange("existing-id");

        filter.filter(exchange, chain).block();

        verify(chain).filter(org.mockito.ArgumentMatchers.argThat(updated ->
                "existing-id".equals(updated.getRequest().getHeaders().getFirst(CorrelationIdFilter.H))));
        assertThat(exchange.getResponse().getHeaders().getFirst(CorrelationIdFilter.H)).isEqualTo("existing-id");
    }

    @Test
    void createsUuidForMissingOrBlankCorrelationId() {
        for (MockServerWebExchange exchange : new MockServerWebExchange[]{
                MockServerWebExchange.from(MockServerHttpRequest.get("/api/analytics").build()),
                exchange("   ")
        }) {
            WebFilterChain chain = mock(WebFilterChain.class);
            when(chain.filter(any())).thenReturn(Mono.empty());
            filter.filter(exchange, chain).block();
            assertThat(UUID.fromString(exchange.getResponse().getHeaders().getFirst(CorrelationIdFilter.H)))
                    .isNotNull();
        }
    }

    private MockServerWebExchange exchange(String correlationId) {
        return MockServerWebExchange.from(MockServerHttpRequest.get("/api/analytics")
                .header(CorrelationIdFilter.H, correlationId).build());
    }
}
