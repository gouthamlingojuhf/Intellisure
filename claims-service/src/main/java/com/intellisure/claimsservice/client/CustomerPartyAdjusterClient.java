package com.intellisure.claimsservice.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CustomerPartyAdjusterClient {

    private final WebClient webClient;

    public CustomerPartyAdjusterClient(WebClient.Builder loadBalancedWebClientBuilder) {
        this.webClient = loadBalancedWebClientBuilder
                .baseUrl("http://customer-party-service")
                .build();
    }

    public Flux<UUID> findAvailableAdjusters() {
        Mono<AdjusterSummary[]> payload = webClient.get()
                .uri("/api/users/role/CLAIMS_ADJUSTER")
                .retrieve()
                .bodyToMono(AdjusterSummary[].class)
                .switchIfEmpty(Mono.just(new AdjusterSummary[0]));

        return payload.flatMapMany(Flux::fromArray)
                .filter(user -> user != null && user.userId() != null)
                .map(AdjusterSummary::userId);
    }

    public record AdjusterSummary(UUID userId, String role, String accountStatus) {
    }
}
