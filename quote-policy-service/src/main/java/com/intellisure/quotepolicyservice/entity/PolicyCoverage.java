package com.intellisure.quotepolicyservice.entity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Table("policy_coverage")
public class PolicyCoverage {

    @Id
    @Column("policy_coverage_id")
    private UUID policyCoverageId;

    @Column("policy_id")
    private UUID policyId;

    @Column("coverage_code")
    private String coverageCode;

    @Column("coverage_name")
    private String coverageName;

    @Column("limit_amount")
    private BigDecimal limitAmount;

    @Column("deductible_amount")
    private BigDecimal deductibleAmount;

    @Column("coverage_premium")
    private BigDecimal coveragePremium;

    @Column("conditions")
    private String conditions;

    @Column("exclusions")
    private String exclusions;

    @Column("waiting_period_days")
    private Integer waitingPeriodDays;

    @Column("effective_from")
    private LocalDate effectiveFrom;

    @Column("effective_to")
    private LocalDate effectiveTo;

    @Column("created_at")
    private LocalDateTime createdAt;

    public UUID getPolicyCoverageId() { return policyCoverageId; }
    public void setPolicyCoverageId(UUID policyCoverageId) { this.policyCoverageId = policyCoverageId; }
    public UUID getPolicyId() { return policyId; }
    public void setPolicyId(UUID policyId) { this.policyId = policyId; }
    public String getCoverageCode() { return coverageCode; }
    public void setCoverageCode(String coverageCode) { this.coverageCode = coverageCode; }
    public String getCoverageName() { return coverageName; }
    public void setCoverageName(String coverageName) { this.coverageName = coverageName; }
    public BigDecimal getLimitAmount() { return limitAmount; }
    public void setLimitAmount(BigDecimal limitAmount) { this.limitAmount = limitAmount; }
    public BigDecimal getDeductibleAmount() { return deductibleAmount; }
    public void setDeductibleAmount(BigDecimal deductibleAmount) { this.deductibleAmount = deductibleAmount; }
    public BigDecimal getCoveragePremium() { return coveragePremium; }
    public void setCoveragePremium(BigDecimal coveragePremium) { this.coveragePremium = coveragePremium; }
    public String getConditions() { return conditions; }
    public void setConditions(String conditions) { this.conditions = conditions; }
    public String getExclusions() { return exclusions; }
    public void setExclusions(String exclusions) { this.exclusions = exclusions; }
    public Integer getWaitingPeriodDays() { return waitingPeriodDays; }
    public void setWaitingPeriodDays(Integer waitingPeriodDays) { this.waitingPeriodDays = waitingPeriodDays; }
    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(LocalDate effectiveFrom) { this.effectiveFrom = effectiveFrom; }
    public LocalDate getEffectiveTo() { return effectiveTo; }
    public void setEffectiveTo(LocalDate effectiveTo) { this.effectiveTo = effectiveTo; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}