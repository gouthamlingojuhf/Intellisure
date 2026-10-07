package com.intellisure.recoveryservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class VendorPartnerClient {

    private final WebClient webClient;

    public VendorPartnerClient(WebClient.Builder loadBalancedWebClientBuilder) {
        this.webClient = loadBalancedWebClientBuilder
                .baseUrl("http://vendor-partner-service")
                .build();
    }

    public Mono<Map> createVendorAssignment(UUID vendorId, UUID claimId, UUID recoveryCaseId,
                                            String taskDescription, LocalDate dueDate) {
        log.info("Dispatching optional network vendor assignment for vendorId={}, caseId={}", vendorId, recoveryCaseId);

        Map<String, Object> body = new java.util.HashMap<>();
        body.put("vendorId", vendorId);
        body.put("assignmentType", "RESTORATION");
        body.put("claimId", claimId != null ? claimId : UUID.randomUUID());
        body.put("recoveryCaseId", recoveryCaseId);
        body.put("recoveryPath", "NETWORK_VENDOR");
        body.put("taskDescription", taskDescription != null ? taskDescription : "Post-loss business restoration and recovery");
        body.put("dueDate", dueDate != null ? dueDate.toString() : LocalDate.now().plusWeeks(2).toString());
        body.put("priority", "HIGH");

        return webClient.post()
                .uri("/api/vendor-assignments")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .doOnError(err -> log.warn("Vendor assignment dispatch failed (non-blocking for recovery flow): {}", err.getMessage()))
                .onErrorResume(err -> Mono.empty());
    }
}
