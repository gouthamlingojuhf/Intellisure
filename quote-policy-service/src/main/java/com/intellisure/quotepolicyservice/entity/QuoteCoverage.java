package com.intellisure.quotepolicyservice.entity;

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