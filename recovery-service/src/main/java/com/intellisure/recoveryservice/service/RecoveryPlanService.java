package com.intellisure.recoveryservice.service;

import com.intellisure.recoveryservice.dto.CreateRecoveryPlanRequest;
import com.intellisure.recoveryservice.dto.RecoveryPlanResponse;
import com.intellisure.recoveryservice.entity.RecoveryPlan;
import com.intellisure.recoveryservice.entity.RecoveryPlanStatus;
import com.intellisure.recoveryservice.repository.RecoveryPlanRepository;
import com.intellisure.recoveryservice.repository.RecoveryCaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecoveryPlanService {

    private final RecoveryPlanRepository recoveryPlanRepository;
    private final RecoveryCaseRepository recoveryCaseRepository;

    public Mono<RecoveryPlanResponse> createPlan(CreateRecoveryPlanRequest request) {
        // Verify recovery case exists
        return recoveryCaseRepository.findById(request.recoveryCaseId())
                .flatMap(recoveryCase -> {
                    RecoveryPlan plan = RecoveryPlan.builder()
                            .recoveryPlanId(UUID.randomUUID())
                            .recoveryCaseId(request.recoveryCaseId())
                            .planSummary(request.planSummary())
                            .priorityActions(request.priorityActions())
                            .vendorAssignmentIds(request.vendorAssignmentIds())
                            .temporaryResourceNeeds(request.temporaryResourceNeeds())
                            .targetMilestones(request.targetMilestones())
                            .status(request.status() != null ? request.status() : com.intellisure.recoveryservice.entity.RecoveryPlanStatus.DRAFT)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .isNew(true)
                            .build();

                    return recoveryPlanRepository.save(plan)
                            .map(this::mapToResponse);
                })
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Recovery case not found: " + request.recoveryCaseId())));
    }

    public Mono<RecoveryPlanResponse> getPlan(UUID recoveryPlanId) {
        return recoveryPlanRepository.findById(recoveryPlanId)
                .map(this::mapToResponse)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Recovery plan not found: " + recoveryPlanId)));
    }

    public Mono<RecoveryPlanResponse> getPlanByCaseId(UUID recoveryCaseId) {
        return recoveryPlanRepository.findByRecoveryCaseId(recoveryCaseId)
                .map(this::mapToResponse)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("No recovery plan found for case: " + recoveryCaseId)));
    }

    public Flux<RecoveryPlanResponse> getPlansByStatus(com.intellisure.recoveryservice.entity.RecoveryPlanStatus status) {
        return recoveryPlanRepository.findByStatus(status)
                .map(this::mapToResponse);
    }

    public Mono<RecoveryPlanResponse> updatePlanStatus(UUID recoveryPlanId, com.intellisure.recoveryservice.entity.RecoveryPlanStatus status) {
        return recoveryPlanRepository.findById(recoveryPlanId)
                .flatMap(plan -> {
                    plan.setStatus(status);
                    plan.setUpdatedAt(LocalDateTime.now());
                    plan.setNew(false);
                    return recoveryPlanRepository.save(plan);
                })
                .map(this::mapToResponse);
    }

    private RecoveryPlanResponse mapToResponse(RecoveryPlan plan) {
        return new RecoveryPlanResponse(
                plan.getRecoveryPlanId(),
                plan.getRecoveryCaseId(),
                plan.getPlanSummary(),
                plan.getPriorityActions(),
                plan.getVendorAssignmentIds(),
                plan.getTemporaryResourceNeeds(),
                plan.getTargetMilestones(),
                plan.getStatus(),
                plan.getCreatedAt(),
                plan.getUpdatedAt()
        );
    }
}