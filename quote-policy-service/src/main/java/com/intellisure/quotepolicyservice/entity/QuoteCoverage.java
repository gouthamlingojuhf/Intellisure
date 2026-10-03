package com.intellisure.quotepolicyservice.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("quote_coverage")
public class QuoteCoverage {

    @Id
    @Column("quote_coverage_id")
    private UUID quoteCoverageId;

    @Column("quote_id")
    private UUID quoteId;

    @Column("coverage_code")
    private String coverageCode;

    @Column("coverage_name")
    private String coverageName;

    @Column("requested_limit")
    private BigDecimal requestedLimit;

    @Column("offered_limit")
    private BigDecimal offeredLimit;

    @Column("requested_deductible")
    private BigDecimal requestedDeductible;

    @Column("offered_deductible")
    private BigDecimal offeredDeductible;

    @Column("coverage_premium")
    private BigDecimal coveragePremium;

    @Column("conditions")
    private String conditions;

    @Column("exclusions")
    private String exclusions;

    @Column("waiting_period_days")
    private Integer waitingPeriodDays;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    public QuoteCoverage() {}

    public QuoteCoverage(UUID quoteCoverageId, UUID quoteId, String coverageCode, String coverageName,
                         BigDecimal requestedLimit, BigDecimal offeredLimit, BigDecimal requestedDeductible,
                         BigDecimal offeredDeductible, BigDecimal coveragePremium, String conditions,
                         String exclusions, Integer waitingPeriodDays, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.quoteCoverageId = quoteCoverageId;
        this.quoteId = quoteId;
        this.coverageCode = coverageCode;
        this.coverageName = coverageName;
        this.requestedLimit = requestedLimit;
        this.offeredLimit = offeredLimit;
        this.requestedDeductible = requestedDeductible;
        this.offeredDeductible = offeredDeductible;
        this.coveragePremium = coveragePremium;
        this.conditions = conditions;
        this.exclusions = exclusions;
        this.waitingPeriodDays = waitingPeriodDays;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static QuoteCoverageBuilder builder() {
        return new QuoteCoverageBuilder();
    }

    public static class QuoteCoverageBuilder {
        private UUID quoteCoverageId;
        private UUID quoteId;
        private String coverageCode;
        private String coverageName;
        private BigDecimal requestedLimit;
        private BigDecimal offeredLimit;
        private BigDecimal requestedDeductible;
        private BigDecimal offeredDeductible;
        private BigDecimal coveragePremium;
        private String conditions;
        private String exclusions;
        private Integer waitingPeriodDays;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public QuoteCoverageBuilder quoteCoverageId(UUID quoteCoverageId) { this.quoteCoverageId = quoteCoverageId; return this; }
        public QuoteCoverageBuilder quoteId(UUID quoteId) { this.quoteId = quoteId; return this; }
        public QuoteCoverageBuilder coverageCode(String coverageCode) { this.coverageCode = coverageCode; return this; }
        public QuoteCoverageBuilder coverageName(String coverageName) { this.coverageName = coverageName; return this; }
        public QuoteCoverageBuilder requestedLimit(BigDecimal requestedLimit) { this.requestedLimit = requestedLimit; return this; }
        public QuoteCoverageBuilder offeredLimit(BigDecimal offeredLimit) { this.offeredLimit = offeredLimit; return this; }
        public QuoteCoverageBuilder requestedDeductible(BigDecimal requestedDeductible) { this.requestedDeductible = requestedDeductible; return this; }
        public QuoteCoverageBuilder offeredDeductible(BigDecimal offeredDeductible) { this.offeredDeductible = offeredDeductible; return this; }
        public QuoteCoverageBuilder coveragePremium(BigDecimal coveragePremium) { this.coveragePremium = coveragePremium; return this; }
        public QuoteCoverageBuilder conditions(String conditions) { this.conditions = conditions; return this; }
        public QuoteCoverageBuilder exclusions(String exclusions) { this.exclusions = exclusions; return this; }
        public QuoteCoverageBuilder waitingPeriodDays(Integer waitingPeriodDays) { this.waitingPeriodDays = waitingPeriodDays; return this; }
        public QuoteCoverageBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public QuoteCoverageBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public QuoteCoverage build() { return new QuoteCoverage(quoteCoverageId, quoteId, coverageCode, coverageName, requestedLimit, offeredLimit, requestedDeductible, offeredDeductible, coveragePremium, conditions, exclusions, waitingPeriodDays, createdAt, updatedAt); }
    }

    public UUID getQuoteCoverageId() { return quoteCoverageId; }
    public void setQuoteCoverageId(UUID quoteCoverageId) { this.quoteCoverageId = quoteCoverageId; }
    public UUID getQuoteId() { return quoteId; }
    public void setQuoteId(UUID quoteId) { this.quoteId = quoteId; }
    public String getCoverageCode() { return coverageCode; }
    public void setCoverageCode(String coverageCode) { this.coverageCode = coverageCode; }
    public String getCoverageName() { return coverageName; }
    public void setCoverageName(String coverageName) { this.coverageName = coverageName; }
    public BigDecimal getRequestedLimit() { return requestedLimit; }
    public void setRequestedLimit(BigDecimal requestedLimit) { this.requestedLimit = requestedLimit; }
    public BigDecimal getOfferedLimit() { return offeredLimit; }
    public void setOfferedLimit(BigDecimal offeredLimit) { this.offeredLimit = offeredLimit; }
    public BigDecimal getRequestedDeductible() { return requestedDeductible; }
    public void setRequestedDeductible(BigDecimal requestedDeductible) { this.requestedDeductible = requestedDeductible; }
    public BigDecimal getOfferedDeductible() { return offeredDeductible; }
    public void setOfferedDeductible(BigDecimal offeredDeductible) { this.offeredDeductible = offeredDeductible; }
    public BigDecimal getCoveragePremium() { return coveragePremium; }
    public void setCoveragePremium(BigDecimal coveragePremium) { this.coveragePremium = coveragePremium; }
    public String getConditions() { return conditions; }
    public void setConditions(String conditions) { this.conditions = conditions; }
    public String getExclusions() { return exclusions; }
    public void setExclusions(String exclusions) { this.exclusions = exclusions; }
    public Integer getWaitingPeriodDays() { return waitingPeriodDays; }
    public void setWaitingPeriodDays(Integer waitingPeriodDays) { this.waitingPeriodDays = waitingPeriodDays; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}