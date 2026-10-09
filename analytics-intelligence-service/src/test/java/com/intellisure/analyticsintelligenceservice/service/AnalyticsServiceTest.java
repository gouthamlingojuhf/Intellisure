package com.intellisure.analyticsintelligenceservice.service;

import com.intellisure.analyticsintelligenceservice.dto.GenerateRiskScoreRequest;
import com.intellisure.analyticsintelligenceservice.entity.ExecutiveDashboardSummary;
import com.intellisure.analyticsintelligenceservice.entity.LossRatioMetrics;
import com.intellisure.analyticsintelligenceservice.entity.LossTriangle;
import com.intellisure.analyticsintelligenceservice.exception.AnalyticsCapabilityUnavailableException;
import com.intellisure.analyticsintelligenceservice.exception.AnalyticsDataUnavailableException;
import com.intellisure.analyticsintelligenceservice.repository.ExecutiveDashboardSummaryRepository;
import com.intellisure.analyticsintelligenceservice.repository.LossRatioMetricsRepository;
import com.intellisure.analyticsintelligenceservice.repository.LossTriangleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock LossRatioMetricsRepository lossRatioRepository;
    @Mock LossTriangleRepository lossTriangleRepository;
    @Mock ExecutiveDashboardSummaryRepository dashboardRepository;

    private AnalyticsService service;

    @BeforeEach
    void setUp() {
        service = new AnalyticsService(lossRatioRepository, lossTriangleRepository, dashboardRepository);
    }

    @Test
    void refusesToFabricateRiskScoreWithoutUnderwritingData() {
        StepVerifier.create(service.generateRiskScore(new GenerateRiskScoreRequest(UUID.randomUUID())))
                .expectError(AnalyticsCapabilityUnavailableException.class).verify();
    }

    @Test
    void mapsLatestLossRatioMetrics() {
        UUID id = UUID.randomUUID();
        LocalDateTime calculatedAt = LocalDateTime.now();
        LossRatioMetrics metrics = LossRatioMetrics.builder()
                .metricsId(id).totalEarnedPremium(new BigDecimal("1000.00"))
                .totalIncurredClaims(new BigDecimal("250.00"))
                .lossAdjustmentExpenses(new BigDecimal("40.00"))
                .lossRatioPercentage(new BigDecimal("29.00"))
                .activePolicyCount(12).totalClaimsFiled(4).calculatedAt(calculatedAt)
                .periodStart(calculatedAt.minusDays(30)).periodEnd(calculatedAt).build();
        when(lossRatioRepository.findFirstByOrderByCalculatedAtDesc()).thenReturn(Mono.just(metrics));

        StepVerifier.create(service.getLatestLossRatioMetrics())
                .assertNext(response -> {
                    org.assertj.core.api.Assertions.assertThat(response.metricsId()).isEqualTo(id);
                    org.assertj.core.api.Assertions.assertThat(response.lossRatioPercentage()).isEqualByComparingTo("29.00");
                    org.assertj.core.api.Assertions.assertThat(response.totalClaimsFiled()).isEqualTo(4);
                }).verifyComplete();
    }

    @Test
    void reportsUnavailableLossRatioMetrics() {
        when(lossRatioRepository.findFirstByOrderByCalculatedAtDesc()).thenReturn(Mono.empty());

        StepVerifier.create(service.getLatestLossRatioMetrics())
                .expectErrorSatisfies(error -> org.assertj.core.api.Assertions.assertThat(error)
                        .isInstanceOf(AnalyticsDataUnavailableException.class)
                        .hasMessage("No loss-ratio metrics are available"))
                .verify();
    }

    @Test
    void usesRequestedYearForLossTriangle() {
        LossTriangle triangle = LossTriangle.builder().triangleId(UUID.randomUUID())
                .accidentYear(2024).developmentYear(1)
                .cumulativeIncurredClaims(new BigDecimal("100.00"))
                .cumulativePaidClaims(new BigDecimal("50.00"))
                .caseReserves(new BigDecimal("30.00")).ibnrReserves(new BigDecimal("20.00"))
                .calculatedAt(LocalDateTime.now()).build();
        when(lossTriangleRepository.findByAccidentYearOrderByDevelopmentYear(2024))
                .thenReturn(Flux.just(triangle));

        StepVerifier.create(service.getLossTriangle(2024))
                .assertNext(response -> {
                    org.assertj.core.api.Assertions.assertThat(response.accidentYear()).isEqualTo(2024);
                    org.assertj.core.api.Assertions.assertThat(response.items()).hasSize(1);
                    org.assertj.core.api.Assertions.assertThat(response.items().get(0).developmentYear()).isEqualTo(1);
                }).verifyComplete();
        verify(lossTriangleRepository).findByAccidentYearOrderByDevelopmentYear(2024);
    }

    @Test
    void defaultsLossTriangleYearWhenRequestYearIsMissing() {
        int expectedYear = LocalDateTime.now().getYear() - 5;
        when(lossTriangleRepository.findByAccidentYearOrderByDevelopmentYear(expectedYear))
                .thenReturn(Flux.empty());

        StepVerifier.create(service.getLossTriangle(null))
                .assertNext(response -> {
                    org.assertj.core.api.Assertions.assertThat(response.accidentYear()).isEqualTo(expectedYear);
                    org.assertj.core.api.Assertions.assertThat(response.items()).isEmpty();
                }).verifyComplete();
    }

    @Test
    void mapsLatestDashboardSummary() {
        UUID id = UUID.randomUUID();
        ExecutiveDashboardSummary summary = ExecutiveDashboardSummary.builder().summaryId(id)
                .totalWrittenPremium(new BigDecimal("5000.00")).totalEarnedPremium(new BigDecimal("4000.00"))
                .totalIncurredLosses(new BigDecimal("1200.00")).lossRatioPercentage(new BigDecimal("30.00"))
                .claimsFrequency(new BigDecimal("0.25")).netSubrogationYield(new BigDecimal("100.00"))
                .activePolicyCount(20).totalClaimsFiled(8).openClaimsCount(3).closedClaimsCount(5)
                .calculatedAt(LocalDateTime.now()).periodStart(LocalDateTime.now().minusMonths(1))
                .periodEnd(LocalDateTime.now()).build();
        when(dashboardRepository.findFirstByOrderByCalculatedAtDesc()).thenReturn(Mono.just(summary));

        StepVerifier.create(service.getDashboardSummary())
                .assertNext(response -> {
                    org.assertj.core.api.Assertions.assertThat(response.summaryId()).isEqualTo(id);
                    org.assertj.core.api.Assertions.assertThat(response.openClaimsCount()).isEqualTo(3);
                }).verifyComplete();
    }

    @Test
    void reportsUnavailableDashboardSummary() {
        when(dashboardRepository.findFirstByOrderByCalculatedAtDesc()).thenReturn(Mono.empty());

        StepVerifier.create(service.getDashboardSummary())
                .expectError(AnalyticsDataUnavailableException.class).verify();
    }
}
