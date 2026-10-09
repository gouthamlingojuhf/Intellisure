package com.intellisure.analyticsintelligenceservice.service;

import com.intellisure.analyticsintelligenceservice.dto.GenerateRiskScoreRequest;
import com.intellisure.analyticsintelligenceservice.exception.AnalyticsCapabilityUnavailableException;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;
import java.util.UUID;

class AnalyticsServiceTest {
    private final AnalyticsService service = new AnalyticsService(null, null, null);

    @Test
    void refusesToFabricateRiskScoreWithoutUnderwritingData() {
        StepVerifier.create(service.generateRiskScore(new GenerateRiskScoreRequest(UUID.randomUUID())))
                .expectError(AnalyticsCapabilityUnavailableException.class).verify();
    }
}
