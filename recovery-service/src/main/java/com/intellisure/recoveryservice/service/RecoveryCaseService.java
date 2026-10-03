package com.intellisure.recoveryservice.service;

import com.intellisure.recoveryservice.dto.CreateRecoveryCaseRequest;
import com.intellisure.recoveryservice.dto.RecoveryCaseFilterRequest;
import com.intellisure.recoveryservice.dto.RecoveryCaseListResponse;
import com.intellisure.recoveryservice.dto.RecoveryCaseResponse;
import com.intellisure.recoveryservice.dto.UpdateRecoveryCaseRequest;
import com.intellisure.recoveryservice.dto.UpdateRecoveryStatusRequest;
import com.intellisure.recoveryservice.dto.CompleteRecoveryCaseRequest;
import com.intellisure.recoveryservice.dto.RecoveryEstimationResponse;
import com.intellisure.recoveryservice.entity.RecoveryCase;
import com.intellisure.recoveryservice.entity.RecoveryCaseStatus;
import com.intellisure.recoveryservice.entity.RecoverySeverity;
import com.intellisure.recoveryservice.repository.RecoveryCaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecoveryCaseService {

    private final RecoveryCaseRepository recoveryCaseRepository;
    private final RecoveryEstimationService estimationService;

    public Mono<RecoveryCaseResponse> createCase(CreateRecoveryCaseRequest request) {
        RecoveryCase recoveryCase = RecoveryCase.builder()
                .recoveryCaseId(UUID.randomUUID())
                .claimId(request.claimId())
                .customerId(request.customerId())
                .severity(request.severity())
                .status(RecoveryCaseStatus.INITIATED)
                .recoveryObjective(request.recoveryObjective())
                .targetRestoreDate(request.targetRestoreDate())
                .currentRestorePercent(BigDecimal.ZERO)
                .ownerId(request.ownerId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isNew(true)
                .build();

        return recoveryCaseRepository.save(recoveryCase)
                .map(this::mapToResponse);
    }

    public Mono<RecoveryCaseResponse> getCase(UUID recoveryCaseId) {
        return recoveryCaseRepository.findById(recoveryCaseId)
                .map(this::mapToResponse)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Recovery case not found: " + recoveryCaseId)));
    }

    public Mono<RecoveryCaseListResponse> getCases(RecoveryCaseFilterRequest filter) {
        Flux<RecoveryCase> cases;

        if (filter.customerId() != null) {
            cases = recoveryCaseRepository.findByCustomerId(filter.customerId());
        } else if (filter.ownerId() != null) {
            cases = recoveryCaseRepository.findByOwnerId(filter.ownerId());
        } else if (filter.status() != null) {
            cases = recoveryCaseRepository.findByStatus(filter.status());
        } else if (filter.severity() != null) {
            cases = recoveryCaseRepository.findBySeverity(filter.severity());
        } else {
            cases = recoveryCaseRepository.findAll();
        }

        return cases
                .map(this::mapToResponse)
                .collectList()
                .map(list -> new RecoveryCaseListResponse(list, 
                        filter.page() != null ? filter.page() : 0, 
                        filter.size() != null ? filter.size() : 20, 
                        (long) list.size()));
    }

    public Mono<RecoveryCaseResponse> updateCase(UUID recoveryCaseId, UpdateRecoveryCaseRequest request) {
        return recoveryCaseRepository.findById(recoveryCaseId)
                .flatMap(recoveryCase -> {
                    if (request.severity() != null) recoveryCase.setSeverity(request.severity());
                    if (request.recoveryObjective() != null) recoveryCase.setRecoveryObjective(request.recoveryObjective());
                    if (request.targetRestoreDate() != null) recoveryCase.setTargetRestoreDate(request.targetRestoreDate());
                    if (request.currentRestorePercent() != null) recoveryCase.setCurrentRestorePercent(request.currentRestorePercent());
                    if (request.ownerId() != null) recoveryCase.setOwnerId(request.ownerId());
                    recoveryCase.setUpdatedAt(LocalDateTime.now());
                    recoveryCase.setNew(false);
                    return recoveryCaseRepository.save(recoveryCase);
                })
                .map(this::mapToResponse);
    }

    public Mono<RecoveryCaseResponse> updateStatus(UUID recoveryCaseId, UpdateRecoveryStatusRequest request) {
        return recoveryCaseRepository.findById(recoveryCaseId)
                .flatMap(recoveryCase -> {
                    recoveryCase.setStatus(request.status());
                    recoveryCase.setUpdatedAt(LocalDateTime.now());
                    recoveryCase.setNew(false);
                    
                    // If completing, set restore percent to 100%
                    if (request.status() == com.intellisure.recoveryservice.entity.RecoveryCaseStatus.COMPLETED) {
                        recoveryCase.setCurrentRestorePercent(java.math.BigDecimal.valueOf(100));
                    }
                    
                    return recoveryCaseRepository.save(recoveryCase);
                })
                .map(this::mapToResponse);
    }

    public Mono<RecoveryCaseResponse> completeCase(UUID recoveryCaseId, com.intellisure.recoveryservice.dto.CompleteRecoveryCaseRequest request) {
        return recoveryCaseRepository.findById(recoveryCaseId)
                .flatMap(recoveryCase -> {
                    recoveryCase.setStatus(com.intellisure.recoveryservice.entity.RecoveryCaseStatus.COMPLETED);
                    recoveryCase.setCurrentRestorePercent(java.math.BigDecimal.valueOf(100));
                    recoveryCase.setUpdatedAt(LocalDateTime.now());
                    recoveryCase.setNew(false);
                    // Store completion summary in recoveryObjective or add a new field
                    recoveryCase.setRecoveryObjective(recoveryCase.getRecoveryObjective() + "\nCOMPLETION: " + request.completionSummary() + "\nOUTCOME: " + request.outcome());
                    return recoveryCaseRepository.save(recoveryCase);
                })
                .map(this::mapToResponse);
    }

    public Mono<RecoveryEstimationResponse> estimateRecoveryAsync(com.intellisure.recoveryservice.dto.RecoveryEstimationRequest request) {
        return Mono.fromFuture(estimationService.estimateRecoveryAsync(request));
    }

    private RecoveryCaseResponse mapToResponse(RecoveryCase recoveryCase) {
        return new RecoveryCaseResponse(
                recoveryCase.getRecoveryCaseId(),
                recoveryCase.getClaimId(),
                recoveryCase.getCustomerId(),
                recoveryCase.getSeverity(),
                recoveryCase.getStatus(),
                recoveryCase.getRecoveryObjective(),
                recoveryCase.getTargetRestoreDate(),
                recoveryCase.getCurrentRestorePercent(),
                recoveryCase.getOwnerId(),
                recoveryCase.getCreatedAt(),
                recoveryCase.getUpdatedAt()
        );
    }
}