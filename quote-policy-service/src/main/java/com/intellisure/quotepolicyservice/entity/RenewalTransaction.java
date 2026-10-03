package com.intellisure.quotepolicyservice.entity;

import com.intellisure.quotepolicyservice.enums.RenewalStatus;
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