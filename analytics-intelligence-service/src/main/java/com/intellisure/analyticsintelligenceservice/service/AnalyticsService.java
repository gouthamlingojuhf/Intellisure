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
import com.intellisure.analyticsintelligenceservice.entity.RiskScoreSnapshot;
import com.intellisure.analyticsintelligenceservice.repository.ExecutiveDashboardSummaryRepository;
import com.intellisure.analyticsintelligenceservice.repository.LossRatioMetricsRepository;
import com.intellisure.analyticsintelligenceservice.repository.LossTriangleRepository;
import com.intellisure.analyticsintelligenceservice.repository.RiskScoreSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final RiskScoreSnapshotRepository riskScoreSnapshotRepository;
    private final LossRatioMetricsRepository lossRatioMetricsRepository;
    private final LossTriangleRepository lossTriangleRepository;
    private final ExecutiveDashboardSummaryRepository executiveDashboardSummaryRepository;

    public Mono<RiskScoreResponse> generateRiskScore(GenerateRiskScoreRequest request) {
        LocalDateTime now = LocalDateTime.now();

        RiskScoreSnapshot snapshot = RiskScoreSnapshot.builder()
                .snapshotId(UUID.randomUUID())
                .customerId(request.customerId())
                .riskScore(new BigDecimal("75.5"))
                .riskBand("MEDIUM")
                .keyFactors("Recent claims history, Location risk")
                .modelVersion("v1.2.0")
                .generatedAt(now)
                .isNew(true)
                .build();

        return riskScoreSnapshotRepository.save(snapshot)
                .map(this::mapToRiskScoreResponse);
    }

    public Mono<LossRatioMetricsResponse> calculateLossRatioMetrics() {
        log.info("Calculating loss ratio metrics");

        // In a real implementation, this would query from claims and policy services
        // For now, we use mock data that would come from other services
        BigDecimal totalEarnedPremium = new BigDecimal("10000000.00"); // $10M
        BigDecimal totalIncurredClaims = new BigDecimal("4500000.00"); // $4.5M
        BigDecimal lossAdjustmentExpenses = new BigDecimal("500000.00"); // $500K
        Integer activePolicyCount = 5000;
        Integer totalClaimsFiled = 1250;

        // Loss Ratio = (Incurred Claims + LAE) / Earned Premium * 100
        BigDecimal totalLosses = totalIncurredClaims.add(lossAdjustmentExpenses);
        BigDecimal lossRatioPercentage = totalLosses
                .divide(totalEarnedPremium, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime periodStart = now.minusYears(1);
        LocalDateTime periodEnd = now;

        LossRatioMetrics metrics = LossRatioMetrics.builder()
                .metricsId(UUID.randomUUID())
                .totalEarnedPremium(totalEarnedPremium)
                .totalIncurredClaims(totalIncurredClaims)
                .lossAdjustmentExpenses(lossAdjustmentExpenses)
                .lossRatioPercentage(lossRatioPercentage)
                .activePolicyCount(activePolicyCount)
                .totalClaimsFiled(totalClaimsFiled)
                .calculatedAt(LocalDateTime.now())
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .isNew(true)
                .build();

        return lossRatioMetricsRepository.save(metrics)
                .map(this::mapToLossRatioResponse);
    }

    public Mono<LossRatioMetricsResponse> getLatestLossRatioMetrics() {
        return lossRatioMetricsRepository.findFirstByOrderByCalculatedAtDesc()
                .map(this::mapToLossRatioResponse)
                .switchIfEmpty(Mono.defer(this::calculateLossRatioMetrics));
    }

    public Mono<LossTriangleListResponse> getLossTriangle(Integer accidentYear) {
        log.info("Generating loss triangle for accident year: {}", accidentYear);

        final Integer finalAccidentYear = (accidentYear == null) ? LocalDateTime.now().getYear() - 5 : accidentYear;

        return lossTriangleRepository.findByAccidentYearOrderByDevelopmentYear(finalAccidentYear)
                .map(this::mapToLossTriangleResponse)
                .collectList()
                .map(items -> new LossTriangleListResponse(items, finalAccidentYear))
                .switchIfEmpty(Mono.defer(() -> generateMockLossTriangle(finalAccidentYear)));
    }

    public Mono<ExecutiveDashboardSummaryResponse> getDashboardSummary() {
        log.info("Generating executive dashboard summary");

        return executiveDashboardSummaryRepository.findFirstByOrderByCalculatedAtDesc()
                .map(this::mapToDashboardResponse)
                .switchIfEmpty(Mono.defer(this::generateMockDashboardSummary));
    }

    private Mono<ExecutiveDashboardSummaryResponse> generateMockDashboardSummary() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime periodStart = now.minusYears(1);
        LocalDateTime periodEnd = now;

        ExecutiveDashboardSummary summary = ExecutiveDashboardSummary.builder()
                .summaryId(UUID.randomUUID())
                .totalWrittenPremium(new BigDecimal("12500000.00"))
                .totalEarnedPremium(new BigDecimal("10000000.00"))
                .totalIncurredLosses(new BigDecimal("5500000.00"))
                .lossRatioPercentage(new BigDecimal("55.00"))
                .claimsFrequency(new BigDecimal("2.5"))
                .netSubrogationYield(new BigDecimal("350000.00"))
                .activePolicyCount(5000)
                .totalClaimsFiled(1250)
                .openClaimsCount(320)
                .closedClaimsCount(930)
                .calculatedAt(now)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .isNew(true)
                .build();

        return executiveDashboardSummaryRepository.save(summary)
                .map(this::mapToDashboardResponse);
    }

    private Mono<LossTriangleListResponse> generateMockLossTriangle(Integer accidentYear) {
        Flux<LossTriangle> triangles = Flux.range(1, 5)
                .map(devYear -> {
                    int baseAmount = 1000000 * accidentYear / 10; // Simplified calculation
                    BigDecimal incurred = BigDecimal.valueOf(baseAmount * devYear * 0.8);
                    BigDecimal paid = BigDecimal.valueOf(baseAmount * devYear * 0.6);
                    BigDecimal caseReserves = BigDecimal.valueOf(baseAmount * devYear * 0.15);
                    BigDecimal ibnr = BigDecimal.valueOf(baseAmount * devYear * 0.05);

                    return LossTriangle.builder()
                            .triangleId(UUID.randomUUID())
                            .accidentYear(accidentYear)
                            .developmentYear(devYear)
                            .cumulativeIncurredClaims(incurred)
                            .cumulativePaidClaims(paid)
                            .caseReserves(caseReserves)
                            .ibnrReserves(ibnr)
                            .calculatedAt(LocalDateTime.now())
                            .isNew(true)
                            .build();
                });

        return triangles
                .flatMap(lossTriangleRepository::save)
                .map(this::mapToLossTriangleResponse)
                .collectList()
                .map(items -> new LossTriangleListResponse(items, accidentYear));
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

    private RiskScoreResponse mapToRiskScoreResponse(RiskScoreSnapshot snapshot) {
        return new RiskScoreResponse(
                snapshot.getSnapshotId(),
                snapshot.getCustomerId(),
                snapshot.getRiskScore(),
                snapshot.getRiskBand(),
                snapshot.getKeyFactors(),
                snapshot.getModelVersion(),
                snapshot.getGeneratedAt()
        );
    }
}