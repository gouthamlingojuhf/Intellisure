package com.intellisure.recoveryservice.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EntityContractTest {
    @Test
    void recoveryCasePersistenceContractAndAccessorsWork() {
        UUID id = UUID.randomUUID();
        RecoveryCase entity = RecoveryCase.builder().recoveryCaseId(id).claimId(id).customerId(id)
                .severity(RecoverySeverity.HIGH).status(RecoveryCaseStatus.PLANNING).recoveryPath(RecoveryPath.CUSTOMER_MANAGED)
                .recoveryObjective("objective").recoveryNotes("notes").targetRestoreDate(LocalDate.now())
                .actualRestorationDate(LocalDate.now()).currentRestorePercent(BigDecimal.TEN).ownerId(id)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).isNew(true).build();
        assertEquals(id, entity.getId()); assertTrue(entity.isNew());
        entity.setRecoveryCaseId(id); entity.setClaimId(id); entity.setCustomerId(id); entity.setSeverity(RecoverySeverity.CRITICAL);
        entity.setStatus(RecoveryCaseStatus.COMPLETED); entity.setRecoveryPath(RecoveryPath.NETWORK_VENDOR);
        entity.setRecoveryObjective("new"); entity.setRecoveryNotes("new notes"); entity.setTargetRestoreDate(LocalDate.now());
        entity.setActualRestorationDate(LocalDate.now()); entity.setCurrentRestorePercent(BigDecimal.valueOf(100)); entity.setOwnerId(id);
        entity.setCreatedAt(LocalDateTime.now()); entity.setUpdatedAt(LocalDateTime.now()); entity.setNew(false);
        assertFalse(entity.isNew()); assertEquals(RecoveryCaseStatus.COMPLETED, entity.getStatus());
    }

    @Test
    void planAndSupportRequestPersistenceContractsWork() {
        UUID id = UUID.randomUUID();
        RecoveryPlan plan = RecoveryPlan.builder().recoveryPlanId(id).recoveryCaseId(id).planSummary("plan")
                .priorityActions(List.of("repair")).vendorAssignmentIds(List.of(id)).temporaryResourceNeeds(List.of("space"))
                .targetMilestones("milestone").status(RecoveryPlanStatus.DRAFT).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).isNew(true).build();
        assertEquals(id, plan.getId()); assertTrue(plan.isNew());
        plan.setRecoveryPlanId(id); plan.setRecoveryCaseId(id); plan.setPlanSummary("new"); plan.setPriorityActions(List.of());
        plan.setVendorAssignmentIds(List.of()); plan.setTemporaryResourceNeeds(List.of()); plan.setTargetMilestones("new milestone");
        plan.setStatus(RecoveryPlanStatus.COMPLETED); plan.setCreatedAt(LocalDateTime.now()); plan.setUpdatedAt(LocalDateTime.now()); plan.setNew(false);
        assertFalse(plan.isNew());

        RecoverySupportRequest request = RecoverySupportRequest.builder().supportRequestId(id).recoveryCaseId(id)
                .supportType(SupportType.REPAIR).description("repair").priority(SupportPriority.HIGH)
                .status(RecoverySupportStatus.PENDING).requiredByDate(LocalDate.now()).location("site").vendorAssignmentId(id)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).isNew(true).build();
        assertEquals(id, request.getId()); assertTrue(request.isNew());
        request.setSupportRequestId(id); request.setRecoveryCaseId(id); request.setSupportType(SupportType.OTHER); request.setDescription("new");
        request.setPriority(SupportPriority.CRITICAL); request.setStatus(RecoverySupportStatus.COMPLETED); request.setRequiredByDate(LocalDate.now());
        request.setLocation("new site"); request.setVendorAssignmentId(id); request.setCreatedAt(LocalDateTime.now()); request.setUpdatedAt(LocalDateTime.now()); request.setNew(false);
        assertFalse(request.isNew()); assertEquals(RecoverySupportStatus.COMPLETED, request.getStatus());
    }

    @Test
    void enumsExposeAllPersistedValues() {
        assertTrue(RecoveryCaseStatus.values().length > 5);
        assertEquals(3, RecoveryPath.values().length);
        assertEquals(4, RecoverySeverity.values().length);
        assertTrue(RecoveryPlanStatus.values().length > 3);
        assertTrue(RecoverySupportStatus.values().length > 4);
        assertEquals(4, SupportPriority.values().length);
        assertTrue(SupportType.values().length > 5);
    }
}
