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
@Table("quote_version")
public class QuoteVersion {

    @Id
    @Column("quote_version_id")
    private UUID quoteVersionId;

    @Column("quote_id")
    private UUID quoteId;

    @Column("version")
    private Long version;

    @Column("total_premium")
    private BigDecimal totalPremium;

    @Column("coverage_snapshot")
    private String coverageSnapshot;

    @Column("offered_by_user_id")
    private UUID offeredByUserId;

    @Column("offered_at")
    private LocalDateTime offeredAt;

    @Column("created_at")
    private LocalDateTime createdAt;

    public UUID getQuoteVersionId() { return quoteVersionId; }
    public void setQuoteVersionId(UUID quoteVersionId) { this.quoteVersionId = quoteVersionId; }
    public UUID getQuoteId() { return quoteId; }
    public void setQuoteId(UUID quoteId) { this.quoteId = quoteId; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public BigDecimal getTotalPremium() { return totalPremium; }
    public void setTotalPremium(BigDecimal totalPremium) { this.totalPremium = totalPremium; }
    public String getCoverageSnapshot() { return coverageSnapshot; }
    public void setCoverageSnapshot(String coverageSnapshot) { this.coverageSnapshot = coverageSnapshot; }
    public UUID getOfferedByUserId() { return offeredByUserId; }
    public void setOfferedByUserId(UUID offeredByUserId) { this.offeredByUserId = offeredByUserId; }
    public LocalDateTime getOfferedAt() { return offeredAt; }
    public void setOfferedAt(LocalDateTime offeredAt) { this.offeredAt = offeredAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}