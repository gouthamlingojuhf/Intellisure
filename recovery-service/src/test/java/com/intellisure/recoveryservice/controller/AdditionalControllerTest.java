package com.intellisure.recoveryservice.controller;

import com.intellisure.recoveryservice.dto.*;
import com.intellisure.recoveryservice.entity.*;
import com.intellisure.recoveryservice.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdditionalControllerTest {
    @Mock RecoveryCaseService caseService;
    @Mock RecoveryEstimationService estimationService;
    @Mock RecoveryPlanService planService;
    @Mock RecoverySupportRequestService supportService;
    @InjectMocks RecoveryCaseController caseController;
    @InjectMocks RecoveryEstimationController estimationController;
    @InjectMocks RecoveryPlanController planController;
    @InjectMocks RecoverySupportRequestController supportController;

    @Test
    void caseControllerDelegatesCreateReadUpdateStatusCompleteAndEstimate() {
        UUID id = UUID.randomUUID();
        CreateRecoveryCaseRequest create = new CreateRecoveryCaseRequest(id, id, RecoverySeverity.HIGH, "restore", null, null);
        UpdateRecoveryCaseRequest update = new UpdateRecoveryCaseRequest(null, "new", null, null, null);
        UpdateRecoveryStatusRequest status = new UpdateRecoveryStatusRequest(RecoveryCaseStatus.PLANNING);
        CompleteRecoveryCaseRequest complete = new CompleteRecoveryCaseRequest("done", "success");
        RecoveryEstimationRequest estimate = new RecoveryEstimationRequest(id, id, BigDecimal.TEN, .2, RecoverySeverity.LOW, null, null);
        when(caseService.createCase(create)).thenReturn(Mono.empty()); when(caseService.getCase(id)).thenReturn(Mono.empty());
        when(caseService.getCases(any())).thenReturn(Mono.empty()); when(caseService.updateCase(id, update)).thenReturn(Mono.empty());
        when(caseService.updateStatus(id, status)).thenReturn(Mono.empty()); when(caseService.completeCase(id, complete)).thenReturn(Mono.empty());
        when(caseService.estimateRecoveryAsync(estimate)).thenReturn(Mono.empty());

        caseController.createCase(create); caseController.getCase(id);
        caseController.getCases(id, id, RecoveryCaseStatus.IN_PROGRESS, RecoverySeverity.HIGH, LocalDate.now(), LocalDate.now(), 0, 20);
        caseController.updateCase(id, update); caseController.updateStatus(id, status); caseController.updateProgress(id, new RecordRecoveryProgressRequest(BigDecimal.TEN, "progress")); caseController.completeCase(id, complete);
        caseController.estimateRecovery(id, estimate);
        verify(caseService).createCase(create); verify(caseService).getCase(id); verify(caseService).updateCase(id, update);
        verify(caseService).updateStatus(id, status); verify(caseService).completeCase(id, complete); verify(caseService).estimateRecoveryAsync(estimate);
    }

    @Test
    void estimationControllerDelegatesAsyncEstimate() {
        RecoveryEstimationRequest request = new RecoveryEstimationRequest(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.TEN, .2, RecoverySeverity.LOW, null, null);
        RecoveryEstimationResponse response = new RecoveryEstimationResponse(UUID.randomUUID(), request.claimId(), BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, "notes", null);
        when(estimationService.estimateRecoveryAsync(request)).thenReturn(CompletableFuture.completedFuture(response));
        org.junit.jupiter.api.Assertions.assertEquals(response, estimationController.estimateRecovery(request).block());
    }

    @Test
    void planControllerBuildsPathBoundRequestAndSupportsStatusOperations() {
        UUID caseId = UUID.randomUUID(); UUID planId = UUID.randomUUID();
        CreateRecoveryPlanRequest request = new CreateRecoveryPlanRequest(null, "Plan", List.of("Action"), List.of(planId), List.of("Space"), "Milestone", RecoveryPlanStatus.ACTIVE);
        when(planService.createPlan(any())).thenReturn(Mono.empty()); when(planService.getPlanByCaseId(caseId)).thenReturn(Mono.empty());
        when(planService.getPlan(planId)).thenReturn(Mono.empty()); when(planService.getPlansByStatus(RecoveryPlanStatus.ACTIVE)).thenReturn(Flux.empty());
        when(planService.updatePlanStatus(planId, RecoveryPlanStatus.COMPLETED)).thenReturn(Mono.empty());
        planController.createPlan(caseId, request); planController.getPlanByCaseId(caseId); planController.getPlan(planId);
        planController.getPlansByStatus(RecoveryPlanStatus.ACTIVE); planController.getPlansByStatus(null);
        planController.updatePlanStatus(planId, RecoveryPlanStatus.COMPLETED);
        verify(planService).createPlan(argThat(r -> caseId.equals(r.recoveryCaseId())));
    }

    @Test
    void supportControllerBuildsPathBoundRequestAndSupportsAllQueryModes() {
        UUID caseId = UUID.randomUUID(); UUID requestId = UUID.randomUUID();
        CreateRecoverySupportRequest request = new CreateRecoverySupportRequest(null, SupportType.REPAIR, "repair", SupportPriority.HIGH, LocalDate.now(), "site", null);
        UpdateSupportRequestStatusRequest status = new UpdateSupportRequestStatusRequest(RecoverySupportStatus.APPROVED);
        when(supportService.createSupportRequest(any())).thenReturn(Mono.empty()); when(supportService.getSupportRequests(caseId)).thenReturn(Flux.empty());
        when(supportService.getSupportRequest(requestId)).thenReturn(Mono.empty()); when(supportService.getSupportRequestsByStatus(RecoverySupportStatus.PENDING)).thenReturn(Flux.empty());
        when(supportService.getSupportRequestsByPriority(SupportPriority.HIGH)).thenReturn(Flux.empty()); when(supportService.getSupportRequestsByType(SupportType.REPAIR)).thenReturn(Flux.empty());
        when(supportService.getOverdueSupportRequests()).thenReturn(Flux.empty()); when(supportService.updateStatus(requestId, status)).thenReturn(Mono.empty());
        supportController.createSupportRequest(caseId, request); supportController.getSupportRequests(caseId); supportController.getSupportRequest(requestId);
        supportController.getSupportRequestsByStatus(RecoverySupportStatus.PENDING, null, null);
        supportController.getSupportRequestsByStatus(null, SupportPriority.HIGH, null);
        supportController.getSupportRequestsByStatus(null, null, SupportType.REPAIR);
        supportController.getSupportRequestsByStatus(null, null, null); supportController.getOverdueSupportRequests(); supportController.updateSupportRequestStatus(requestId, status);
        verify(supportService).createSupportRequest(argThat(r -> caseId.equals(r.recoveryCaseId())));
    }
}
