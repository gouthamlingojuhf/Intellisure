package com.intellisure.quotepolicyservice.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.intellisure.quotepolicyservice.client.dto.RiskAssessmentStatusClient;
import com.intellisure.quotepolicyservice.client.dto.RiskBandClient;
import com.intellisure.quotepolicyservice.client.dto.UnderwritingOutcomeClient;
import com.intellisure.quotepolicyservice.client.dto.UnderwritingResultClientResponse;
import com.intellisure.quotepolicyservice.exception.BusinessException;
import com.intellisure.quotepolicyservice.exception.DownstreamServiceException;
import com.intellisure.quotepolicyservice.exception.DownstreamServiceUnavailableException;
import com.intellisure.quotepolicyservice.exception.ResourceNotFoundException;
import com.intellisure.quotepolicyservice.testsupport.TestAssertions;
import com.intellisure.quotepolicyservice.testsupport.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@DisplayName("RiskUnderwritingClient")
class RiskUnderwritingClientTest {

    private static final UUID QUOTE_ID = UUID.randomUUID();

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private AtomicReference<ClientRequest> capturedRequest;
    private ReactiveCircuitBreakerFactory<?, ?> circuitBreakerFactory;
    private WebClient.Builder webClientBuilder;

    @BeforeEach
    void setUp() {
        capturedRequest = new AtomicReference<>();

        ExchangeFunction exchangeFunction = request -> {
            capturedRequest.set(request);

            return responseSupplier.get();
        };

        webClientBuilder = WebClient.builder()
                .exchangeFunction(exchangeFunction);

        ReactiveCircuitBreaker circuitBreaker =
                new ReactiveCircuitBreaker() {

                    @Override
                    public <T> Mono<T> run(
                            Mono<T> toRun,
                            Function<Throwable, Mono<T>> fallback
                    ) {
                        return toRun.onErrorResume(fallback);
                    }

                    @Override
                    public <T> reactor.core.publisher.Flux<T> run(
                            reactor.core.publisher.Flux<T> toRun,
                            Function<Throwable,
                                    reactor.core.publisher.Flux<T>> fallback
                    ) {
                        return toRun.onErrorResume(fallback);
                    }
                };

        circuitBreakerFactory = mock(ReactiveCircuitBreakerFactory.class);
        doReturn(circuitBreaker)
                .when(circuitBreakerFactory)
                .create(anyString());
    }

    private final ResponseSupplier responseSupplier =
            new ResponseSupplier();

    private void stubResponse(Mono<ClientResponse> response) {
        responseSupplier.set(response);
    }

    private static final class ResponseSupplier implements
            java.util.function.Supplier<Mono<ClientResponse>> {

        private Mono<ClientResponse> delegate = Mono.empty();

        void set(Mono<ClientResponse> response) {
            this.delegate = response;
        }

        @Override
        public Mono<ClientResponse> get() {
            return delegate;
        }
    }

    private RiskUnderwritingClient newClient() {
        return new RiskUnderwritingClient(
                webClientBuilder,
                circuitBreakerFactory
        );
    }

    private ClientResponse jsonResponse(
            HttpStatusCode status,
            Object body
    ) {
        String serialised;

        try {
            serialised = objectMapper.writeValueAsString(body);
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }

        return ClientResponse.create(status)
                .header("Content-Type", "application/json")
                .body(serialised)
                .build();
    }

    private UnderwritingResultClientResponse payload(
            UUID quoteId
    ) {
        return new UnderwritingResultClientResponse(
                UUID.randomUUID(),
                "RA-2026-0001",
                quoteId,
                RiskAssessmentStatusClient.COMPLETED,
                new BigDecimal("42.50"),
                RiskBandClient.MODERATE,
                UUID.randomUUID(),
                UnderwritingOutcomeClient.APPROVED,
                "rationale",
                "AUTOMATED",
                Boolean.TRUE,
                new BigDecimal("90000.00"),
                new BigDecimal("750.00"),
                new BigDecimal("1450.00"),
                "Survey",
                Boolean.FALSE,
                "RULES-2026.01",
                TestFixtures.NOW
        );
    }

    @Nested
    @DisplayName("successful responses")
    class Success {

