package com.intellisure.riskunderwritingservice.client;

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

    public Flux<UUID> findAvailableRiskEngineers() {
        return webClient.get()
                .uri("/api/users/role/RISK_ENGINEER/available")
                .retrieve()
                .bodyToFlux(EmployeeSummary.class)
                .doOnError(err -> log.warn("Risk engineer lookup from customer-party-service failed: {}", err.getMessage()))
                .onErrorResume(err -> Flux.empty())
                .filter(user -> user != null && user.userId() != null)
                .map(EmployeeSummary::userId);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EmployeeSummary(UUID userId, String email, String role, String accountStatus) {
    }
}
