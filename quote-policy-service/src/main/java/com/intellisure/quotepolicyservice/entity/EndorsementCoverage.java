package com.intellisure.quotepolicyservice.entity;

import com.intellisure.quotepolicyservice.enums.EndorsementOperation;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

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

    public EndorsementCoverage() {}

    public EndorsementCoverage(UUID endorsementCoverageId, UUID endorsementId, String coverageCode, String coverageName,
                               BigDecimal limitAmount, BigDecimal deductibleAmount, BigDecimal coveragePremium,
                               String conditions, String exclusions, Integer waitingPeriodDays,
                               EndorsementOperation operation, LocalDateTime createdAt) {
        this.endorsementCoverageId = endorsementCoverageId;
        this.endorsementId = endorsementId;
        this.coverageCode = coverageCode;
        this.coverageName = coverageName;
        this.limitAmount = limitAmount;
        this.deductibleAmount = deductibleAmount;
        this.coveragePremium = coveragePremium;
        this.conditions = conditions;
        this.exclusions = exclusions;
        this.waitingPeriodDays = waitingPeriodDays;
        this.operation = operation;
        this.createdAt = createdAt;
    }

    public static EndorsementCoverageBuilder builder() {
        return new EndorsementCoverageBuilder();
    }

    public static class EndorsementCoverageBuilder {
        private UUID endorsementCoverageId;
        private UUID endorsementId;
        private String coverageCode;
        private String coverageName;
        private BigDecimal limitAmount;
        private BigDecimal deductibleAmount;
        private BigDecimal coveragePremium;
        private String conditions;
        private String exclusions;
        private Integer waitingPeriodDays;
        private EndorsementOperation operation;
        private LocalDateTime createdAt;

        public EndorsementCoverageBuilder endorsementCoverageId(UUID endorsementCoverageId) { this.endorsementCoverageId = endorsementCoverageId; return this; }
        public EndorsementCoverageBuilder endorsementId(UUID endorsementId) { this.endorsementId = endorsementId; return this; }
        public EndorsementCoverageBuilder coverageCode(String coverageCode) { this.coverageCode = coverageCode; return this; }
        public EndorsementCoverageBuilder coverageName(String coverageName) { this.coverageName = coverageName; return this; }
        public EndorsementCoverageBuilder limitAmount(BigDecimal limitAmount) { this.limitAmount = limitAmount; return this; }
        public EndorsementCoverageBuilder deductibleAmount(BigDecimal deductibleAmount) { this.deductibleAmount = deductibleAmount; return this; }
        public EndorsementCoverageBuilder coveragePremium(BigDecimal coveragePremium) { this.coveragePremium = coveragePremium; return this; }
        public EndorsementCoverageBuilder conditions(String conditions) { this.conditions = conditions; return this; }
        public EndorsementCoverageBuilder exclusions(String exclusions) { this.exclusions = exclusions; return this; }
        public EndorsementCoverageBuilder waitingPeriodDays(Integer waitingPeriodDays) { this.waitingPeriodDays = waitingPeriodDays; return this; }
        public EndorsementCoverageBuilder operation(EndorsementOperation operation) { this.operation = operation; return this; }
        public EndorsementCoverageBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public EndorsementCoverage build() { return new EndorsementCoverage(endorsementCoverageId, endorsementId, coverageCode, coverageName, limitAmount, deductibleAmount, coveragePremium, conditions, exclusions, waitingPeriodDays, operation, createdAt); }
    }

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