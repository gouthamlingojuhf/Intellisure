package com.intellisure.quotepolicyservice.entity;

import com.intellisure.quotepolicyservice.enums.AuditStatus;
import com.intellisure.quotepolicyservice.enums.AuditType;
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
@Table("premium_audit")
public class PremiumAudit {

    @Id
    @Column("audit_id")
    private UUID auditId;

    @Column("policy_id")
    private UUID policyId;

    @Column("audit_number")
    private String auditNumber;

    @Column("audit_type")
    private AuditType auditType;

    @Column("status")
    private AuditStatus status;

    @Column("estimated_exposure")
    private BigDecimal estimatedExposure;

    @Column("actual_exposure")
    private BigDecimal actualExposure;

    @Column("exposure_basis")
    private String exposureBasis;

    @Column("premium_delta")
    private BigDecimal premiumDelta;

    @Column("additional_premium")
    private BigDecimal additionalPremium;

    @Column("return_premium")
    private BigDecimal returnPremium;

    @Column("audited_by_user_id")
    private UUID auditedByUserId;

    @Column("audited_at")
    private LocalDateTime auditedAt;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    public UUID getAuditId() { return auditId; }
    public void setAuditId(UUID auditId) { this.auditId = auditId; }
    public UUID getPolicyId() { return policyId; }
    public void setPolicyId(UUID policyId) { this.policyId = policyId; }
    public String getAuditNumber() { return auditNumber; }
    public void setAuditNumber(String auditNumber) { this.auditNumber = auditNumber; }
    public AuditType getAuditType() { return auditType; }
    public void setAuditType(AuditType auditType) { this.auditType = auditType; }
    public AuditStatus getStatus() { return status; }
    public void setStatus(AuditStatus status) { this.status = status; }
    public BigDecimal getEstimatedExposure() { return estimatedExposure; }
    public void setEstimatedExposure(BigDecimal estimatedExposure) { this.estimatedExposure = estimatedExposure; }
    public BigDecimal getActualExposure() { return actualExposure; }
    public void setActualExposure(BigDecimal actualExposure) { this.actualExposure = actualExposure; }
    public String getExposureBasis() { return exposureBasis; }
    public void setExposureBasis(String exposureBasis) { this.exposureBasis = exposureBasis; }
    public BigDecimal getPremiumDelta() { return premiumDelta; }
    public void setPremiumDelta(BigDecimal premiumDelta) { this.premiumDelta = premiumDelta; }
    public BigDecimal getAdditionalPremium() { return additionalPremium; }
    public void setAdditionalPremium(BigDecimal additionalPremium) { this.additionalPremium = additionalPremium; }
    public BigDecimal getReturnPremium() { return returnPremium; }
    public void setReturnPremium(BigDecimal returnPremium) { this.returnPremium = returnPremium; }
    public UUID getAuditedByUserId() { return auditedByUserId; }
    public void setAuditedByUserId(UUID auditedByUserId) { this.auditedByUserId = auditedByUserId; }
    public LocalDateTime getAuditedAt() { return auditedAt; }
    public void setAuditedAt(LocalDateTime auditedAt) { this.auditedAt = auditedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}