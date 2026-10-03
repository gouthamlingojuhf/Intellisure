package com.intellisure.apigateway.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Slf4j
public class CorrelationIdGlobalFilter
        implements GlobalFilter, Ordered {

    private static final String HEADER =
            "X-Correlation-ID";

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain
    ) {
        String correlationId =
                exchange.getRequest()
                        .getHeaders()
                        .getFirst(HEADER);

        if (correlationId == null
                || correlationId.isBlank()) {
            correlationId =
                    UUID.randomUUID().toString();
        }

        String finalCorrelationId =
                correlationId;

        ServerHttpRequest request =
                exchange.getRequest()
                        .mutate()
                        .header(
                                HEADER,
                                finalCorrelationId
                        )
                        .build();

        ServerWebExchange updatedExchange =
                exchange.mutate()
                        .request(request)
                        .build();

        updatedExchange
                .getResponse()
                .getHeaders()
                .set(
                        HEADER,
                        finalCorrelationId
                );

        long startedAt =
                System.currentTimeMillis();

        log.info(
                "Gateway request started: "
                        + "correlationId={}, method={}, path={}",
                finalCorrelationId,
                request.getMethod(),
                request.getPath().value()
        );

        return chain
                .filter(updatedExchange)
                .doFinally(signal ->
                        log.info(
                                "Gateway request completed: "
                                        + "correlationId={}, method={}, "
                                        + "path={}, status={}, durationMs={}",
                                finalCorrelationId,
                                request.getMethod(),
                                request.getPath().value(),
                                updatedExchange
                                        .getResponse()
                                        .getStatusCode(),
                                System.currentTimeMillis()
                                        - startedAt
                        )
                );
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}