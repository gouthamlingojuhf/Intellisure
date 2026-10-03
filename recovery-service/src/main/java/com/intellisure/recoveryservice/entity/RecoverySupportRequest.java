package com.intellisure.recoveryservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("recovery_support_request")
public class RecoverySupportRequest implements Persistable<UUID> {

    @Id
    @Column("support_request_id")
    private UUID supportRequestId;

    @Column("recovery_case_id")
    private UUID recoveryCaseId;

    private SupportType supportType;
    private String description;
    private SupportPriority priority;
    private RecoverySupportStatus status;
    private LocalDate requiredByDate;
    private String location;
    private UUID vendorAssignmentId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Transient
    @Builder.Default
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return isNew;
    }

    @Override
    public UUID getId() {
        return supportRequestId;
    }

    public UUID getSupportRequestId() { return supportRequestId; }
    public void setSupportRequestId(UUID supportRequestId) { this.supportRequestId = supportRequestId; }
    public UUID getRecoveryCaseId() { return recoveryCaseId; }
    public void setRecoveryCaseId(UUID recoveryCaseId) { this.recoveryCaseId = recoveryCaseId; }
    public SupportType getSupportType() { return supportType; }
    public void setSupportType(SupportType supportType) { this.supportType = supportType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public SupportPriority getPriority() { return priority; }
    public void setPriority(SupportPriority priority) { this.priority = priority; }
    public RecoverySupportStatus getStatus() { return status; }
    public void setStatus(RecoverySupportStatus status) { this.status = status; }
    public LocalDate getRequiredByDate() { return requiredByDate; }
    public void setRequiredByDate(LocalDate requiredByDate) { this.requiredByDate = requiredByDate; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public UUID getVendorAssignmentId() { return vendorAssignmentId; }
    public void setVendorAssignmentId(UUID vendorAssignmentId) { this.vendorAssignmentId = vendorAssignmentId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setNew(boolean isNew) { this.isNew = isNew; }
}