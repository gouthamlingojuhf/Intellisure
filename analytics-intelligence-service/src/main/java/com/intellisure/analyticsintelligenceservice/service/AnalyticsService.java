package com.intellisure.analyticsintelligenceservice.service;

import com.intellisure.analyticsintelligenceservice.dto.ExecutiveDashboardSummaryResponse;
import com.intellisure.analyticsintelligenceservice.dto.GenerateRiskScoreRequest;
import com.intellisure.analyticsintelligenceservice.dto.LossRatioMetricsResponse;
import com.intellisure.analyticsintelligenceservice.dto.LossTriangleListResponse;
import com.intellisure.analyticsintelligenceservice.dto.LossTriangleResponse;
import com.intellisure.analyticsintelligenceservice.dto.RiskScoreResponse;
import com.intellisure.analyticsintelligenceservice.entity.ExecutiveDashboardSummary;
import com.intellisure.analyticsintelligenceservice.entity.LossRatioMetrics;
import com.intellisure.analyticsintelligenceservice.entity.LossTriangle;
import com.intellisure.analyticsintelligenceservice.exception.AnalyticsCapabilityUnavailableException;
import com.intellisure.analyticsintelligenceservice.exception.AnalyticsDataUnavailableException;
import com.intellisure.analyticsintelligenceservice.repository.ExecutiveDashboardSummaryRepository;
import com.intellisure.analyticsintelligenceservice.repository.LossRatioMetricsRepository;
import com.intellisure.analyticsintelligenceservice.repository.LossTriangleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final LossRatioMetricsRepository lossRatioMetricsRepository;
    private final LossTriangleRepository lossTriangleRepository;
    private final ExecutiveDashboardSummaryRepository executiveDashboardSummaryRepository;

    public Mono<RiskScoreResponse> generateRiskScore(GenerateRiskScoreRequest request) {
        return Mono.error(new AnalyticsCapabilityUnavailableException(
                "Risk scoring requires a configured underwriting data source"));
    }

    public Mono<LossRatioMetricsResponse> getLatestLossRatioMetrics() {
        return lossRatioMetricsRepository.findFirstByOrderByCalculatedAtDesc()
                .map(this::mapToLossRatioResponse)
                .switchIfEmpty(Mono.error(new AnalyticsDataUnavailableException("No loss-ratio metrics are available")));
    }

    public Mono<LossTriangleListResponse> getLossTriangle(Integer accidentYear) {
        log.info("Generating loss triangle for accident year: {}", accidentYear);

        final Integer finalAccidentYear = (accidentYear == null) ? LocalDateTime.now().getYear() - 5 : accidentYear;

        return lossTriangleRepository.findByAccidentYearOrderByDevelopmentYear(finalAccidentYear)
                .map(this::mapToLossTriangleResponse)
                .collectList()
                .map(items -> new LossTriangleListResponse(items, finalAccidentYear));
    }

    public Mono<ExecutiveDashboardSummaryResponse> getDashboardSummary() {
        log.info("Generating executive dashboard summary");

        return executiveDashboardSummaryRepository.findFirstByOrderByCalculatedAtDesc()
                .map(this::mapToDashboardResponse)
                .switchIfEmpty(Mono.error(new AnalyticsDataUnavailableException("No executive analytics summary is available")));
    }

    private LossRatioMetricsResponse mapToLossRatioResponse(LossRatioMetrics entity) {
        return new LossRatioMetricsResponse(
                entity.getMetricsId(),
                entity.getTotalEarnedPremium(),
                entity.getTotalIncurredClaims(),
                entity.getLossAdjustmentExpenses(),
                entity.getLossRatioPercentage(),
                entity.getActivePolicyCount(),
                entity.getTotalClaimsFiled(),
                entity.getCalculatedAt(),
                entity.getPeriodStart(),
                entity.getPeriodEnd()
        );
    }

    private LossTriangleResponse mapToLossTriangleResponse(LossTriangle entity) {
        return new LossTriangleResponse(
                entity.getTriangleId(),
                entity.getAccidentYear(),
                entity.getDevelopmentYear(),
                entity.getCumulativeIncurredClaims(),
                entity.getCumulativePaidClaims(),
                entity.getCaseReserves(),
                entity.getIbnrReserves(),
                entity.getCalculatedAt()
        );
    }

    private ExecutiveDashboardSummaryResponse mapToDashboardResponse(ExecutiveDashboardSummary entity) {
        return new ExecutiveDashboardSummaryResponse(
                entity.getSummaryId(),
                entity.getTotalWrittenPremium(),
                entity.getTotalEarnedPremium(),
                entity.getTotalIncurredLosses(),
                entity.getLossRatioPercentage(),
                entity.getClaimsFrequency(),
                entity.getNetSubrogationYield(),
                entity.getActivePolicyCount(),
                entity.getTotalClaimsFiled(),
                entity.getOpenClaimsCount(),
                entity.getClosedClaimsCount(),
                entity.getCalculatedAt(),
                entity.getPeriodStart(),
                entity.getPeriodEnd()
        );
    }

}
