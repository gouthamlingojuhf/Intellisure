package com.intellisure.quotepolicyservice.entity;

import com.intellisure.quotepolicyservice.enums.QuoteStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("quote")
public class Quote {

    @Id
    @Column("quote_id")
    private UUID quoteId;

    @Column("quote_number")
    private String quoteNumber;

    @Column("customer_id")
    private UUID customerId;

    @Column("product_code")
    private String productCode;

    @Column("insurance_need")
    private String insuranceNeed;

    @Column("business_operations")
    private String businessOperations;

    @Column("status")
    private QuoteStatus status;

    @Column("requested_effective_date")
    private LocalDate requestedEffectiveDate;

    @Column("quote_expires_at")
    private LocalDateTime quoteExpiresAt;

    @Column("assigned_underwriter_id")
    private UUID assignedUnderwriterId;

    @Column("risk_assessment_id")
    private UUID riskAssessmentId;

    @Column("total_premium")
    private BigDecimal totalPremium;

    @Column("submitted_at")
    private LocalDateTime submittedAt;

    @Column("quoted_at")
    private LocalDateTime quotedAt;

    @Column("accepted_by_user_id")
    private UUID acceptedByUserId;

    @Column("accepted_at")
    private LocalDateTime acceptedAt;

    @Column("bound_by_user_id")
    private UUID boundByUserId;

    @Column("bound_at")
    private LocalDateTime boundAt;

    @Column("decline_reason")
    private String declineReason;

    @Column("withdrawal_reason")
    private String withdrawalReason;

    @Column("version")
    private Long version;

    @Column("subjectivities")
    private String subjectivities;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    public UUID getQuoteId() { return quoteId; }
    public void setQuoteId(UUID quoteId) { this.quoteId = quoteId; }
    public String getQuoteNumber() { return quoteNumber; }
    public void setQuoteNumber(String quoteNumber) { this.quoteNumber = quoteNumber; }
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getInsuranceNeed() { return insuranceNeed; }
    public void setInsuranceNeed(String insuranceNeed) { this.insuranceNeed = insuranceNeed; }
    public String getBusinessOperations() { return businessOperations; }
    public void setBusinessOperations(String businessOperations) { this.businessOperations = businessOperations; }
    public QuoteStatus getStatus() { return status; }
    public void setStatus(QuoteStatus status) { this.status = status; }
    public LocalDate getRequestedEffectiveDate() { return requestedEffectiveDate; }
    public void setRequestedEffectiveDate(LocalDate requestedEffectiveDate) { this.requestedEffectiveDate = requestedEffectiveDate; }
    public LocalDateTime getQuoteExpiresAt() { return quoteExpiresAt; }
    public void setQuoteExpiresAt(LocalDateTime quoteExpiresAt) { this.quoteExpiresAt = quoteExpiresAt; }
    public UUID getAssignedUnderwriterId() { return assignedUnderwriterId; }
    public void setAssignedUnderwriterId(UUID assignedUnderwriterId) { this.assignedUnderwriterId = assignedUnderwriterId; }
    public UUID getRiskAssessmentId() { return riskAssessmentId; }
    public void setRiskAssessmentId(UUID riskAssessmentId) { this.riskAssessmentId = riskAssessmentId; }
    public BigDecimal getTotalPremium() { return totalPremium; }
    public void setTotalPremium(BigDecimal totalPremium) { this.totalPremium = totalPremium; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public LocalDateTime getQuotedAt() { return quotedAt; }
    public void setQuotedAt(LocalDateTime quotedAt) { this.quotedAt = quotedAt; }
    public UUID getAcceptedByUserId() { return acceptedByUserId; }
    public void setAcceptedByUserId(UUID acceptedByUserId) { this.acceptedByUserId = acceptedByUserId; }
    public LocalDateTime getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(LocalDateTime acceptedAt) { this.acceptedAt = acceptedAt; }
    public UUID getBoundByUserId() { return boundByUserId; }
    public void setBoundByUserId(UUID boundByUserId) { this.boundByUserId = boundByUserId; }
    public LocalDateTime getBoundAt() { return boundAt; }
    public void setBoundAt(LocalDateTime boundAt) { this.boundAt = boundAt; }
    public String getDeclineReason() { return declineReason; }
    public void setDeclineReason(String declineReason) { this.declineReason = declineReason; }
    public String getWithdrawalReason() { return withdrawalReason; }
    public void setWithdrawalReason(String withdrawalReason) { this.withdrawalReason = withdrawalReason; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public String getSubjectivities() { return subjectivities; }
    public void setSubjectivities(String subjectivities) { this.subjectivities = subjectivities; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}