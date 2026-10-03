package com.intellisure.quotepolicyservice.entity;

import com.intellisure.quotepolicyservice.enums.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("endorsement")
public class Endorsement {

    @Id
    @Column("endorsement_id")
    private UUID endorsementId;

    @Column("policy_id")
    private UUID policyId;

    @Column("endorsement_number")
    private String endorsementNumber;

    @Column("endorsement_type")
    private EndorsementType endorsementType;

    @Column("description")
    private String description;

    @Column("premium_delta")
    private BigDecimal premiumDelta;

    @Column("status")
    private EndorsementStatus status;

    @Column("requested_by_user_id")
    private UUID requestedByUserId;

    @Column("approved_by_user_id")
    private UUID approvedByUserId;

    @Column("effective_from")
    private LocalDate effectiveFrom;

    @Column("effective_to")
    private LocalDate effectiveTo;

    @Column("requested_at")
    private LocalDateTime requestedAt;

    @Column("approved_at")
    private LocalDateTime approvedAt;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    public Endorsement() {}

    public Endorsement(UUID endorsementId, UUID policyId, String endorsementNumber,
                       EndorsementType endorsementType, String description, BigDecimal premiumDelta,
                       EndorsementStatus status, UUID requestedByUserId, UUID approvedByUserId,
                       LocalDate effectiveFrom, LocalDate effectiveTo, LocalDateTime requestedAt,
                       LocalDateTime approvedAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.endorsementId = endorsementId;
        this.policyId = policyId;
        this.endorsementNumber = endorsementNumber;
        this.endorsementType = endorsementType;
        this.description = description;
        this.premiumDelta = premiumDelta;
        this.status = status;
        this.requestedByUserId = requestedByUserId;
        this.approvedByUserId = approvedByUserId;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.requestedAt = requestedAt;
        this.approvedAt = approvedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static EndorsementBuilder builder() {
        return new EndorsementBuilder();
    }

    public static class EndorsementBuilder {
        private UUID endorsementId;
        private UUID policyId;
        private String endorsementNumber;
        private EndorsementType endorsementType;
        private String description;
        private BigDecimal premiumDelta;
        private EndorsementStatus status;
        private UUID requestedByUserId;
        private UUID approvedByUserId;
        private LocalDate effectiveFrom;
        private LocalDate effectiveTo;
        private LocalDateTime requestedAt;
        private LocalDateTime approvedAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public EndorsementBuilder endorsementId(UUID endorsementId) { this.endorsementId = endorsementId; return this; }
        public EndorsementBuilder policyId(UUID policyId) { this.policyId = policyId; return this; }
        public EndorsementBuilder endorsementNumber(String endorsementNumber) { this.endorsementNumber = endorsementNumber; return this; }
        public EndorsementBuilder endorsementType(EndorsementType endorsementType) { this.endorsementType = endorsementType; return this; }
        public EndorsementBuilder description(String description) { this.description = description; return this; }
        public EndorsementBuilder premiumDelta(BigDecimal premiumDelta) { this.premiumDelta = premiumDelta; return this; }
        public EndorsementBuilder status(EndorsementStatus status) { this.status = status; return this; }
        public EndorsementBuilder requestedByUserId(UUID requestedByUserId) { this.requestedByUserId = requestedByUserId; return this; }
        public EndorsementBuilder approvedByUserId(UUID approvedByUserId) { this.approvedByUserId = approvedByUserId; return this; }
        public EndorsementBuilder effectiveFrom(LocalDate effectiveFrom) { this.effectiveFrom = effectiveFrom; return this; }
        public EndorsementBuilder effectiveTo(LocalDate effectiveTo) { this.effectiveTo = effectiveTo; return this; }
        public EndorsementBuilder requestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; return this; }
        public EndorsementBuilder approvedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; return this; }
        public EndorsementBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public EndorsementBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public Endorsement build() { return new Endorsement(endorsementId, policyId, endorsementNumber, endorsementType, description, premiumDelta, status, requestedByUserId, approvedByUserId, effectiveFrom, effectiveTo, requestedAt, approvedAt, createdAt, updatedAt); }
    }

    public UUID getEndorsementId() { return endorsementId; }
    public void setEndorsementId(UUID endorsementId) { this.endorsementId = endorsementId; }
    public UUID getPolicyId() { return policyId; }
    public void setPolicyId(UUID policyId) { this.policyId = policyId; }
    public String getEndorsementNumber() { return endorsementNumber; }
    public void setEndorsementNumber(String endorsementNumber) { this.endorsementNumber = endorsementNumber; }
    public EndorsementType getEndorsementType() { return endorsementType; }
    public void setEndorsementType(EndorsementType endorsementType) { this.endorsementType = endorsementType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPremiumDelta() { return premiumDelta; }
    public void setPremiumDelta(BigDecimal premiumDelta) { this.premiumDelta = premiumDelta; }
    public EndorsementStatus getStatus() { return status; }
    public void setStatus(EndorsementStatus status) { this.status = status; }
    public UUID getRequestedByUserId() { return requestedByUserId; }
    public void setRequestedByUserId(UUID requestedByUserId) { this.requestedByUserId = requestedByUserId; }
    public UUID getApprovedByUserId() { return approvedByUserId; }
    public void setApprovedByUserId(UUID approvedByUserId) { this.approvedByUserId = approvedByUserId; }
    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(LocalDate effectiveFrom) { this.effectiveFrom = effectiveFrom; }
    public LocalDate getEffectiveTo() { return effectiveTo; }
    public void setEffectiveTo(LocalDate effectiveTo) { this.effectiveTo = effectiveTo; }
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}