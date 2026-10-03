package com.intellisure.recoveryservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("recovery_plan")
public class RecoveryPlan implements Persistable<UUID> {

    @Id
    @Column("recovery_plan_id")
    private UUID recoveryPlanId;

    @Column("recovery_case_id")
    private UUID recoveryCaseId;

    private String planSummary;
    
    @Column("priority_actions")
    private List<String> priorityActions;
    
    @Column("vendor_assignment_ids")
    private List<UUID> vendorAssignmentIds;
    
    @Column("temporary_resource_needs")
    private List<String> temporaryResourceNeeds;
    
    private String targetMilestones;
    
    private RecoveryPlanStatus status;

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
        return recoveryPlanId;
    }

    public UUID getRecoveryPlanId() { return recoveryPlanId; }
    public void setRecoveryPlanId(UUID recoveryPlanId) { this.recoveryPlanId = recoveryPlanId; }
    public UUID getRecoveryCaseId() { return recoveryCaseId; }
    public void setRecoveryCaseId(UUID recoveryCaseId) { this.recoveryCaseId = recoveryCaseId; }
    public String getPlanSummary() { return planSummary; }
    public void setPlanSummary(String planSummary) { this.planSummary = planSummary; }
    public List<String> getPriorityActions() { return priorityActions; }
    public void setPriorityActions(List<String> priorityActions) { this.priorityActions = priorityActions; }
    public List<UUID> getVendorAssignmentIds() { return vendorAssignmentIds; }
    public void setVendorAssignmentIds(List<UUID> vendorAssignmentIds) { this.vendorAssignmentIds = vendorAssignmentIds; }
    public List<String> getTemporaryResourceNeeds() { return temporaryResourceNeeds; }
    public void setTemporaryResourceNeeds(List<String> temporaryResourceNeeds) { this.temporaryResourceNeeds = temporaryResourceNeeds; }
    public String getTargetMilestones() { return targetMilestones; }
    public void setTargetMilestones(String targetMilestones) { this.targetMilestones = targetMilestones; }
    public RecoveryPlanStatus getStatus() { return status; }
    public void setStatus(RecoveryPlanStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setNew(boolean isNew) { this.isNew = isNew; }
}