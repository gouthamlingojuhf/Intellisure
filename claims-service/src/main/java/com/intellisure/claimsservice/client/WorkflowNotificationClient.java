package com.intellisure.claimsservice.client;

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
                "referenceType", refType != null ? refType : "CLAIM",
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

    public Mono<Void> notifyClaimCreated(UUID userId, UUID adjusterId, UUID claimId, String claimNumber) {
        Mono<Void> customerNotify = sendNotification(
                userId,
                "FNOL_RECEIVED",
                "Claim FNOL Registered",
                "Your incident report for claim " + claimNumber + " has been registered and is under review.",
                "CLAIM",
                claimId
        );

        Mono<Void> adjusterNotify = adjusterId != null ? sendNotification(
                adjusterId,
                "CLAIM_ASSIGNED",
                "New Claim Assigned",
                "Claim " + claimNumber + " has been assigned to you for loss assessment.",
                "CLAIM",
                claimId
        ) : Mono.empty();

        return customerNotify.then(adjusterNotify);
    }

    public Mono<Void> notifyAdjusterReviewed(UUID userId, UUID claimId, String claimNumber, java.math.BigDecimal payoutAmount) {
        String amt = payoutAmount != null ? "₹" + payoutAmount.toPlainString() : "pending settlement";
        return sendNotification(
                userId,
                "ADJUSTER_REVIEWED",
                "Claim Assessment Completed (" + claimNumber + ")",
                "Your claim " + claimNumber + " has been investigated and approved by the Claims Adjuster (" + amt + "). Awaiting final Claims Manager settlement governance.",
                "CLAIM",
                claimId
        );
    }

    public Mono<Void> notifyClaimDecision(UUID userId, UUID claimId, String claimNumber, String decision) {
        String type = "APPROVED".equalsIgnoreCase(decision) ? "CLAIM_APPROVED" : "CLAIM_REJECTED";
        return sendNotification(
                userId,
                type,
                "Claim Decision: " + decision,
                "Coverage decision for claim " + claimNumber + " is " + decision + ".",
                "CLAIM",
                claimId
        );
    }

    public Mono<Void> notifyClaimSettled(UUID userId, UUID claimId, String claimNumber) {
        return sendNotification(
                userId,
                "CLAIM_SETTLED",
                "Claim Settlement Disbursed",
                "Settlement payout for claim " + claimNumber + " has been processed.",
                "CLAIM",
                claimId
        );
    }
}
