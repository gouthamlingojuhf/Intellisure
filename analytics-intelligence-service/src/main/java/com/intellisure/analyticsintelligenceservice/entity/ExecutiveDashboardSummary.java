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
@Table("executive_dashboard_summary")
public class ExecutiveDashboardSummary implements Persistable<UUID> {

    @Id
    private UUID summaryId;

    private BigDecimal totalWrittenPremium;
    private BigDecimal totalEarnedPremium;
    private BigDecimal totalIncurredLosses;
    private BigDecimal lossRatioPercentage;
    private BigDecimal claimsFrequency;
    private BigDecimal netSubrogationYield;
    private Integer activePolicyCount;
    private Integer totalClaimsFiled;
    private Integer openClaimsCount;
    private Integer closedClaimsCount;

    private LocalDateTime calculatedAt;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;

    @Transient
    private boolean isNew = true;

    public static ExecutiveDashboardSummary builder() {
        return new ExecutiveDashboardSummary();
    }

    public ExecutiveDashboardSummary summaryId(UUID summaryId) { this.summaryId = summaryId; return this; }
    public ExecutiveDashboardSummary totalWrittenPremium(BigDecimal totalWrittenPremium) { this.totalWrittenPremium = totalWrittenPremium; return this; }
    public ExecutiveDashboardSummary totalEarnedPremium(BigDecimal totalEarnedPremium) { this.totalEarnedPremium = totalEarnedPremium; return this; }
    public ExecutiveDashboardSummary totalIncurredLosses(BigDecimal totalIncurredLosses) { this.totalIncurredLosses = totalIncurredLosses; return this; }
    public ExecutiveDashboardSummary lossRatioPercentage(BigDecimal lossRatioPercentage) { this.lossRatioPercentage = lossRatioPercentage; return this; }
    public ExecutiveDashboardSummary claimsFrequency(BigDecimal claimsFrequency) { this.claimsFrequency = claimsFrequency; return this; }
    public ExecutiveDashboardSummary netSubrogationYield(BigDecimal netSubrogationYield) { this.netSubrogationYield = netSubrogationYield; return this; }
    public ExecutiveDashboardSummary activePolicyCount(Integer activePolicyCount) { this.activePolicyCount = activePolicyCount; return this; }
    public ExecutiveDashboardSummary totalClaimsFiled(Integer totalClaimsFiled) { this.totalClaimsFiled = totalClaimsFiled; return this; }
    public ExecutiveDashboardSummary openClaimsCount(Integer openClaimsCount) { this.openClaimsCount = openClaimsCount; return this; }
    public ExecutiveDashboardSummary closedClaimsCount(Integer closedClaimsCount) { this.closedClaimsCount = closedClaimsCount; return this; }
    public ExecutiveDashboardSummary calculatedAt(LocalDateTime calculatedAt) { this.calculatedAt = calculatedAt; return this; }
    public ExecutiveDashboardSummary periodStart(LocalDateTime periodStart) { this.periodStart = periodStart; return this; }
    public ExecutiveDashboardSummary periodEnd(LocalDateTime periodEnd) { this.periodEnd = periodEnd; return this; }
    public ExecutiveDashboardSummary isNew(boolean isNew) { this.isNew = isNew; return this; }
    public ExecutiveDashboardSummary build() { return this; }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @Override
    public UUID getId() {
        return summaryId;
    }

    public UUID getSummaryId() { return summaryId; }
    public void setSummaryId(UUID summaryId) { this.summaryId = summaryId; }
    public java.math.BigDecimal getTotalWrittenPremium() { return totalWrittenPremium; }
    public void setTotalWrittenPremium(java.math.BigDecimal totalWrittenPremium) { this.totalWrittenPremium = totalWrittenPremium; }
    public java.math.BigDecimal getTotalEarnedPremium() { return totalEarnedPremium; }
    public void setTotalEarnedPremium(java.math.BigDecimal totalEarnedPremium) { this.totalEarnedPremium = totalEarnedPremium; }
    public java.math.BigDecimal getTotalIncurredLosses() { return totalIncurredLosses; }
    public void setTotalIncurredLosses(java.math.BigDecimal totalIncurredLosses) { this.totalIncurredLosses = totalIncurredLosses; }
    public java.math.BigDecimal getLossRatioPercentage() { return lossRatioPercentage; }
    public void setLossRatioPercentage(java.math.BigDecimal lossRatioPercentage) { this.lossRatioPercentage = lossRatioPercentage; }
    public java.math.BigDecimal getClaimsFrequency() { return claimsFrequency; }
    public void setClaimsFrequency(java.math.BigDecimal claimsFrequency) { this.claimsFrequency = claimsFrequency; }
    public java.math.BigDecimal getNetSubrogationYield() { return netSubrogationYield; }
    public void setNetSubrogationYield(java.math.BigDecimal netSubrogationYield) { this.netSubrogationYield = netSubrogationYield; }
    public Integer getActivePolicyCount() { return activePolicyCount; }
    public void setActivePolicyCount(Integer activePolicyCount) { this.activePolicyCount = activePolicyCount; }
    public Integer getTotalClaimsFiled() { return totalClaimsFiled; }
    public void setTotalClaimsFiled(Integer totalClaimsFiled) { this.totalClaimsFiled = totalClaimsFiled; }
    public Integer getOpenClaimsCount() { return openClaimsCount; }
    public void setOpenClaimsCount(Integer openClaimsCount) { this.openClaimsCount = openClaimsCount; }
    public Integer getClosedClaimsCount() { return closedClaimsCount; }
    public void setClosedClaimsCount(Integer closedClaimsCount) { this.closedClaimsCount = closedClaimsCount; }
    public LocalDateTime getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(LocalDateTime calculatedAt) { this.calculatedAt = calculatedAt; }
    public LocalDateTime getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDateTime periodStart) { this.periodStart = periodStart; }
    public LocalDateTime getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDateTime periodEnd) { this.periodEnd = periodEnd; }
    public void setNew(boolean isNew) { this.isNew = isNew; }
}