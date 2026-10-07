package com.intellisure.claimsservice.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Slf4j
public class CustomerPartyAdjusterClient {

    private final WebClient webClient;

    public CustomerPartyAdjusterClient(WebClient.Builder loadBalancedWebClientBuilder) {
        this.webClient = loadBalancedWebClientBuilder
                .baseUrl("http://customer-party-service")
                .build();
    }

    public Flux<UUID> findAvailableAdjusters() {
        Mono<AdjusterSummary[]> payload = webClient.get()
                .uri("/api/users/role/CLAIMS_ADJUSTER/available")
                .retrieve()
                .bodyToMono(AdjusterSummary[].class)
                .doOnError(err -> log.warn("Adjuster lookup from customer-party-service failed: {}", err.getMessage()))
                .onErrorResume(err -> Mono.just(new AdjusterSummary[0]))
                .switchIfEmpty(Mono.just(new AdjusterSummary[0]));

        return payload.flatMapMany(Flux::fromArray)
                .filter(user -> user != null && user.userId() != null)
                .map(AdjusterSummary::userId)
                .onErrorResume(err -> Flux.empty());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AdjusterSummary(UUID userId, String role, String accountStatus) {
    }
}
