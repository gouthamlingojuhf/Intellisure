package com.intellisure.claimsservice.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("CustomerPartyAdjusterClientTest")
class CustomerPartyAdjusterClientTest {

    private AtomicReference<ClientRequest> capturedRequest;
    private AtomicReference<Mono<ClientResponse>> responseSupplier;
    private CustomerPartyAdjusterClient client;

    @BeforeEach
    void setUp() {
        capturedRequest = new AtomicReference<>();
        responseSupplier = new AtomicReference<>(Mono.empty());

        ExchangeFunction exchangeFunction = request -> {
            capturedRequest.set(request);
            return responseSupplier.get();
        };

        WebClient.Builder builder = WebClient.builder().exchangeFunction(exchangeFunction);
        client = new CustomerPartyAdjusterClient(builder);
    }

    @Test
    @DisplayName("findAvailableAdjusters successfully maps adjuster UUIDs from 200 OK response")
    void findAvailableAdjustersReturnsAdjusterIdsOnSuccess() {
        UUID adjusterId1 = UUID.randomUUID();
        UUID adjusterId2 = UUID.randomUUID();
        String json = """
                [
                    {"userId": "%s", "role": "CLAIMS_ADJUSTER", "accountStatus": "ACTIVE"},
                    {"userId": "%s", "role": "CLAIMS_ADJUSTER", "accountStatus": "ACTIVE"}
                ]
                """.formatted(adjusterId1, adjusterId2);

        ClientResponse response = ClientResponse.create(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(json)
                .build();
        responseSupplier.set(Mono.just(response));

        StepVerifier.create(client.findAvailableAdjusters())
                .expectNext(adjusterId1)
                .expectNext(adjusterId2)
                .verifyComplete();

        assertNotNull(capturedRequest.get());
        assertEquals("/api/users/role/CLAIMS_ADJUSTER/available", capturedRequest.get().url().getPath());
    }

    @Test
    @DisplayName("findAvailableAdjusters falls back to empty Flux when customer-party-service returns 403 Forbidden")
    void findAvailableAdjustersFallsBackToEmptyOnHttp403Forbidden() {
        ClientResponse response = ClientResponse.create(HttpStatus.FORBIDDEN)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body("{\"message\": \"Forbidden\"}")
                .build();
        responseSupplier.set(Mono.just(response));

        StepVerifier.create(client.findAvailableAdjusters())
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    @DisplayName("findAvailableAdjusters falls back to empty Flux when customer-party-service returns 503 Service Unavailable")
    void findAvailableAdjustersFallsBackToEmptyOnHttp503Unavailable() {
        ClientResponse response = ClientResponse.create(HttpStatus.SERVICE_UNAVAILABLE)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body("{\"message\": \"Service Unavailable\"}")
                .build();
        responseSupplier.set(Mono.just(response));

        StepVerifier.create(client.findAvailableAdjusters())
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    @DisplayName("findAvailableAdjusters falls back to empty Flux on network I/O error without failing")
    void findAvailableAdjustersFallsBackToEmptyOnNetworkError() {
        responseSupplier.set(Mono.error(new IOException("Connection refused")));

        StepVerifier.create(client.findAvailableAdjusters())
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void findAvailableAdjustersFiltersNullEntriesAndNullUserIds() {
        UUID valid = UUID.randomUUID();
        ClientResponse response = ClientResponse.create(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body("[{\"userId\": null}, {\"userId\": \"" + valid + "\"}]")
                .build();
        responseSupplier.set(Mono.just(response));

        StepVerifier.create(client.findAvailableAdjusters())
                .expectNext(valid)
                .verifyComplete();
    }

    @Test
    void findAvailableAdjustersSkipsNullArrayEntries() {
        ClientResponse response = ClientResponse.create(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body("[null]")
                .build();
        responseSupplier.set(Mono.just(response));

        StepVerifier.create(client.findAvailableAdjusters()).verifyComplete();
    }
}
