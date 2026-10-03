package com.intellisure.riskunderwritingservice.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class CorrelationIdWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            WebFilterChain chain
    ) {
        String correlationId = exchange.getRequest()
                .getHeaders()
                .getFirst(CorrelationConstants.HEADER);

        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        String finalCorrelationId = correlationId;

        ServerHttpRequest request = exchange.getRequest()
                .mutate()
                .header(CorrelationConstants.HEADER, finalCorrelationId)
                .build();

        ServerWebExchange updatedExchange = exchange.mutate()
                .request(request)
                .build();

        updatedExchange.getResponse()
                .getHeaders()
                .set(CorrelationConstants.HEADER, finalCorrelationId);

        long startedAt = System.currentTimeMillis();

        log.info(
                "Request started: correlationId={}, method={}, path={}",
                finalCorrelationId,
                request.getMethod(),
                request.getPath().value()
        );

        return chain.filter(updatedExchange)
                .doOnSuccess(unused -> log.info(
                        "Request completed: correlationId={}, method={}, path={}, status={}, durationMs={}",
                        finalCorrelationId,
                        request.getMethod(),
                        request.getPath().value(),
                        updatedExchange.getResponse().getStatusCode(),
                        System.currentTimeMillis() - startedAt
                ))
                .doOnError(error -> log.error(
                        "Request failed: correlationId={}, method={}, path={}, errorType={}, durationMs={}",
                        finalCorrelationId,
                        request.getMethod(),
                        request.getPath().value(),
                        error.getClass().getSimpleName(),
                        System.currentTimeMillis() - startedAt
                ))
                .contextWrite(context -> context.put(
                        CorrelationConstants.CONTEXT_KEY,
                        finalCorrelationId
                ));
    }
}
