package com.intellisure.vendorpartnerservice.entity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Table("vendor_assignment")
public class VendorAssignment implements Persistable<UUID> {

    @Id
    @Column("assignment_id")
    private UUID assignmentId;

    @Column("vendor_id")
    private UUID vendorId;

    @Column("assignment_type")
    private AssignmentType assignmentType;

    @Column("claim_id")
    private UUID claimId;

    @Column("recovery_case_id")
    private UUID recoveryCaseId;

    private AssignmentStatus status;
    
    @Column("task_description")
    private String taskDescription;
    
    @Column("due_date")
    private LocalDate dueDate;
    
    private String priority;
    
    @Column("accepted_at")
    private LocalDateTime acceptedAt;
    
    @Column("completed_at")
    private LocalDateTime completedAt;
    
    @Column("evidence_document_ids")
    /* JSON is stored as text by the existing R2DBC list converter. */
    private List<String> evidenceDocumentIds;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Transient
    private boolean isNew = true;

    public static VendorAssignment builder() {
        return new VendorAssignment();
    }
    
    public VendorAssignment assignmentId(UUID assignmentId) { this.assignmentId = assignmentId; return this; }
    public VendorAssignment vendorId(UUID vendorId) { this.vendorId = vendorId; return this; }
    public VendorAssignment assignmentType(AssignmentType assignmentType) { this.assignmentType = assignmentType; return this; }
    public VendorAssignment claimId(UUID claimId) { this.claimId = claimId; return this; }
    public VendorAssignment recoveryCaseId(UUID recoveryCaseId) { this.recoveryCaseId = recoveryCaseId; return this; }
    public VendorAssignment status(AssignmentStatus status) { this.status = status; return this; }
    public VendorAssignment taskDescription(String taskDescription) { this.taskDescription = taskDescription; return this; }
    public VendorAssignment dueDate(LocalDate dueDate) { this.dueDate = dueDate; return this; }
    public VendorAssignment priority(String priority) { this.priority = priority; return this; }
    public VendorAssignment acceptedAt(LocalDateTime acceptedAt) { this.acceptedAt = acceptedAt; return this; }
    public VendorAssignment completedAt(LocalDateTime completedAt) { this.completedAt = completedAt; return this; }
    public VendorAssignment evidenceDocumentIds(List<String> evidenceDocumentIds) { this.evidenceDocumentIds = evidenceDocumentIds; return this; }
    public VendorAssignment createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
    public VendorAssignment updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
    public VendorAssignment isNew(boolean isNew) { this.isNew = isNew; return this; }
    
    public VendorAssignment build() { return this; }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @Override
    public UUID getId() {
        return assignmentId;
    }

    public UUID getAssignmentId() { return assignmentId; }
    public void setAssignmentId(UUID assignmentId) { this.assignmentId = assignmentId; }
    public UUID getVendorId() { return vendorId; }
    public void setVendorId(UUID vendorId) { this.vendorId = vendorId; }
    public AssignmentType getAssignmentType() { return assignmentType; }
    public void setAssignmentType(AssignmentType assignmentType) { this.assignmentType = assignmentType; }
    public UUID getClaimId() { return claimId; }
    public void setClaimId(UUID claimId) { this.claimId = claimId; }
    public UUID getRecoveryCaseId() { return recoveryCaseId; }
    public void setRecoveryCaseId(UUID recoveryCaseId) { this.recoveryCaseId = recoveryCaseId; }
    public AssignmentStatus getStatus() { return status; }
    public void setStatus(AssignmentStatus status) { this.status = status; }
    public String getTaskDescription() { return taskDescription; }
    public void setTaskDescription(String taskDescription) { this.taskDescription = taskDescription; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public LocalDateTime getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(LocalDateTime acceptedAt) { this.acceptedAt = acceptedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public List<String> getEvidenceDocumentIds() { return evidenceDocumentIds; }
    public void setEvidenceDocumentIds(List<String> evidenceDocumentIds) { this.evidenceDocumentIds = evidenceDocumentIds; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setNew(boolean isNew) { this.isNew = isNew; }
}
