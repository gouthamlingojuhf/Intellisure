package com.intellisure.quotepolicyservice.entity;

import com.intellisure.quotepolicyservice.enums.RenewalStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("renewal_transaction")
public class RenewalTransaction {

    @Id
    @Column("renewal_id")
    private UUID renewalId;

    @Column("policy_id")
    private UUID policyId;

    @Column("renewal_number")
    private String renewalNumber;

    @Column("status")
    private RenewalStatus status;

    @Column("proposed_start_date")
    private LocalDate proposedStartDate;

    @Column("proposed_end_date")
    private LocalDate proposedEndDate;

    @Column("proposed_total_premium")
    private BigDecimal proposedTotalPremium;

    @Column("proposed_coverage_snapshot")
    private String proposedCoverageSnapshot;

    @Column("subjectivities")
    private String subjectivities;

    @Column("decided_by_user_id")
    private UUID decidedByUserId;

    @Column("decided_at")
    private LocalDateTime decidedAt;

    @Column("decision_reason")
    private String decisionReason;

    @Column("bound_by_user_id")
    private UUID boundByUserId;

    @Column("bound_at")
    private LocalDateTime boundAt;

    @Column("issued_at")
    private LocalDateTime issuedAt;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    public RenewalTransaction() {}

    public RenewalTransaction(UUID renewalId, UUID policyId, String renewalNumber,
                              RenewalStatus status, LocalDate proposedStartDate, LocalDate proposedEndDate,
                              BigDecimal proposedTotalPremium, String proposedCoverageSnapshot, String subjectivities,
                              UUID decidedByUserId, LocalDateTime decidedAt, String decisionReason,
                              UUID boundByUserId, LocalDateTime boundAt, LocalDateTime issuedAt,
                              LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.renewalId = renewalId;
        this.policyId = policyId;
        this.renewalNumber = renewalNumber;
        this.status = status;
        this.proposedStartDate = proposedStartDate;
        this.proposedEndDate = proposedEndDate;
        this.proposedTotalPremium = proposedTotalPremium;
        this.proposedCoverageSnapshot = proposedCoverageSnapshot;
        this.subjectivities = subjectivities;
        this.decidedByUserId = decidedByUserId;
        this.decidedAt = decidedAt;
        this.decisionReason = decisionReason;
        this.boundByUserId = boundByUserId;
        this.boundAt = boundAt;
        this.issuedAt = issuedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static RenewalTransactionBuilder builder() {
        return new RenewalTransactionBuilder();
    }

    public static class RenewalTransactionBuilder {
        private UUID renewalId;
        private UUID policyId;
        private String renewalNumber;
        private RenewalStatus status;
        private LocalDate proposedStartDate;
        private LocalDate proposedEndDate;
        private BigDecimal proposedTotalPremium;
        private String proposedCoverageSnapshot;
        private String subjectivities;
        private UUID decidedByUserId;
        private LocalDateTime decidedAt;
        private String decisionReason;
        private UUID boundByUserId;
        private LocalDateTime boundAt;
        private LocalDateTime issuedAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public RenewalTransactionBuilder renewalId(UUID renewalId) { this.renewalId = renewalId; return this; }
        public RenewalTransactionBuilder policyId(UUID policyId) { this.policyId = policyId; return this; }
        public RenewalTransactionBuilder renewalNumber(String renewalNumber) { this.renewalNumber = renewalNumber; return this; }
        public RenewalTransactionBuilder status(RenewalStatus status) { this.status = status; return this; }
        public RenewalTransactionBuilder proposedStartDate(LocalDate proposedStartDate) { this.proposedStartDate = proposedStartDate; return this; }
        public RenewalTransactionBuilder proposedEndDate(LocalDate proposedEndDate) { this.proposedEndDate = proposedEndDate; return this; }
        public RenewalTransactionBuilder proposedTotalPremium(BigDecimal proposedTotalPremium) { this.proposedTotalPremium = proposedTotalPremium; return this; }
        public RenewalTransactionBuilder proposedCoverageSnapshot(String proposedCoverageSnapshot) { this.proposedCoverageSnapshot = proposedCoverageSnapshot; return this; }
        public RenewalTransactionBuilder subjectivities(String subjectivities) { this.subjectivities = subjectivities; return this; }
        public RenewalTransactionBuilder decidedByUserId(UUID decidedByUserId) { this.decidedByUserId = decidedByUserId; return this; }
        public RenewalTransactionBuilder decidedAt(LocalDateTime decidedAt) { this.decidedAt = decidedAt; return this; }
        public RenewalTransactionBuilder decisionReason(String decisionReason) { this.decisionReason = decisionReason; return this; }
        public RenewalTransactionBuilder boundByUserId(UUID boundByUserId) { this.boundByUserId = boundByUserId; return this; }
        public RenewalTransactionBuilder boundAt(LocalDateTime boundAt) { this.boundAt = boundAt; return this; }
        public RenewalTransactionBuilder issuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; return this; }
        public RenewalTransactionBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public RenewalTransactionBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public RenewalTransaction build() { return new RenewalTransaction(renewalId, policyId, renewalNumber, status, proposedStartDate, proposedEndDate, proposedTotalPremium, proposedCoverageSnapshot, subjectivities, decidedByUserId, decidedAt, decisionReason, boundByUserId, boundAt, issuedAt, createdAt, updatedAt); }
    }

    public UUID getRenewalId() { return renewalId; }
    public void setRenewalId(UUID renewalId) { this.renewalId = renewalId; }
    public UUID getPolicyId() { return policyId; }
    public void setPolicyId(UUID policyId) { this.policyId = policyId; }
    public String getRenewalNumber() { return renewalNumber; }
    public void setRenewalNumber(String renewalNumber) { this.renewalNumber = renewalNumber; }
    public RenewalStatus getStatus() { return status; }
    public void setStatus(RenewalStatus status) { this.status = status; }
    public LocalDate getProposedStartDate() { return proposedStartDate; }
    public void setProposedStartDate(LocalDate proposedStartDate) { this.proposedStartDate = proposedStartDate; }
    public LocalDate getProposedEndDate() { return proposedEndDate; }
    public void setProposedEndDate(LocalDate proposedEndDate) { this.proposedEndDate = proposedEndDate; }
    public BigDecimal getProposedTotalPremium() { return proposedTotalPremium; }
    public void setProposedTotalPremium(BigDecimal proposedTotalPremium) { this.proposedTotalPremium = proposedTotalPremium; }
    public String getProposedCoverageSnapshot() { return proposedCoverageSnapshot; }
    public void setProposedCoverageSnapshot(String proposedCoverageSnapshot) { this.proposedCoverageSnapshot = proposedCoverageSnapshot; }
    public String getSubjectivities() { return subjectivities; }
    public void setSubjectivities(String subjectivities) { this.subjectivities = subjectivities; }
    public UUID getDecidedByUserId() { return decidedByUserId; }
    public void setDecidedByUserId(UUID decidedByUserId) { this.decidedByUserId = decidedByUserId; }
    public LocalDateTime getDecidedAt() { return decidedAt; }
    public void setDecidedAt(LocalDateTime decidedAt) { this.decidedAt = decidedAt; }
    public String getDecisionReason() { return decisionReason; }
    public void setDecisionReason(String decisionReason) { this.decisionReason = decisionReason; }
    public UUID getBoundByUserId() { return boundByUserId; }
    public void setBoundByUserId(UUID boundByUserId) { this.boundByUserId = boundByUserId; }
    public LocalDateTime getBoundAt() { return boundAt; }
    public void setBoundAt(LocalDateTime boundAt) { this.boundAt = boundAt; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}