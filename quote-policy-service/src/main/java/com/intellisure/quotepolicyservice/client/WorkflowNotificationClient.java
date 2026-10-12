package com.intellisure.quotepolicyservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class WorkflowNotificationClient {

    private final WebClient webClient;

    public WorkflowNotificationClient(WebClient.Builder loadBalancedWebClientBuilder) {
        this.webClient = loadBalancedWebClientBuilder
                .baseUrl("http://workflow-notification-service")
                .build();
    }

    public Mono<Void> sendNotification(UUID userId, String type, String title, String message, String refType, UUID refId) {
        if (userId == null) {
            return Mono.empty();
        }
        Map<String, Object> payload = Map.of(
                "userId", userId,
                "type", type,
                "title", title,
                "message", message,
                "referenceType", refType != null ? refType : "GENERAL",
                "referenceId", refId != null ? refId : UUID.randomUUID(),
                "channel", "IN_APP"
        );

        return webClient.post()
                .uri("/api/notifications")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorResume(e -> {
                    log.warn("Failed to dispatch workflow notification to userId {}: {}", userId, e.getMessage());
                    return Mono.empty();
                });
    }

    public Mono<Void> notifyQuoteSubmitted(UUID userId, UUID underwriterId, UUID quoteId, String quoteNumber) {
        Mono<Void> customerNotify = sendNotification(
                userId,
                "QUOTE_SUBMITTED",
                "Quote Application Received",
                "Your application for quote " + quoteNumber + " has been submitted for underwriting review.",
                "QUOTE",
                quoteId
        );

        Mono<Void> underwriterNotify = underwriterId != null ? sendNotification(
                underwriterId,
                "TASK_ASSIGNED",
                "New Quote Assigned",
                "Quote " + quoteNumber + " has been assigned to you for underwriting review.",
                "QUOTE",
                quoteId
        ) : Mono.empty();

        return customerNotify.then(underwriterNotify);
    }

    public Mono<Void> notifyQuoteOffered(UUID userId, UUID quoteId, String quoteNumber) {
        return sendNotification(
                userId,
                "QUOTE_ISSUED",
                "Quote Terms Offered",
                "Terms and premium for quote " + quoteNumber + " have been offered. You can now review and bind coverage.",
                "QUOTE",
                quoteId
        );
    }

    public Mono<Void> notifyPolicyIssued(UUID userId, UUID policyId, String policyNumber) {
        return sendNotification(
                userId,
                "POLICY_ISSUED",
                "Commercial Policy Active",
                "Commercial insurance policy " + policyNumber + " is now officially active.",
                "POLICY",
                policyId
        );
    }
}