        @Test
        @DisplayName("maps a 200 payload onto the client response record")
        void mapsSuccessfulResponse() {
            stubResponse(
                    Mono.just(
                            jsonResponse(
                                    HttpStatusCode.valueOf(200),
                                    payload(QUOTE_ID)
                            )
                    )
            );

            StepVerifier.create(
                            newClient().getUnderwritingResult(
                                    QUOTE_ID,
                                    "Bearer abc"
                            )
                    )
                    .assertNext(result -> {
                        assertEquals(
                                RiskAssessmentStatusClient.COMPLETED,
                                result.assessmentStatus()
                        );
                        assertEquals(
                                UnderwritingOutcomeClient.APPROVED,
                                result.outcome()
                        );
                        assertEquals(
                                "RA-2026-0001",
                                result.assessmentNumber()
                        );
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("calls the underwriting result endpoint of the quote")
        void callsExpectedPath() {
            stubResponse(
                    Mono.just(
                            jsonResponse(
                                    HttpStatusCode.valueOf(200),
                                    payload(QUOTE_ID)
                            )
                    )
            );

            newClient().getUnderwritingResult(QUOTE_ID, "Bearer abc")
                    .block();

            assertEquals(
                    "/api/quotes/" + QUOTE_ID
                            + "/underwriting-result",
                    capturedRequest.get().url().getPath()
            );
            assertEquals(
                    "risk-underwriting-service",
                    capturedRequest.get().url().getHost()
            );
        }

        @Test
        @DisplayName("forwards the authorization header when present")
        void forwardsAuthorizationHeader() {
            stubResponse(
                    Mono.just(
                            jsonResponse(
                                    HttpStatusCode.valueOf(200),
                                    payload(QUOTE_ID)
                            )
                    )
            );

            newClient().getUnderwritingResult(QUOTE_ID, "Bearer abc")
                    .block();

            assertEquals(
                    "Bearer abc",
                    capturedRequest.get()
                            .headers()
                            .getFirst("Authorization")
            );
        }

        @Test
        @DisplayName("omits the authorization header when absent or blank")
        void omitsAuthorizationHeader() {
            stubResponse(
                    Mono.just(
                            jsonResponse(
                                    HttpStatusCode.valueOf(200),
                                    payload(QUOTE_ID)
                            )
                    )
            );

            newClient().getUnderwritingResult(QUOTE_ID, null).block();
            assertNull(
                    capturedRequest.get()
                            .headers()
                            .getFirst("Authorization")
            );

            newClient().getUnderwritingResult(QUOTE_ID, "   ").block();
            assertNull(
                    capturedRequest.get()
                            .headers()
                            .getFirst("Authorization")
            );
        }
    }

    @Nested
    @DisplayName("error responses")
    class Errors {

        @Test
        @DisplayName("maps 404 to a resource not found error")
        void mapsNotFound() {
            stubResponse(
                    Mono.just(
                            ClientResponse.create(
                                    HttpStatusCode.valueOf(404)
                            )
                                    .header("Content-Type", "text/plain")
                                    .body("no result")
                                    .build()
                    )
            );

            StepVerifier.create(
                            newClient().getUnderwritingResult(
                                    QUOTE_ID,
                                    "Bearer abc"
                            )
                    )
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            ResourceNotFoundException.class,
                            "No authoritative underwriting result exists "
                                    + "for quote: " + QUOTE_ID,
                            error
                    ))
                    .verify();
        }

        @Test
        @DisplayName("maps 400 to a business error")
        void mapsBadRequest() {
            stubResponse(
                    Mono.just(
                            ClientResponse.create(
                                    HttpStatusCode.valueOf(400)
                            )
                                    .header("Content-Type", "text/plain")
                                    .body("invalid")
                                    .build()
                    )
            );

            StepVerifier.create(
                            newClient().getUnderwritingResult(
                                    QUOTE_ID,
                                    "Bearer abc"
                            )
                    )
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            BusinessException.class,
                            "Risk & Underwriting rejected the request",
                            error
                    ))
                    .verify();
        }

