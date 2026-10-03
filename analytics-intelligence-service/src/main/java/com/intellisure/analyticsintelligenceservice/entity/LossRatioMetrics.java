package com.intellisure.analyticsintelligenceservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("loss_ratio_metrics")
public class LossRatioMetrics implements Persistable<UUID> {

    @Id
    private UUID metricsId;

    private BigDecimal totalEarnedPremium;
    private BigDecimal totalIncurredClaims;
    private BigDecimal lossAdjustmentExpenses;
    private BigDecimal lossRatioPercentage;
    private Integer activePolicyCount;
    private Integer totalClaimsFiled;

    private LocalDateTime calculatedAt;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;

    @Transient
    private boolean isNew = true;

    public static LossRatioMetrics builder() {
        return new LossRatioMetrics();
    }

    public LossRatioMetrics metricsId(UUID metricsId) { this.metricsId = metricsId; return this; }
    public LossRatioMetrics totalEarnedPremium(BigDecimal totalEarnedPremium) { this.totalEarnedPremium = totalEarnedPremium; return this; }
    public LossRatioMetrics totalIncurredClaims(BigDecimal totalIncurredClaims) { this.totalIncurredClaims = totalIncurredClaims; return this; }
    public LossRatioMetrics lossAdjustmentExpenses(BigDecimal lossAdjustmentExpenses) { this.lossAdjustmentExpenses = lossAdjustmentExpenses; return this; }
    public LossRatioMetrics lossRatioPercentage(BigDecimal lossRatioPercentage) { this.lossRatioPercentage = lossRatioPercentage; return this; }
    public LossRatioMetrics activePolicyCount(Integer activePolicyCount) { this.activePolicyCount = activePolicyCount; return this; }
    public LossRatioMetrics totalClaimsFiled(Integer totalClaimsFiled) { this.totalClaimsFiled = totalClaimsFiled; return this; }
    public LossRatioMetrics calculatedAt(LocalDateTime calculatedAt) { this.calculatedAt = calculatedAt; return this; }
    public LossRatioMetrics periodStart(LocalDateTime periodStart) { this.periodStart = periodStart; return this; }
    public LossRatioMetrics periodEnd(LocalDateTime periodEnd) { this.periodEnd = periodEnd; return this; }
    public LossRatioMetrics isNew(boolean isNew) { this.isNew = isNew; return this; }
    public LossRatioMetrics build() { return this; }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @Override
    public UUID getId() {
        return metricsId;
    }

    public UUID getMetricsId() { return metricsId; }
    public void setMetricsId(UUID metricsId) { this.metricsId = metricsId; }
    public BigDecimal getTotalEarnedPremium() { return totalEarnedPremium; }
    public void setTotalEarnedPremium(BigDecimal totalEarnedPremium) { this.totalEarnedPremium = totalEarnedPremium; }
    public BigDecimal getTotalIncurredClaims() { return totalIncurredClaims; }
    public void setTotalIncurredClaims(BigDecimal totalIncurredClaims) { this.totalIncurredClaims = totalIncurredClaims; }
    public BigDecimal getLossAdjustmentExpenses() { return lossAdjustmentExpenses; }
    public void setLossAdjustmentExpenses(BigDecimal lossAdjustmentExpenses) { this.lossAdjustmentExpenses = lossAdjustmentExpenses; }
    public BigDecimal getLossRatioPercentage() { return lossRatioPercentage; }
    public void setLossRatioPercentage(BigDecimal lossRatioPercentage) { this.lossRatioPercentage = lossRatioPercentage; }
    public Integer getActivePolicyCount() { return activePolicyCount; }
    public void setActivePolicyCount(Integer activePolicyCount) { this.activePolicyCount = activePolicyCount; }
    public Integer getTotalClaimsFiled() { return totalClaimsFiled; }
    public void setTotalClaimsFiled(Integer totalClaimsFiled) { this.totalClaimsFiled = totalClaimsFiled; }
    public LocalDateTime getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(LocalDateTime calculatedAt) { this.calculatedAt = calculatedAt; }
    public LocalDateTime getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDateTime periodStart) { this.periodStart = periodStart; }
    public LocalDateTime getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDateTime periodEnd) { this.periodEnd = periodEnd; }
    public void setNew(boolean isNew) { this.isNew = isNew; }
}