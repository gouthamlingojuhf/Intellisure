package com.intellisure.vendorpartnerservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class CustomerPartyClient {

    private final WebClient webClient;

    public CustomerPartyClient(WebClient.Builder loadBalancedWebClientBuilder) {
        this.webClient = loadBalancedWebClientBuilder
                .baseUrl("http://customer-party-service")
                .build();
    }

    public Mono<Void> promoteVendorByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Mono.empty();
        }
        log.info("Promoting user account with email {} to ROLE_VENDOR", email);
        return webClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/users/promote-vendor")
                        .queryParam("email", email)
                        .build())
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(v -> log.info("Successfully promoted {} to ROLE_VENDOR", email))
                .doOnError(err -> log.warn("Failed to promote {} to ROLE_VENDOR in customer-party-service: {}", email, err.getMessage()))
                .onErrorResume(err -> Mono.empty());
    }
}
