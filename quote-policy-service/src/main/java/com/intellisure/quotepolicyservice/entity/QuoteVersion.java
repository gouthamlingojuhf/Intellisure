package com.intellisure.quotepolicyservice.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

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

    public QuoteVersion() {}

    public QuoteVersion(UUID quoteVersionId, UUID quoteId, Long version, BigDecimal totalPremium,
                        String coverageSnapshot, UUID offeredByUserId, LocalDateTime offeredAt, LocalDateTime createdAt) {
        this.quoteVersionId = quoteVersionId;
        this.quoteId = quoteId;
        this.version = version;
        this.totalPremium = totalPremium;
        this.coverageSnapshot = coverageSnapshot;
        this.offeredByUserId = offeredByUserId;
        this.offeredAt = offeredAt;
        this.createdAt = createdAt;
    }

    public static QuoteVersionBuilder builder() {
        return new QuoteVersionBuilder();
    }

    public static class QuoteVersionBuilder {
        private UUID quoteVersionId;
        private UUID quoteId;
        private Long version;
        private BigDecimal totalPremium;
        private String coverageSnapshot;
        private UUID offeredByUserId;
        private LocalDateTime offeredAt;
        private LocalDateTime createdAt;

        public QuoteVersionBuilder quoteVersionId(UUID quoteVersionId) { this.quoteVersionId = quoteVersionId; return this; }
        public QuoteVersionBuilder quoteId(UUID quoteId) { this.quoteId = quoteId; return this; }
        public QuoteVersionBuilder version(Long version) { this.version = version; return this; }
        public QuoteVersionBuilder totalPremium(BigDecimal totalPremium) { this.totalPremium = totalPremium; return this; }
        public QuoteVersionBuilder coverageSnapshot(String coverageSnapshot) { this.coverageSnapshot = coverageSnapshot; return this; }
        public QuoteVersionBuilder offeredByUserId(UUID offeredByUserId) { this.offeredByUserId = offeredByUserId; return this; }
        public QuoteVersionBuilder offeredAt(LocalDateTime offeredAt) { this.offeredAt = offeredAt; return this; }
        public QuoteVersionBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public QuoteVersion build() { return new QuoteVersion(quoteVersionId, quoteId, version, totalPremium, coverageSnapshot, offeredByUserId, offeredAt, createdAt); }
    }

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