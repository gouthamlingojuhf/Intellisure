package com.intellisure.documentauditservice.client;

import com.intellisure.documentauditservice.exception.AccessDeniedBusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EurekaDocumentEntityOwnershipClientTest {
    private final UUID customerId = UUID.randomUUID();
    private final UUID entityId = UUID.randomUUID();

    @Test
    void resolvesSupportedEntityOwnershipThroughExistingServiceRoute() {
        var builder = WebClient.builder().exchangeFunction(request -> {
            assertThat(request.url().toString()).isEqualTo("lb://quote-policy-service/api/quotes/" + entityId);
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body("{\"customerId\":\"" + customerId + "\"}")
                    .build());
        });
        EurekaDocumentEntityOwnershipClient client = new EurekaDocumentEntityOwnershipClient(builder);

        StepVerifier.create(client.findCustomerId(entityId, " quote "))
                .expectNext(customerId).verifyComplete();
    }

    @Test
    void rejectsUnsupportedEntityType() {
        EurekaDocumentEntityOwnershipClient client = new EurekaDocumentEntityOwnershipClient(WebClient.builder());

        StepVerifier.create(client.findCustomerId(entityId, "VENDOR"))
                .expectErrorSatisfies(error -> assertThat(error)
                        .isInstanceOf(AccessDeniedBusinessException.class)
                        .hasMessage("Policyholders cannot access documents for this entity type"))
                .verify();
    }

    @Test
    void resolvesPolicyAndClaimRoutes() {
        var builder = WebClient.builder().exchangeFunction(request -> {
            String path = request.url().getPath();
            String expectedService = path.contains("policies")
                    ? "lb://quote-policy-service" : "lb://claims-service";
            assertThat(request.url().toString()).startsWith(expectedService);
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body("{\"customerId\":\"" + customerId + "\"}")
                    .build());
        });
        EurekaDocumentEntityOwnershipClient client = new EurekaDocumentEntityOwnershipClient(builder);

        StepVerifier.create(client.findCustomerId(entityId, "POLICY")).expectNext(customerId).verifyComplete();
        StepVerifier.create(client.findCustomerId(entityId, "CLAIM")).expectNext(customerId).verifyComplete();
    }

    @Test
    void reportsEmptyDownstreamResponseAndNullEntityType() {
        var builder = WebClient.builder().exchangeFunction(request ->
                Mono.just(ClientResponse.create(HttpStatus.OK).build()));
        EurekaDocumentEntityOwnershipClient client = new EurekaDocumentEntityOwnershipClient(builder);

        StepVerifier.create(client.findCustomerId(entityId, "QUOTE"))
                .expectErrorMessage("Document entity ownership could not be verified").verify();
        StepVerifier.create(client.findCustomerId(entityId, null))
                .expectErrorMessage("Policyholders cannot access documents for this entity type").verify();
    }

    @Test
    void mapsDownstreamHttpErrorsToAccessDenied() {
        var builder = WebClient.builder().exchangeFunction(request ->
                Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build()));
        EurekaDocumentEntityOwnershipClient client = new EurekaDocumentEntityOwnershipClient(builder);

        StepVerifier.create(client.findCustomerId(entityId, "CLAIM"))
                .expectErrorSatisfies(error -> assertThat(error)
                        .isInstanceOf(AccessDeniedBusinessException.class)
                        .hasMessage("Document entity ownership could not be verified"))
                .verify();
    }
}
