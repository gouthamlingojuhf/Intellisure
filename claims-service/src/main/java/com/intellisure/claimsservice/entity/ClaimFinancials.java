package com.intellisure.claimsservice.entity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@lombok.Getter
@lombok.Setter
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@lombok.Builder
@Table("claim_financials")
public class ClaimFinancials {

    @org.springframework.data.annotation.Id
    @Column("financial_id")
    private UUID financialId;

    @Column("claim_id")
    private UUID claimId;

    @Column("reserve_amount")
    private BigDecimal reserveAmount;

    @Column("total_incurred")
    private BigDecimal totalIncurred;

    @Column("paid_amount")
    private BigDecimal paidAmount;

    @Column("outstanding_reserve")
    private BigDecimal outstandingReserve;

    @Column("last_updated_by")
    private UUID lastUpdatedBy;

    @Column("last_updated_at")
    private LocalDateTime lastUpdatedAt;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    public UUID getFinancialId() { return financialId; }
    public void setFinancialId(UUID financialId) { this.financialId = financialId; }
    public UUID getClaimId() { return claimId; }
    public void setClaimId(UUID claimId) { this.claimId = claimId; }
    public BigDecimal getReserveAmount() { return reserveAmount; }
    public void setReserveAmount(BigDecimal reserveAmount) { this.reserveAmount = reserveAmount; }
    public BigDecimal getTotalIncurred() { return totalIncurred; }
    public void setTotalIncurred(BigDecimal totalIncurred) { this.totalIncurred = totalIncurred; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
    public BigDecimal getOutstandingReserve() { return outstandingReserve; }
    public void setOutstandingReserve(BigDecimal outstandingReserve) { this.outstandingReserve = outstandingReserve; }
    public UUID getLastUpdatedBy() { return lastUpdatedBy; }
    public void setLastUpdatedBy(UUID lastUpdatedBy) { this.lastUpdatedBy = lastUpdatedBy; }
    public LocalDateTime getLastUpdatedAt() { return lastUpdatedAt; }
    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) { this.lastUpdatedAt = lastUpdatedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}