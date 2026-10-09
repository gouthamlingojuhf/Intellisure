package com.intellisure.analyticsintelligenceservice.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AnalyticsEntityContractTest {
    private final UUID id = UUID.randomUUID();
    private final LocalDateTime now = LocalDateTime.now();

    @Test
    void lossRatioMetricsBuilderAndAccessorsRemainComplete() {
        LossRatioMetrics entity = LossRatioMetrics.builder().metricsId(id)
                .totalEarnedPremium(BigDecimal.ONE).totalIncurredClaims(BigDecimal.TEN)
                .lossAdjustmentExpenses(BigDecimal.ONE).lossRatioPercentage(BigDecimal.TEN)
                .activePolicyCount(2).totalClaimsFiled(3).calculatedAt(now)
                .periodStart(now.minusDays(1)).periodEnd(now).isNew(false).build();

        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.isNew()).isFalse();
        assertThat(entity.getTotalEarnedPremium()).isEqualTo(BigDecimal.ONE);
        entity.setMetricsId(UUID.randomUUID());
        entity.setTotalEarnedPremium(BigDecimal.ZERO);
        entity.setTotalIncurredClaims(BigDecimal.ZERO);
        entity.setLossAdjustmentExpenses(BigDecimal.ZERO);
        entity.setLossRatioPercentage(BigDecimal.ZERO);
        entity.setActivePolicyCount(0);
        entity.setTotalClaimsFiled(0);
        entity.setCalculatedAt(now);
        entity.setPeriodStart(now);
        entity.setPeriodEnd(now);
        entity.setNew(true);
        assertThat(entity.isNew()).isTrue();
    }

    @Test
    void lossTriangleBuilderAndAccessorsRemainComplete() {
        LossTriangle entity = LossTriangle.builder().triangleId(id).accidentYear(2024).developmentYear(1)
                .cumulativeIncurredClaims(BigDecimal.ONE).cumulativePaidClaims(BigDecimal.TEN)
                .caseReserves(BigDecimal.ONE).ibnrReserves(BigDecimal.TEN).calculatedAt(now)
                .isNew(false).build();

        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.isNew()).isFalse();
        entity.setTriangleId(UUID.randomUUID());
        entity.setAccidentYear(2025);
        entity.setDevelopmentYear(2);
        entity.setCumulativeIncurredClaims(BigDecimal.ZERO);
        entity.setCumulativePaidClaims(BigDecimal.ZERO);
        entity.setCaseReserves(BigDecimal.ZERO);
        entity.setIbnrReserves(BigDecimal.ZERO);
        entity.setCalculatedAt(now);
        entity.setNew(true);
        assertThat(entity.getAccidentYear()).isEqualTo(2025);
        assertThat(entity.isNew()).isTrue();
    }

    @Test
    void executiveSummaryBuilderAndAccessorsRemainComplete() {
        ExecutiveDashboardSummary entity = ExecutiveDashboardSummary.builder().summaryId(id)
                .totalWrittenPremium(BigDecimal.ONE).totalEarnedPremium(BigDecimal.ONE)
                .totalIncurredLosses(BigDecimal.TEN).lossRatioPercentage(BigDecimal.ONE)
                .claimsFrequency(BigDecimal.ONE).netSubrogationYield(BigDecimal.ONE)
                .activePolicyCount(1).totalClaimsFiled(2).openClaimsCount(1).closedClaimsCount(1)
                .calculatedAt(now).periodStart(now.minusDays(1)).periodEnd(now).isNew(false).build();

        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.isNew()).isFalse();
        entity.setSummaryId(UUID.randomUUID());
        entity.setTotalWrittenPremium(BigDecimal.ZERO);
        entity.setTotalEarnedPremium(BigDecimal.ZERO);
        entity.setTotalIncurredLosses(BigDecimal.ZERO);
        entity.setLossRatioPercentage(BigDecimal.ZERO);
        entity.setClaimsFrequency(BigDecimal.ZERO);
        entity.setNetSubrogationYield(BigDecimal.ZERO);
        entity.setActivePolicyCount(0);
        entity.setTotalClaimsFiled(0);
        entity.setOpenClaimsCount(0);
        entity.setClosedClaimsCount(0);
        entity.setCalculatedAt(now);
        entity.setPeriodStart(now);
        entity.setPeriodEnd(now);
        entity.setNew(true);
        assertThat(entity.isNew()).isTrue();
    }

    @Test
    void riskScoreSnapshotAccessorsRemainComplete() {
        RiskScoreSnapshot entity = RiskScoreSnapshot.builder().snapshotId(id).customerId(id)
                .riskScore(BigDecimal.TEN).riskBand("LOW").keyFactors("none")
                .modelVersion("v1").generatedAt(now).isNew(false).build();

        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getCustomerId()).isEqualTo(id);
        assertThat(entity.getSnapshotId()).isEqualTo(id);
        assertThat(entity.getRiskScore()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(entity.getRiskBand()).isEqualTo("LOW");
        assertThat(entity.getKeyFactors()).isEqualTo("none");
        assertThat(entity.getModelVersion()).isEqualTo("v1");
        assertThat(entity.getGeneratedAt()).isEqualTo(now);
        assertThat(entity.isNew()).isFalse();
        entity.setSnapshotId(UUID.randomUUID());
        entity.setCustomerId(UUID.randomUUID());
        entity.setRiskScore(BigDecimal.ZERO);
        entity.setRiskBand("UNKNOWN");
        entity.setKeyFactors("updated");
        entity.setModelVersion("v2");
        entity.setGeneratedAt(now);
        entity.setNew(true);
        assertThat(entity.isNew()).isTrue();
    }
}
