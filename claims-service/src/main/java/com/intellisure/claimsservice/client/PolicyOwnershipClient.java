package com.intellisure.claimsservice.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.intellisure.claimsservice.exception.AccessDeniedBusinessException;
import com.intellisure.claimsservice.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class PolicyOwnershipClient {

    private final WebClient webClient;

    public PolicyOwnershipClient(WebClient.Builder loadBalancedWebClientBuilder) {
        this.webClient = loadBalancedWebClientBuilder
                .baseUrl("http://quote-policy-service")
                .build();
    }

    public Mono<Void> assertPolicyOwnership(UUID policyId, UUID customerId) {
        return webClient.get()
                .uri("/api/policies/{policyId}", policyId)
                .retrieve()
                .onStatus(status -> status.value() == 401 || status.value() == 403,
                        response -> Mono.error(new AccessDeniedBusinessException(
                                "The authenticated customer cannot file a claim for this policy"
                        )))
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> Mono.error(new ResourceNotFoundException(
                                "The policy could not be verified for claim filing"
                        )))
                .bodyToMono(PolicySummary.class)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "The policy could not be verified for claim filing"
                )))
                .flatMap(policy -> {
                    if (!customerId.equals(policy.customerId())) {
                        return Mono.error(new AccessDeniedBusinessException(
                                "The authenticated customer cannot file a claim for this policy"
                        ));
                    }
                    return Mono.empty();
                });
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record PolicySummary(UUID customerId) {}
}
