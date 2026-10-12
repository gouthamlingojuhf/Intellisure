package com.intellisure.quotepolicyservice.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Slf4j
public class CustomerPartyClient {

    private final WebClient webClient;

    public CustomerPartyClient(WebClient.Builder loadBalancedWebClientBuilder) {
        this.webClient = loadBalancedWebClientBuilder
                .baseUrl("http://customer-party-service")
                .build();
    }

    public Flux<UUID> findAvailableEmployeesByRole(String role) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/users/available")
                        .queryParam("role", role)
                        .queryParam("status", "ACTIVE")
                        .build())
                .retrieve()
                .bodyToFlux(EmployeeSummary.class)
                .doOnError(err -> log.warn("Employee lookup for role {} from customer-party-service failed: {}", role, err.getMessage()))
                .onErrorResume(err -> Flux.empty())
                .filter(user -> user != null && user.userId() != null)
                .map(EmployeeSummary::userId);
    }

    public Mono<UUID> findCustomerIdByUserId(UUID userId) {
        return findCustomerIdByUserId(userId, null);
    }

    public Mono<UUID> findCustomerIdByUserId(UUID userId, String tokenValue) {
        log.info("Querying customer-party-service for userId={}", userId);
        var spec = webClient.get()
                .uri("/api/customers/by-user/{userId}", userId);

        if (tokenValue != null && !tokenValue.isBlank()) {
            spec = spec.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValue);
        }

        return spec.retrieve()
                .bodyToMono(CustomerSummary.class)
                .map(CustomerSummary::customerId)
                .doOnNext(cid -> log.info("Resolved customerId={} for userId={}", cid, userId))
                .doOnError(err -> log.warn("Customer lookup failed for userId {}: {}", userId, err.getMessage()))
                .onErrorResume(WebClientResponseException.NotFound.class, err -> Mono.empty())
                .onErrorResume(err -> Mono.empty());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EmployeeSummary(UUID userId, String email, String role, String accountStatus) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CustomerSummary(UUID customerId, UUID userId) {
    }
}
