package com.intellisure.quotepolicyservice.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

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

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EmployeeSummary(UUID userId, String email, String role, String accountStatus) {
    }
}
