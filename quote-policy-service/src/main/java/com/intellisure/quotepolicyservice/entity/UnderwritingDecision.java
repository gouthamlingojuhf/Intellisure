package com.intellisure.quotepolicyservice.entity;

import com.intellisure.quotepolicyservice.enums.UnderwritingDecisionType;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("underwriting_decision")
public class UnderwritingDecision {

    @Id
    @Column("underwriting_decision_id")
    private UUID underwritingDecisionId;

    @Column("source_decision_id")
    private UUID sourceDecisionId;

    @Column("quote_id")
    private UUID quoteId;

    @Column("underwriter_id")
    private UUID underwriterId;

    @Column("decision")
    private UnderwritingDecisionType decision;

    @Column("decision_reason")
    private String decisionReason;

    @Column("authority_level")
    private String authorityLevel;

    @Column("conditions")
    private String conditions;

    @Column("decided_at")
    private LocalDateTime decidedAt;

    @Column("created_at")
    private LocalDateTime createdAt;

    public UnderwritingDecision() {}

    public UnderwritingDecision(UUID underwritingDecisionId, UUID sourceDecisionId, UUID quoteId,
                                UUID underwriterId, UnderwritingDecisionType decision, String decisionReason,
                                String authorityLevel, String conditions, LocalDateTime decidedAt, LocalDateTime createdAt) {
        this.underwritingDecisionId = underwritingDecisionId;
        this.sourceDecisionId = sourceDecisionId;
        this.quoteId = quoteId;
        this.underwriterId = underwriterId;
        this.decision = decision;
        this.decisionReason = decisionReason;
        this.authorityLevel = authorityLevel;
        this.conditions = conditions;
        this.decidedAt = decidedAt;
        this.createdAt = createdAt;
    }

    public static UnderwritingDecisionBuilder builder() {
        return new UnderwritingDecisionBuilder();
    }

    public static class UnderwritingDecisionBuilder {
        private UUID underwritingDecisionId;
        private UUID sourceDecisionId;
        private UUID quoteId;
        private UUID underwriterId;
        private UnderwritingDecisionType decision;
        private String decisionReason;
        private String authorityLevel;
        private String conditions;
        private LocalDateTime decidedAt;
        private LocalDateTime createdAt;

        public UnderwritingDecisionBuilder underwritingDecisionId(UUID underwritingDecisionId) { this.underwritingDecisionId = underwritingDecisionId; return this; }
        public UnderwritingDecisionBuilder sourceDecisionId(UUID sourceDecisionId) { this.sourceDecisionId = sourceDecisionId; return this; }
        public UnderwritingDecisionBuilder quoteId(UUID quoteId) { this.quoteId = quoteId; return this; }
        public UnderwritingDecisionBuilder underwriterId(UUID underwriterId) { this.underwriterId = underwriterId; return this; }
        public UnderwritingDecisionBuilder decision(UnderwritingDecisionType decision) { this.decision = decision; return this; }
        public UnderwritingDecisionBuilder decisionReason(String decisionReason) { this.decisionReason = decisionReason; return this; }
        public UnderwritingDecisionBuilder authorityLevel(String authorityLevel) { this.authorityLevel = authorityLevel; return this; }
        public UnderwritingDecisionBuilder conditions(String conditions) { this.conditions = conditions; return this; }
        public UnderwritingDecisionBuilder decidedAt(LocalDateTime decidedAt) { this.decidedAt = decidedAt; return this; }
        public UnderwritingDecisionBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public UnderwritingDecision build() { return new UnderwritingDecision(underwritingDecisionId, sourceDecisionId, quoteId, underwriterId, decision, decisionReason, authorityLevel, conditions, decidedAt, createdAt); }
    }

    public UUID getUnderwritingDecisionId() { return underwritingDecisionId; }
    public void setUnderwritingDecisionId(UUID underwritingDecisionId) { this.underwritingDecisionId = underwritingDecisionId; }
    public UUID getSourceDecisionId() { return sourceDecisionId; }
    public void setSourceDecisionId(UUID sourceDecisionId) { this.sourceDecisionId = sourceDecisionId; }
    public UUID getQuoteId() { return quoteId; }
    public void setQuoteId(UUID quoteId) { this.quoteId = quoteId; }
    public UUID getUnderwriterId() { return underwriterId; }
    public void setUnderwriterId(UUID underwriterId) { this.underwriterId = underwriterId; }
    public UnderwritingDecisionType getDecision() { return decision; }
    public void setDecision(UnderwritingDecisionType decision) { this.decision = decision; }
    public String getDecisionReason() { return decisionReason; }
    public void setDecisionReason(String decisionReason) { this.decisionReason = decisionReason; }
    public String getAuthorityLevel() { return authorityLevel; }
    public void setAuthorityLevel(String authorityLevel) { this.authorityLevel = authorityLevel; }
    public String getConditions() { return conditions; }
    public void setConditions(String conditions) { this.conditions = conditions; }
    public LocalDateTime getDecidedAt() { return decidedAt; }
    public void setDecidedAt(LocalDateTime decidedAt) { this.decidedAt = decidedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}