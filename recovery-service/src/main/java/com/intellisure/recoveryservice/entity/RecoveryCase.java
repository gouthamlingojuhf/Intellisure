package com.intellisure.recoveryservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("recovery_case")
public class RecoveryCase implements Persistable<UUID> {

    @Id
    private UUID recoveryCaseId;

    private UUID claimId;
    private UUID customerId;
    private RecoverySeverity severity;
    private RecoveryCaseStatus status;
    private RecoveryPath recoveryPath;
    private String recoveryObjective;
    private String recoveryNotes;
    private LocalDate targetRestoreDate;
    private LocalDate actualRestorationDate;
    private BigDecimal currentRestorePercent;
    private UUID ownerId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Transient
    private boolean isNew = true;

    public static RecoveryCase builder() {
        return new RecoveryCase();
    }

    public RecoveryCase recoveryCaseId(UUID recoveryCaseId) { this.recoveryCaseId = recoveryCaseId; return this; }
    public RecoveryCase claimId(UUID claimId) { this.claimId = claimId; return this; }
    public RecoveryCase customerId(UUID customerId) { this.customerId = customerId; return this; }
    public RecoveryCase severity(RecoverySeverity severity) { this.severity = severity; return this; }
    public RecoveryCase status(RecoveryCaseStatus status) { this.status = status; return this; }
    public RecoveryCase recoveryObjective(String recoveryObjective) { this.recoveryObjective = recoveryObjective; return this; }
    public RecoveryCase recoveryPath(RecoveryPath recoveryPath) { this.recoveryPath = recoveryPath; return this; }
    public RecoveryCase recoveryNotes(String recoveryNotes) { this.recoveryNotes = recoveryNotes; return this; }
    public RecoveryCase targetRestoreDate(LocalDate targetRestoreDate) { this.targetRestoreDate = targetRestoreDate; return this; }
    public RecoveryCase actualRestorationDate(LocalDate actualRestorationDate) { this.actualRestorationDate = actualRestorationDate; return this; }
    public RecoveryCase currentRestorePercent(BigDecimal currentRestorePercent) { this.currentRestorePercent = currentRestorePercent; return this; }
    public RecoveryCase ownerId(UUID ownerId) { this.ownerId = ownerId; return this; }
    public RecoveryCase createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
    public RecoveryCase updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
    public RecoveryCase isNew(boolean isNew) { this.isNew = isNew; return this; }
    public RecoveryCase build() { return this; }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @Override
    public UUID getId() {
        return recoveryCaseId;
    }

    public UUID getRecoveryCaseId() { return recoveryCaseId; }
    public void setRecoveryCaseId(UUID recoveryCaseId) { this.recoveryCaseId = recoveryCaseId; }
    public UUID getClaimId() { return claimId; }
    public void setClaimId(UUID claimId) { this.claimId = claimId; }
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public RecoverySeverity getSeverity() { return severity; }
    public void setSeverity(RecoverySeverity severity) { this.severity = severity; }
    public RecoveryCaseStatus getStatus() { return status; }
    public void setStatus(RecoveryCaseStatus status) { this.status = status; }
    public String getRecoveryObjective() { return recoveryObjective; }
    public void setRecoveryObjective(String recoveryObjective) { this.recoveryObjective = recoveryObjective; }
    public RecoveryPath getRecoveryPath() { return recoveryPath; }
    public void setRecoveryPath(RecoveryPath recoveryPath) { this.recoveryPath = recoveryPath; }
    public String getRecoveryNotes() { return recoveryNotes; }
    public void setRecoveryNotes(String recoveryNotes) { this.recoveryNotes = recoveryNotes; }
    public LocalDate getTargetRestoreDate() { return targetRestoreDate; }
    public void setTargetRestoreDate(LocalDate targetRestoreDate) { this.targetRestoreDate = targetRestoreDate; }
    public LocalDate getActualRestorationDate() { return actualRestorationDate; }
    public void setActualRestorationDate(LocalDate actualRestorationDate) { this.actualRestorationDate = actualRestorationDate; }
    public BigDecimal getCurrentRestorePercent() { return currentRestorePercent; }
    public void setCurrentRestorePercent(BigDecimal currentRestorePercent) { this.currentRestorePercent = currentRestorePercent; }
    public UUID getOwnerId() { return ownerId; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setNew(boolean isNew) { this.isNew = isNew; }
}