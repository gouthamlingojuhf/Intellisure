package com.intellisure.analyticsintelligenceservice.controller;

import com.intellisure.analyticsintelligenceservice.dto.ExecutiveDashboardSummaryResponse;
import com.intellisure.analyticsintelligenceservice.dto.GenerateRiskScoreRequest;
import com.intellisure.analyticsintelligenceservice.dto.LossRatioMetricsResponse;
import com.intellisure.analyticsintelligenceservice.dto.LossTriangleListResponse;
import com.intellisure.analyticsintelligenceservice.dto.RiskScoreResponse;
import com.intellisure.analyticsintelligenceservice.service.AnalyticsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsControllerTest {
    @Mock AnalyticsService analyticsService;
    @InjectMocks AnalyticsController controller;

    @Test
    void delegatesRiskScoreGeneration() {
        GenerateRiskScoreRequest request = new GenerateRiskScoreRequest(UUID.randomUUID());
        RiskScoreResponse response = new RiskScoreResponse(UUID.randomUUID(), request.customerId(),
                new BigDecimal("20"), "LOW", "none", "v1", LocalDateTime.now());
        when(analyticsService.generateRiskScore(request)).thenReturn(Mono.just(response));

        StepVerifier.create(controller.generateRiskScore(request)).expectNext(response).verifyComplete();
        verify(analyticsService).generateRiskScore(request);
    }

    @Test
    void delegatesAnalyticsReads() {
        LossRatioMetricsResponse lossRatio = new LossRatioMetricsResponse(UUID.randomUUID(),
                BigDecimal.ONE, BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ONE, 1, 1,
                LocalDateTime.now(), LocalDateTime.now().minusDays(1), LocalDateTime.now());
        LossTriangleListResponse triangle = new LossTriangleListResponse(List.of(), 2024);
        ExecutiveDashboardSummaryResponse summary = new ExecutiveDashboardSummaryResponse(
                UUID.randomUUID(), BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE,
                BigDecimal.ONE, BigDecimal.ONE, 1, 1, 1, 0, LocalDateTime.now(),
                LocalDateTime.now().minusDays(1), LocalDateTime.now());
        when(analyticsService.getLatestLossRatioMetrics()).thenReturn(Mono.just(lossRatio));
        when(analyticsService.getLossTriangle(2024)).thenReturn(Mono.just(triangle));
        when(analyticsService.getDashboardSummary()).thenReturn(Mono.just(summary));

        StepVerifier.create(controller.getLossRatioMetrics()).expectNext(lossRatio).verifyComplete();
        StepVerifier.create(controller.getLossTriangle(2024)).expectNext(triangle).verifyComplete();
        StepVerifier.create(controller.getDashboardSummary()).expectNext(summary).verifyComplete();
        verify(analyticsService).getLatestLossRatioMetrics();
        verify(analyticsService).getLossTriangle(2024);
        verify(analyticsService).getDashboardSummary();
    }
}
