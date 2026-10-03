package com.intellisure.quotepolicyservice.client;

import com.intellisure.quotepolicyservice.client.dto.UnderwritingResultClientResponse;
import com.intellisure.quotepolicyservice.exception.BusinessException;
import com.intellisure.quotepolicyservice.exception.DownstreamServiceException;
import com.intellisure.quotepolicyservice.exception.DownstreamServiceUnavailableException;
import com.intellisure.quotepolicyservice.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Slf4j
public class RiskUnderwritingClient {

    private static final String CIRCUIT_BREAKER_NAME =
            "riskUnderwritingService";

    private final WebClient webClient;

    private final ReactiveCircuitBreaker circuitBreaker;

    public RiskUnderwritingClient(
            WebClient.Builder loadBalancedWebClientBuilder,
            ReactiveCircuitBreakerFactory<?, ?>
                    circuitBreakerFactory
    ) {
        this.webClient =
                loadBalancedWebClientBuilder
                        .baseUrl(
                                "http://risk-underwriting-service"
                        )
                        .build();

        this.circuitBreaker =
                circuitBreakerFactory.create(
                        CIRCUIT_BREAKER_NAME
                );
    }

    public Mono<UnderwritingResultClientResponse>
    getUnderwritingResult(
            UUID quoteId,
            String authorizationHeader
    ) {
        Mono<UnderwritingResultClientResponse> remoteCall =
                executeRequest(
                        quoteId,
                        authorizationHeader
                );

        return circuitBreaker.run(
                remoteCall,
                throwable ->
                        handleFallback(
                                quoteId,
                                throwable
                        )
        );
    }

    private Mono<UnderwritingResultClientResponse>
    executeRequest(
            UUID quoteId,
            String authorizationHeader
    ) {
        WebClient.RequestHeadersSpec<?> request =
                webClient
                        .get()
                        .uri(
                                "/api/v1/quotes/{quoteId}"
                                        + "/underwriting-result",
                                quoteId
                        );

        if (authorizationHeader != null
                && !authorizationHeader.isBlank()) {
            request = request.header(
                    HttpHeaders.AUTHORIZATION,
                    authorizationHeader
            );
        }

        long startedAt =
                System.currentTimeMillis();

        return request
                .retrieve()

                .onStatus(
                        status -> status.value() == 404,
                        response ->
                                response
                                        .bodyToMono(String.class)
                                        .defaultIfEmpty("")
                                        .flatMap(body ->
                                                Mono.error(
                                                        new ResourceNotFoundException(
                                                                "No authoritative "
                                                                        + "underwriting result "
                                                                        + "exists for quote: "
                                                                        + quoteId
                                                        )
                                                )
                                        )
                )

                .onStatus(
                        status ->
                                status.value() == 400
                                        || status.value() == 409,
                        response ->
                                response
                                        .bodyToMono(String.class)
                                        .defaultIfEmpty("")
                                        .flatMap(body ->
                                                Mono.error(
                                                        new BusinessException(
                                                                "Risk & Underwriting "
                                                                        + "rejected the request"
                                                        )
                                                )
                                        )
                )

                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        response ->
                                response
                                        .bodyToMono(String.class)
                                        .defaultIfEmpty("")
                                        .flatMap(body ->
                                                Mono.error(
                                                        new DownstreamServiceException(
                                                                "Risk & Underwriting "
                                                                        + "Service returned "
                                                                        + response
                                                                        .statusCode()
                                                                        .value()
                                                        )
                                                )
                                        )
                )

                .bodyToMono(
                        UnderwritingResultClientResponse.class
                )

                .switchIfEmpty(
                        Mono.error(
                                new DownstreamServiceException(
                                        "Risk & Underwriting Service "
                                                + "returned an empty response"
                                )
                        )
                )

                .doOnSubscribe(subscription ->
                        log.info(
                                "Calling Risk & Underwriting Service: "
                                        + "quoteId={}",
                                quoteId
                        )
                )

                .doOnSuccess(result ->
                        log.info(
                                "Risk & Underwriting call succeeded: "
                                        + "quoteId={}, assessmentId={}, "
                                        + "outcome={}, durationMs={}",
                                quoteId,
                                result == null
                                        ? null
                                        : result.assessmentId(),
                                result == null
                                        ? null
                                        : result.outcome(),
                                System.currentTimeMillis()
                                        - startedAt
                        )
                )

                .doOnError(error ->
                        log.warn(
                                "Risk & Underwriting call failed: "
                                        + "quoteId={}, errorType={}, "
                                        + "durationMs={}",
                                quoteId,
                                error.getClass()
                                        .getSimpleName(),
                                System.currentTimeMillis()
                                        - startedAt
                        )
                );
    }

    private Mono<UnderwritingResultClientResponse>
    handleFallback(
            UUID quoteId,
            Throwable throwable
    ) {
        if (throwable
                instanceof ResourceNotFoundException
                || throwable
                instanceof BusinessException) {
            return Mono.error(throwable);
        }

        log.error(
                "Risk & Underwriting circuit-breaker fallback: "
                        + "quoteId={}, errorType={}, message={}",
                quoteId,
                throwable.getClass().getSimpleName(),
                throwable.getMessage()
        );

        return Mono.error(
                new DownstreamServiceUnavailableException(
                        "Risk & Underwriting Service is temporarily "
                                + "unavailable. Please retry later.",
                        throwable
                )
        );
    }
}