        @Test
        @DisplayName("maps 409 to a business error")
        void mapsConflict() {
            stubResponse(
                    Mono.just(
                            ClientResponse.create(
                                    HttpStatusCode.valueOf(409)
                            )
                                    .header("Content-Type", "text/plain")
                                    .body("conflict")
                                    .build()
                    )
            );

            StepVerifier.create(
                            newClient().getUnderwritingResult(
                                    QUOTE_ID,
                                    "Bearer abc"
                            )
                    )
                    .expectError(BusinessException.class)
                    .verify();
        }

        @Test
        @DisplayName("a 5xx response is reported as a service outage, not as 502")
        void mapsServerError() {
            stubResponse(
                    Mono.just(
                            ClientResponse.create(
                                    HttpStatusCode.valueOf(503)
                            )
                                    .header("Content-Type", "text/plain")
                                    .body("unavailable")
                                    .build()
                    )
            );

            StepVerifier.create(
                            newClient().getUnderwritingResult(
                                    QUOTE_ID,
                                    "Bearer abc"
                            )
                    )
                    .expectErrorSatisfies(error -> {
                        TestAssertions.errorIs(
                                DownstreamServiceUnavailableException.class,
                                "Risk & Underwriting Service is "
                                        + "temporarily unavailable. "
                                        + "Please retry later.",
                                error
                        );
                        org.junit.jupiter.api.Assertions.assertInstanceOf(
                                DownstreamServiceException.class,
                                error.getCause()
                        );
                        org.junit.jupiter.api.Assertions.assertEquals(
                                "Risk & Underwriting Service returned 503",
                                error.getCause().getMessage()
                        );
                    })
                    .verify();
        }

        @Test
        @DisplayName("an empty body is reported as a service outage")
        void mapsEmptyBody() {
            stubResponse(
                    Mono.just(
                            ClientResponse.create(
                                    HttpStatusCode.valueOf(204)
                            ).build()
                    )
            );

            StepVerifier.create(
                            newClient().getUnderwritingResult(
                                    QUOTE_ID,
                                    "Bearer abc"
                            )
                    )
                    .expectErrorSatisfies(error -> {
                        org.junit.jupiter.api.Assertions.assertInstanceOf(
                                DownstreamServiceUnavailableException.class,
                                error
                        );
                        org.junit.jupiter.api.Assertions.assertEquals(
                                "Risk & Underwriting Service returned an "
                                        + "empty response",
                                error.getCause().getMessage()
                        );
                    })
                    .verify();
        }

        @Test
        @DisplayName("wraps a transport failure as a service outage")
        void wrapsTransportFailure() {
            stubResponse(
                    Mono.error(
                            new java.io.IOException("connection refused")
                    )
            );

            StepVerifier.create(
                            newClient().getUnderwritingResult(
                                    QUOTE_ID,
                                    "Bearer abc"
                            )
                    )
                    .expectErrorSatisfies(error -> {
                        TestAssertions.errorIs(
                                DownstreamServiceUnavailableException.class,
                                "Risk & Underwriting Service is "
                                        + "temporarily unavailable. "
                                        + "Please retry later.",
                                error
                        );
                        assertEquals(
                                "connection refused",
                                error.getCause().getMessage()
                        );
                    })
                    .verify();
        }

        @Test
        @DisplayName("passes business failures through the circuit breaker unchanged")
        void passesBusinessFailuresThrough() {
            stubResponse(
                    Mono.just(
                            ClientResponse.create(
                                    HttpStatusCode.valueOf(400)
                            )
                                    .header("Content-Type", "text/plain")
                                    .body("invalid")
                                    .build()
                    )
            );

            StepVerifier.create(
                            newClient().getUnderwritingResult(
                                    QUOTE_ID,
                                    "Bearer abc"
                            )
                    )
                    .expectError(BusinessException.class)
                    .verify();
        }

        @Test
        @DisplayName("passes not found failures through the circuit breaker unchanged")
        void passesNotFoundThrough() {
            stubResponse(
                    Mono.just(
                            ClientResponse.create(
                                    HttpStatusCode.valueOf(404)
                            )
                                    .header("Content-Type", "text/plain")
                                    .body("missing")
                                    .build()
                    )
            );

            StepVerifier.create(
                            newClient().getUnderwritingResult(
                                    QUOTE_ID,
                                    "Bearer abc"
                            )
                    )
                    .expectError(ResourceNotFoundException.class)
                    .verify();
        }
    }
}
