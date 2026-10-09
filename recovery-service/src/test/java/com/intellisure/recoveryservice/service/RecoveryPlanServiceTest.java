package com.intellisure.recoveryservice.service;

import com.intellisure.recoveryservice.dto.CreateRecoveryPlanRequest;
import com.intellisure.recoveryservice.entity.RecoveryCase;
import com.intellisure.recoveryservice.entity.RecoveryPlan;
import com.intellisure.recoveryservice.entity.RecoveryPlanStatus;
import com.intellisure.recoveryservice.repository.RecoveryCaseRepository;
import com.intellisure.recoveryservice.repository.RecoveryPlanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecoveryPlanServiceTest {
    @Mock RecoveryPlanRepository planRepository;
    @Mock RecoveryCaseRepository caseRepository;
    @InjectMocks RecoveryPlanService service;

    @Test
    void createsPlanWithDefaultDraftStatusAndMapsIt() {
        UUID caseId = UUID.randomUUID();
        when(caseRepository.findById(caseId)).thenReturn(Mono.just(RecoveryCase.builder().recoveryCaseId(caseId).build()));
        when(planRepository.save(any(RecoveryPlan.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        CreateRecoveryPlanRequest request = new CreateRecoveryPlanRequest(caseId, "Plan", List.of("Action"),
                List.of(UUID.randomUUID()), List.of("Workspace"), "Milestone", null);
        StepVerifier.create(service.createPlan(request))
                .assertNext(response -> assertEquals(RecoveryPlanStatus.DRAFT, response.status())).verifyComplete();
    }

    @Test
    void createsActivePlanAndHandlesMissingPlans() {
        UUID caseId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        RecoveryPlan plan = plan(planId, caseId, RecoveryPlanStatus.ACTIVE);
        when(caseRepository.findById(caseId)).thenReturn(Mono.just(RecoveryCase.builder().recoveryCaseId(caseId).build()));
        when(planRepository.save(any(RecoveryPlan.class))).thenReturn(Mono.just(plan));
        when(planRepository.findById(planId)).thenReturn(Mono.just(plan));
        when(planRepository.findByRecoveryCaseId(caseId)).thenReturn(Mono.just(plan));
        when(planRepository.findByStatus(RecoveryPlanStatus.ACTIVE)).thenReturn(Flux.just(plan));
        when(planRepository.save(plan)).thenReturn(Mono.just(plan));

        StepVerifier.create(service.getPlan(planId)).assertNext(r -> assertEquals(planId, r.recoveryPlanId())).verifyComplete();
        StepVerifier.create(service.getPlanByCaseId(caseId)).expectNextCount(1).verifyComplete();
        StepVerifier.create(service.getPlansByStatus(RecoveryPlanStatus.ACTIVE)).expectNextCount(1).verifyComplete();
        StepVerifier.create(service.updatePlanStatus(planId, RecoveryPlanStatus.COMPLETED)).assertNext(r -> assertEquals(RecoveryPlanStatus.COMPLETED, r.status())).verifyComplete();
        when(caseRepository.findById(caseId)).thenReturn(Mono.empty());
        StepVerifier.create(service.createPlan(new CreateRecoveryPlanRequest(caseId, "Plan", List.of(), List.of(), List.of(), "Milestone", null)))
                .expectError(IllegalArgumentException.class).verify();
        when(planRepository.findById(planId)).thenReturn(Mono.empty());
        StepVerifier.create(service.getPlan(planId)).expectError(IllegalArgumentException.class).verify();
        when(planRepository.findByRecoveryCaseId(caseId)).thenReturn(Mono.empty());
        StepVerifier.create(service.getPlanByCaseId(caseId)).expectError(IllegalArgumentException.class).verify();
    }

    private RecoveryPlan plan(UUID planId, UUID caseId, RecoveryPlanStatus status) {
        return RecoveryPlan.builder().recoveryPlanId(planId).recoveryCaseId(caseId).planSummary("Plan")
                .priorityActions(List.of("Action")).vendorAssignmentIds(List.of()).temporaryResourceNeeds(List.of())
                .targetMilestones("Milestone").status(status).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).isNew(false).build();
    }
}
