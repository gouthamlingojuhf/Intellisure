package com.intellisure.riskunderwritingservice.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkflowNotificationClient {

    private final WebClient.Builder loadBalancedWebClientBuilder;

    public Mono<Void> notifyRiskEngineerAssignment(UUID engineerId, UUID assessmentId, String assessmentNumber) {
        if (engineerId == null) {
            return Mono.empty();
        }
        Map<String, Object> notification = Map.of(
                "userId", engineerId,
                "type", "RISK_ASSESSMENT_ASSIGNED",
                "title", "New Risk Assessment Inspection Assigned",
                "message", "You have been assigned to conduct a risk engineering inspection for assessment "
                        + (assessmentNumber != null ? assessmentNumber : assessmentId),
                "referenceType", "RISK_ASSESSMENT",
                "referenceId", assessmentId,
                "channel", "IN_APP"
        );

        return loadBalancedWebClientBuilder.build()
                .post()
                .uri("http://workflow-notification-service/api/notifications")
                .bodyValue(notification)
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorResume(e -> {
                    log.warn("Failed to dispatch workflow notification for risk engineer assignment: {}", e.getMessage());
                    return Mono.empty();
                });
    }
}
