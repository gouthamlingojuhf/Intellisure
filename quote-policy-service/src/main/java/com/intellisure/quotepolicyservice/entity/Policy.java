package com.intellisure.quotepolicyservice.entity;

import com.intellisure.quotepolicyservice.enums.PolicyStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("policy")
public class Policy {

    @Id
    @Column("policy_id")
    private UUID policyId;

    @Column("policy_number")
    private String policyNumber;

    @Column("quote_id")
    private UUID quoteId;

    @Column("customer_id")
    private UUID customerId;

    @Column("product_code")
    private String productCode;

    @Column("status")
    private PolicyStatus status;

    @Column("start_date")
    private LocalDate startDate;

    @Column("end_date")
    private LocalDate endDate;

    @Column("total_premium")
    private BigDecimal totalPremium;

    @Column("issued_by_user_id")
    private UUID issuedByUserId;

    @Column("bound_at")
    private LocalDateTime boundAt;

    @Column("issued_at")
    private LocalDateTime issuedAt;

    @Column("expired_at")
    private LocalDateTime expiredAt;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    public Policy() {}

    public Policy(UUID policyId, String policyNumber, UUID quoteId, UUID customerId,
                  String productCode, PolicyStatus status, LocalDate startDate, LocalDate endDate,
                  BigDecimal totalPremium, UUID issuedByUserId, LocalDateTime boundAt,
                  LocalDateTime issuedAt, LocalDateTime expiredAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.policyId = policyId;
        this.policyNumber = policyNumber;
        this.quoteId = quoteId;
        this.customerId = customerId;
        this.productCode = productCode;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalPremium = totalPremium;
        this.issuedByUserId = issuedByUserId;
        this.boundAt = boundAt;
        this.issuedAt = issuedAt;
        this.expiredAt = expiredAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PolicyBuilder builder() {
        return new PolicyBuilder();
    }

    public static class PolicyBuilder {
        private UUID policyId;
        private String policyNumber;
        private UUID quoteId;
        private UUID customerId;
        private String productCode;
        private PolicyStatus status;
        private LocalDate startDate;
        private LocalDate endDate;
        private BigDecimal totalPremium;
        private UUID issuedByUserId;
        private LocalDateTime boundAt;
        private LocalDateTime issuedAt;
        private LocalDateTime expiredAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public PolicyBuilder policyId(UUID policyId) { this.policyId = policyId; return this; }
        public PolicyBuilder policyNumber(String policyNumber) { this.policyNumber = policyNumber; return this; }
        public PolicyBuilder quoteId(UUID quoteId) { this.quoteId = quoteId; return this; }
        public PolicyBuilder customerId(UUID customerId) { this.customerId = customerId; return this; }
        public PolicyBuilder productCode(String productCode) { this.productCode = productCode; return this; }
        public PolicyBuilder status(PolicyStatus status) { this.status = status; return this; }
        public PolicyBuilder startDate(LocalDate startDate) { this.startDate = startDate; return this; }
        public PolicyBuilder endDate(LocalDate endDate) { this.endDate = endDate; return this; }
        public PolicyBuilder totalPremium(BigDecimal totalPremium) { this.totalPremium = totalPremium; return this; }
        public PolicyBuilder issuedByUserId(UUID issuedByUserId) { this.issuedByUserId = issuedByUserId; return this; }
        public PolicyBuilder boundAt(LocalDateTime boundAt) { this.boundAt = boundAt; return this; }
        public PolicyBuilder issuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; return this; }
        public PolicyBuilder expiredAt(LocalDateTime expiredAt) { this.expiredAt = expiredAt; return this; }
        public PolicyBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public PolicyBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public Policy build() { return new Policy(policyId, policyNumber, quoteId, customerId, productCode, status, startDate, endDate, totalPremium, issuedByUserId, boundAt, issuedAt, expiredAt, createdAt, updatedAt); }
    }

    public UUID getPolicyId() { return policyId; }
    public void setPolicyId(UUID policyId) { this.policyId = policyId; }
    public String getPolicyNumber() { return policyNumber; }
    public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }
    public UUID getQuoteId() { return quoteId; }
    public void setQuoteId(UUID quoteId) { this.quoteId = quoteId; }
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public PolicyStatus getStatus() { return status; }
    public void setStatus(PolicyStatus status) { this.status = status; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public BigDecimal getTotalPremium() { return totalPremium; }
    public void setTotalPremium(BigDecimal totalPremium) { this.totalPremium = totalPremium; }
    public UUID getIssuedByUserId() { return issuedByUserId; }
    public void setIssuedByUserId(UUID issuedByUserId) { this.issuedByUserId = issuedByUserId; }
    public LocalDateTime getBoundAt() { return boundAt; }
    public void setBoundAt(LocalDateTime boundAt) { this.boundAt = boundAt; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }
    public LocalDateTime getExpiredAt() { return expiredAt; }
    public void setExpiredAt(LocalDateTime expiredAt) { this.expiredAt = expiredAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}