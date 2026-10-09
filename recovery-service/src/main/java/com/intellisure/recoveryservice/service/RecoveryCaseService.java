package com.intellisure.recoveryservice.service;

import com.intellisure.recoveryservice.client.VendorPartnerClient;
import com.intellisure.recoveryservice.dto.CompleteRecoveryCaseRequest;
import com.intellisure.recoveryservice.dto.CreateRecoveryCaseRequest;
import com.intellisure.recoveryservice.dto.RecordRecoveryProgressRequest;
import com.intellisure.recoveryservice.dto.RecoveryCaseFilterRequest;
import com.intellisure.recoveryservice.dto.RecoveryCaseListResponse;
import com.intellisure.recoveryservice.dto.RecoveryCaseResponse;
import com.intellisure.recoveryservice.dto.RecoveryEstimationRequest;
import com.intellisure.recoveryservice.dto.RecoveryEstimationResponse;
import com.intellisure.recoveryservice.dto.SelectRecoveryPathRequest;
import com.intellisure.recoveryservice.dto.UpdateRecoveryCaseRequest;
import com.intellisure.recoveryservice.dto.UpdateRecoveryStatusRequest;
import com.intellisure.recoveryservice.entity.RecoveryCase;
import com.intellisure.recoveryservice.entity.RecoveryCaseStatus;
import com.intellisure.recoveryservice.entity.RecoveryPath;
import com.intellisure.recoveryservice.entity.RecoverySeverity;
import com.intellisure.recoveryservice.repository.RecoveryCaseRepository;
import com.intellisure.recoveryservice.security.SecurityActorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecoveryCaseService {

    private final RecoveryCaseRepository recoveryCaseRepository;
    private final RecoveryEstimationService estimationService;
    private final VendorPartnerClient vendorPartnerClient;
    private final SecurityActorService securityActorService;

    public Mono<RecoveryCaseResponse> createCase(CreateRecoveryCaseRequest request) {
        return recoveryCaseRepository.findByClaimId(request.claimId())
                .flatMap(existing -> securityActorService
                        .assertCustomerAccess(existing.getCustomerId())
                        .thenReturn(mapToResponse(existing)))
                .switchIfEmpty(Mono.defer(() -> securityActorService
                        .assertCustomerAccess(request.customerId())
                        .then(Mono.defer(() -> {
                            RecoveryCase recoveryCase = RecoveryCase.builder()
                                    .recoveryCaseId(UUID.randomUUID())
                                    .claimId(request.claimId())
                                    .customerId(request.customerId())
                                    .severity(request.severity())
                                    .status(RecoveryCaseStatus.INITIATED)
                                    .recoveryPath(RecoveryPath.CUSTOMER_MANAGED)
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
                        }))));
    }

    public Mono<RecoveryCaseResponse> getCase(UUID recoveryCaseId) {
        return findCaseForCaller(recoveryCaseId)
                .map(this::mapToResponse);
    }

    public Mono<RecoveryCaseListResponse> getCases(RecoveryCaseFilterRequest filter) {
        return securityActorService.hasAnyRole("CLAIMS_ADJUSTER", "CLAIMS_MANAGER", "SYSTEM_ADMINISTRATOR", "ADMIN")
                .flatMapMany(isStaff -> isStaff
                        ? findCasesForStaff(filter)
                        : securityActorService.currentCustomerId()
                                .flatMapMany(recoveryCaseRepository::findByCustomerId))
                .map(this::mapToResponse)
                .collectList()
                .map(list -> new RecoveryCaseListResponse(list, 
                        filter.page() != null ? filter.page() : 0, 
                        filter.size() != null ? filter.size() : 20, 
                        (long) list.size()));
    }

    public Mono<RecoveryCaseResponse> updateCase(UUID recoveryCaseId, UpdateRecoveryCaseRequest request) {
        return findCaseForCaller(recoveryCaseId)
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

    public Mono<RecoveryCaseResponse> selectRecoveryPath(UUID recoveryCaseId, SelectRecoveryPathRequest request) {
        if (request == null || request.recoveryPath() == null) {
            return Mono.error(new IllegalArgumentException("Recovery path is required and cannot be null"));
        }

        return findCaseForCaller(recoveryCaseId)
                .flatMap(recoveryCase -> {
                    if (recoveryCase.getStatus() == RecoveryCaseStatus.COMPLETED
                            || recoveryCase.getStatus() == RecoveryCaseStatus.CANCELLED) {
                        return Mono.error(new IllegalStateException(
                                "Cannot change recovery path for case in status: " + recoveryCase.getStatus()));
                    }

                    recoveryCase.setRecoveryPath(request.recoveryPath());
                    if (request.notes() != null && !request.notes().isBlank()) {
                        recoveryCase.setRecoveryNotes(request.notes());
                    }
                    if (recoveryCase.getStatus() == RecoveryCaseStatus.INITIATED) {
                        recoveryCase.setStatus(RecoveryCaseStatus.PLANNING);
                    }
                    recoveryCase.setUpdatedAt(LocalDateTime.now());
                    recoveryCase.setNew(false);

                    Mono<Void> assignmentMono = Mono.empty();
                    if (request.recoveryPath() == RecoveryPath.NETWORK_VENDOR && request.vendorId() != null) {
                        log.info("Dispatching optional network vendor assignment for case {} with vendor {}",
                                recoveryCaseId, request.vendorId());
                        assignmentMono = vendorPartnerClient.createVendorAssignment(
                                request.vendorId(),
                                recoveryCase.getClaimId(),
                                recoveryCase.getRecoveryCaseId(),
                                request.taskDescription(),
                                request.dueDate()
                        ).then();
                    } else if (request.recoveryPath() != RecoveryPath.NETWORK_VENDOR) {
                        log.info("Customer-owned recovery selected ({}); no vendor assignment dispatched for case {}",
                                request.recoveryPath(), recoveryCaseId);
                    }

                    return assignmentMono
                            .then(recoveryCaseRepository.save(recoveryCase))
                            .map(this::mapToResponse);
                });
    }

    public Mono<RecoveryCaseResponse> selectRecoveryPath(UUID recoveryCaseId, RecoveryPath recoveryPath) {
        if (recoveryPath == null) {
            return Mono.error(new IllegalArgumentException("Recovery path is required and cannot be null"));
        }
        return selectRecoveryPath(recoveryCaseId, new SelectRecoveryPathRequest(recoveryPath, null, null, null, null));
    }

    public Mono<RecoveryCaseResponse> selectRecoveryPath(UUID recoveryCaseId, String recoveryPathStr) {
        if (recoveryPathStr == null || recoveryPathStr.isBlank()) {
            return Mono.error(new IllegalArgumentException("Recovery path is required and cannot be null"));
        }
        try {
            RecoveryPath path = RecoveryPath.valueOf(recoveryPathStr.trim().toUpperCase());
            return selectRecoveryPath(recoveryCaseId, path);
        } catch (IllegalArgumentException ex) {
            return Mono.error(new IllegalArgumentException("Invalid recovery path: " + recoveryPathStr));
        }
    }

    public Mono<RecoveryCaseResponse> recordProgress(UUID recoveryCaseId, RecordRecoveryProgressRequest request) {
        if (request == null || request.restorePercent() == null) {
            return Mono.error(new IllegalArgumentException("Restore percent is required"));
        }
        return findCaseForCaller(recoveryCaseId)
                .flatMap(recoveryCase -> {
                    if (recoveryCase.getStatus() == RecoveryCaseStatus.COMPLETED
                            || recoveryCase.getStatus() == RecoveryCaseStatus.CANCELLED) {
                        return Mono.error(new IllegalStateException(
                                "Cannot record progress on a case that is " + recoveryCase.getStatus()));
                    }
                    recoveryCase.setCurrentRestorePercent(request.restorePercent());
                    if (request.notes() != null && !request.notes().isBlank()) {
                        recoveryCase.setRecoveryNotes(request.notes());
                    }
                    if (request.restorePercent().compareTo(BigDecimal.valueOf(100)) >= 0) {
                        recoveryCase.setStatus(RecoveryCaseStatus.BUSINESS_RESTORED);
                        if (recoveryCase.getActualRestorationDate() == null) {
                            recoveryCase.setActualRestorationDate(LocalDate.now());
                        }
                    } else if (request.restorePercent().compareTo(BigDecimal.valueOf(50)) >= 0) {
                        if (recoveryCase.getStatus() == RecoveryCaseStatus.INITIATED
                                || recoveryCase.getStatus() == RecoveryCaseStatus.PLANNING
                                || recoveryCase.getStatus() == RecoveryCaseStatus.IN_PROGRESS) {
                            recoveryCase.setStatus(RecoveryCaseStatus.BUSINESS_PARTIALLY_RESTORED);
                        }
                    } else if (request.restorePercent().compareTo(BigDecimal.ZERO) > 0) {
                        if (recoveryCase.getStatus() == RecoveryCaseStatus.INITIATED
                                || recoveryCase.getStatus() == RecoveryCaseStatus.PLANNING) {
                            recoveryCase.setStatus(RecoveryCaseStatus.IN_PROGRESS);
                        }
                    }
                    recoveryCase.setUpdatedAt(LocalDateTime.now());
                    recoveryCase.setNew(false);
                    return recoveryCaseRepository.save(recoveryCase);
                })
                .map(this::mapToResponse);
    }

    public void validateStatusTransition(RecoveryCaseStatus currentStatus, RecoveryCaseStatus targetStatus) {
        if (currentStatus == null || targetStatus == null) {
            throw new IllegalArgumentException("Current and target status must not be null");
        }
        if (currentStatus == targetStatus) {
            return;
        }
        if (currentStatus == RecoveryCaseStatus.CANCELLED) {
            throw new IllegalStateException("Cannot transition a CANCELLED recovery case to " + targetStatus);
        }
        if (currentStatus == RecoveryCaseStatus.COMPLETED && targetStatus != RecoveryCaseStatus.REOPENED) {
            throw new IllegalStateException("Cannot transition a COMPLETED recovery case to " + targetStatus);
        }
        if (currentStatus == RecoveryCaseStatus.INITIATED &&
                (targetStatus == RecoveryCaseStatus.COMPLETED
                        || targetStatus == RecoveryCaseStatus.BUSINESS_RESTORED
                        || targetStatus == RecoveryCaseStatus.BUSINESS_PARTIALLY_RESTORED)) {
            throw new IllegalStateException("Cannot transition directly from INITIATED to " + targetStatus
                    + " without recovery planning or progress");
        }
        if (targetStatus == RecoveryCaseStatus.REOPENED
                && currentStatus != RecoveryCaseStatus.COMPLETED
                && currentStatus != RecoveryCaseStatus.CANCELLED
                && currentStatus != RecoveryCaseStatus.ON_HOLD) {
            throw new IllegalStateException("Cannot reopen a recovery case that is currently " + currentStatus);
        }
    }

    public void validateStatusTransition(String currentStatus, String targetStatus) {
        if (currentStatus == null || targetStatus == null) {
            throw new IllegalArgumentException("Current and target status must not be null");
        }
        try {
            RecoveryCaseStatus current = RecoveryCaseStatus.valueOf(currentStatus.trim().toUpperCase());
            RecoveryCaseStatus target = RecoveryCaseStatus.valueOf(targetStatus.trim().toUpperCase());
            validateStatusTransition(current, target);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid status value provided: " + ex.getMessage(), ex);
        }
    }

    public Mono<RecoveryCaseResponse> updateStatus(UUID recoveryCaseId, UpdateRecoveryStatusRequest request) {
        if (request == null || request.status() == null) {
            return Mono.error(new IllegalArgumentException("Status is required"));
        }
        return findCaseForCaller(recoveryCaseId)
                .flatMap(recoveryCase -> {
                    try {
                        validateStatusTransition(recoveryCase.getStatus(), request.status());
                    } catch (IllegalStateException | IllegalArgumentException ex) {
                        return Mono.error(ex);
                    }
                    recoveryCase.setStatus(request.status());
                    recoveryCase.setUpdatedAt(LocalDateTime.now());
                    recoveryCase.setNew(false);

                    if (request.status() == RecoveryCaseStatus.COMPLETED
                            || request.status() == RecoveryCaseStatus.BUSINESS_RESTORED) {
                        recoveryCase.setCurrentRestorePercent(BigDecimal.valueOf(100));
                        if (recoveryCase.getActualRestorationDate() == null) {
                            recoveryCase.setActualRestorationDate(LocalDate.now());
                        }
                    }

                    return recoveryCaseRepository.save(recoveryCase);
                })
                .map(this::mapToResponse);
    }

    public Mono<RecoveryCaseResponse> completeCase(UUID recoveryCaseId, CompleteRecoveryCaseRequest request) {
        return findCaseForCaller(recoveryCaseId)
                .flatMap(recoveryCase -> {
                    try {
                        validateStatusTransition(recoveryCase.getStatus(), RecoveryCaseStatus.COMPLETED);
                    } catch (IllegalStateException | IllegalArgumentException ex) {
                        return Mono.error(ex);
                    }
                    recoveryCase.setStatus(RecoveryCaseStatus.COMPLETED);
                    recoveryCase.setCurrentRestorePercent(BigDecimal.valueOf(100));
                    if (recoveryCase.getActualRestorationDate() == null) {
                        recoveryCase.setActualRestorationDate(LocalDate.now());
                    }
                    recoveryCase.setUpdatedAt(LocalDateTime.now());
                    recoveryCase.setNew(false);
                    if (request != null && request.completionSummary() != null) {
                        String summary = "COMPLETION: " + request.completionSummary()
                                + (request.outcome() != null ? "\nOUTCOME: " + request.outcome() : "");
                        if (recoveryCase.getRecoveryObjective() != null) {
                            recoveryCase.setRecoveryObjective(recoveryCase.getRecoveryObjective() + "\n" + summary);
                        } else {
                            recoveryCase.setRecoveryObjective(summary);
                        }
                    }
                    return recoveryCaseRepository.save(recoveryCase);
                })
                .map(this::mapToResponse);
    }

    public Mono<RecoveryEstimationResponse> estimateRecoveryAsync(RecoveryEstimationRequest request) {
        return Mono.fromFuture(estimationService.estimateRecoveryAsync(request));
    }

    private Flux<RecoveryCase> findCasesForStaff(RecoveryCaseFilterRequest filter) {
        if (filter.customerId() != null) return recoveryCaseRepository.findByCustomerId(filter.customerId());
        if (filter.ownerId() != null) return recoveryCaseRepository.findByOwnerId(filter.ownerId());
        if (filter.status() != null) return recoveryCaseRepository.findByStatus(filter.status());
        if (filter.severity() != null) return recoveryCaseRepository.findBySeverity(filter.severity());
        return recoveryCaseRepository.findAll();
    }

    private Mono<RecoveryCase> findCaseForCaller(UUID recoveryCaseId) {
        return recoveryCaseRepository.findById(recoveryCaseId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Recovery case not found: " + recoveryCaseId)))
                .flatMap(recoveryCase -> securityActorService.assertCustomerAccess(recoveryCase.getCustomerId())
                        .thenReturn(recoveryCase));
    }

    private RecoveryCaseResponse mapToResponse(RecoveryCase recoveryCase) {
        return new RecoveryCaseResponse(
                recoveryCase.getRecoveryCaseId(),
                recoveryCase.getClaimId(),
                recoveryCase.getCustomerId(),
                recoveryCase.getSeverity(),
                recoveryCase.getStatus(),
                recoveryCase.getRecoveryPath(),
                recoveryCase.getRecoveryObjective(),
                recoveryCase.getRecoveryNotes(),
                recoveryCase.getTargetRestoreDate(),
                recoveryCase.getActualRestorationDate(),
                recoveryCase.getCurrentRestorePercent(),
                recoveryCase.getOwnerId(),
                recoveryCase.getCreatedAt(),
                recoveryCase.getUpdatedAt()
        );
    }
}
