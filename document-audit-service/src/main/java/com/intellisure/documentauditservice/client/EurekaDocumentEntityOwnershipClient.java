package com.intellisure.documentauditservice.client;

import com.intellisure.documentauditservice.exception.AccessDeniedBusinessException;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class EurekaDocumentEntityOwnershipClient implements DocumentEntityOwnershipClient {

    private final WebClient.Builder webClientBuilder;

    public EurekaDocumentEntityOwnershipClient(@LoadBalanced WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public Mono<UUID> findCustomerId(UUID entityId, String entityType) {
        String normalizedType = entityType == null ? "" : entityType.trim().toUpperCase();
        String serviceName;
        String resourcePath;

        switch (normalizedType) {
            case "QUOTE" -> {
                serviceName = "quote-policy-service";
                resourcePath = "/api/quotes/" + entityId;
            }
            case "POLICY" -> {
                serviceName = "quote-policy-service";
                resourcePath = "/api/policies/" + entityId;
            }
            case "CLAIM" -> {
                serviceName = "claims-service";
                resourcePath = "/api/claims/" + entityId;
            }
            default -> {
                return Mono.error(new AccessDeniedBusinessException(
                        "Policyholders cannot access documents for this entity type"));
            }
        }

        return webClientBuilder.build()
                .get()
                .uri("lb://" + serviceName + resourcePath)
                .retrieve()
                .bodyToMono(OwnedEntityResponse.class)
                .map(OwnedEntityResponse::customerId)
                .switchIfEmpty(Mono.error(new AccessDeniedBusinessException("Document entity ownership could not be verified")))
                .onErrorMap(WebClientResponseException.class,
                        ex -> new AccessDeniedBusinessException("Document entity ownership could not be verified"));
    }

    private record OwnedEntityResponse(UUID customerId) {}
}
