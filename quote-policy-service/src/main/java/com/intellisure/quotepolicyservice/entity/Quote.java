package com.intellisure.quotepolicyservice.entity;

import com.intellisure.quotepolicyservice.enums.QuoteStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

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

    public Quote() {}

    public Quote(UUID quoteId, String quoteNumber, UUID customerId, String productCode,
                 String insuranceNeed, String businessOperations, QuoteStatus status,
                 LocalDate requestedEffectiveDate, LocalDateTime quoteExpiresAt, UUID assignedUnderwriterId,
                 UUID riskAssessmentId, BigDecimal totalPremium, LocalDateTime submittedAt,
                 LocalDateTime quotedAt, UUID acceptedByUserId, LocalDateTime acceptedAt,
                 UUID boundByUserId, LocalDateTime boundAt, String declineReason, String withdrawalReason,
                 Long version, String subjectivities, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.quoteId = quoteId;
        this.quoteNumber = quoteNumber;
        this.customerId = customerId;
        this.productCode = productCode;
        this.insuranceNeed = insuranceNeed;
        this.businessOperations = businessOperations;
        this.status = status;
        this.requestedEffectiveDate = requestedEffectiveDate;
        this.quoteExpiresAt = quoteExpiresAt;
        this.assignedUnderwriterId = assignedUnderwriterId;
        this.riskAssessmentId = riskAssessmentId;
        this.totalPremium = totalPremium;
        this.submittedAt = submittedAt;
        this.quotedAt = quotedAt;
        this.acceptedByUserId = acceptedByUserId;
        this.acceptedAt = acceptedAt;
        this.boundByUserId = boundByUserId;
        this.boundAt = boundAt;
        this.declineReason = declineReason;
        this.withdrawalReason = withdrawalReason;
        this.version = version;
        this.subjectivities = subjectivities;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static QuoteBuilder builder() {
        return new QuoteBuilder();
    }

    public static class QuoteBuilder {
        private UUID quoteId;
        private String quoteNumber;
        private UUID customerId;
        private String productCode;
        private String insuranceNeed;
        private String businessOperations;
        private QuoteStatus status;
        private LocalDate requestedEffectiveDate;
        private LocalDateTime quoteExpiresAt;
        private UUID assignedUnderwriterId;
        private UUID riskAssessmentId;
        private BigDecimal totalPremium;
        private LocalDateTime submittedAt;
        private LocalDateTime quotedAt;
        private UUID acceptedByUserId;
        private LocalDateTime acceptedAt;
        private UUID boundByUserId;
        private LocalDateTime boundAt;
        private String declineReason;
        private String withdrawalReason;
        private Long version;
        private String subjectivities;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public QuoteBuilder quoteId(UUID quoteId) { this.quoteId = quoteId; return this; }
        public QuoteBuilder quoteNumber(String quoteNumber) { this.quoteNumber = quoteNumber; return this; }
        public QuoteBuilder customerId(UUID customerId) { this.customerId = customerId; return this; }
        public QuoteBuilder productCode(String productCode) { this.productCode = productCode; return this; }
        public QuoteBuilder insuranceNeed(String insuranceNeed) { this.insuranceNeed = insuranceNeed; return this; }
        public QuoteBuilder businessOperations(String businessOperations) { this.businessOperations = businessOperations; return this; }
        public QuoteBuilder status(QuoteStatus status) { this.status = status; return this; }
        public QuoteBuilder requestedEffectiveDate(LocalDate requestedEffectiveDate) { this.requestedEffectiveDate = requestedEffectiveDate; return this; }
        public QuoteBuilder quoteExpiresAt(LocalDateTime quoteExpiresAt) { this.quoteExpiresAt = quoteExpiresAt; return this; }
        public QuoteBuilder assignedUnderwriterId(UUID assignedUnderwriterId) { this.assignedUnderwriterId = assignedUnderwriterId; return this; }
        public QuoteBuilder riskAssessmentId(UUID riskAssessmentId) { this.riskAssessmentId = riskAssessmentId; return this; }
        public QuoteBuilder totalPremium(BigDecimal totalPremium) { this.totalPremium = totalPremium; return this; }
        public QuoteBuilder submittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; return this; }
        public QuoteBuilder quotedAt(LocalDateTime quotedAt) { this.quotedAt = quotedAt; return this; }
        public QuoteBuilder acceptedByUserId(UUID acceptedByUserId) { this.acceptedByUserId = acceptedByUserId; return this; }
        public QuoteBuilder acceptedAt(LocalDateTime acceptedAt) { this.acceptedAt = acceptedAt; return this; }
        public QuoteBuilder boundByUserId(UUID boundByUserId) { this.boundByUserId = boundByUserId; return this; }
        public QuoteBuilder boundAt(LocalDateTime boundAt) { this.boundAt = boundAt; return this; }
        public QuoteBuilder declineReason(String declineReason) { this.declineReason = declineReason; return this; }
        public QuoteBuilder withdrawalReason(String withdrawalReason) { this.withdrawalReason = withdrawalReason; return this; }
        public QuoteBuilder version(Long version) { this.version = version; return this; }
        public QuoteBuilder subjectivities(String subjectivities) { this.subjectivities = subjectivities; return this; }
        public QuoteBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public QuoteBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public Quote build() { return new Quote(quoteId, quoteNumber, customerId, productCode, insuranceNeed, businessOperations, status, requestedEffectiveDate, quoteExpiresAt, assignedUnderwriterId, riskAssessmentId, totalPremium, submittedAt, quotedAt, acceptedByUserId, acceptedAt, boundByUserId, boundAt, declineReason, withdrawalReason, version, subjectivities, createdAt, updatedAt); }
    }

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