package com.intellisure.quotepolicyservice.entity;

import com.intellisure.quotepolicyservice.enums.EndorsementOperation;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Table("endorsement_coverage")
public class EndorsementCoverage {

    @Id
    @Column("endorsement_coverage_id")
    private UUID endorsementCoverageId;

    @Column("endorsement_id")
    private UUID endorsementId;

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

    @Column("operation")
    private EndorsementOperation operation;

    @Column("created_at")
    private LocalDateTime createdAt;

    public UUID getEndorsementCoverageId() { return endorsementCoverageId; }
    public void setEndorsementCoverageId(UUID endorsementCoverageId) { this.endorsementCoverageId = endorsementCoverageId; }
    public UUID getEndorsementId() { return endorsementId; }
    public void setEndorsementId(UUID endorsementId) { this.endorsementId = endorsementId; }
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
    public EndorsementOperation getOperation() { return operation; }
    public void setOperation(EndorsementOperation operation) { this.operation = operation; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}