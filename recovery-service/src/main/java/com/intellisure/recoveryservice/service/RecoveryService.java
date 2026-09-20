package com.intellisure.recoveryservice.service;

import com.intellisure.recoveryservice.dto.InitiateRecoveryRequest;
import com.intellisure.recoveryservice.dto.RecoveryCaseResponse;
import com.intellisure.recoveryservice.entity.RecoveryCase;
import com.intellisure.recoveryservice.repository.RecoveryCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecoveryService {

    private final RecoveryCaseRepository recoveryCaseRepository;

    public Flux<RecoveryCaseResponse> getCases(UUID customerId) {
        return (customerId == null ? recoveryCaseRepository.findAll() : recoveryCaseRepository.findByCustomerId(customerId))
                .map(this::mapToResponse);
    }

    public Mono<RecoveryCaseResponse> getCase(UUID id) {
        return recoveryCaseRepository.findById(id).map(this::mapToResponse)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Recovery case not found: " + id)));
    }

    public Mono<RecoveryCaseResponse> initiateRecovery(InitiateRecoveryRequest request) {
        LocalDateTime now = LocalDateTime.now();
        
        RecoveryCase recoveryCase = RecoveryCase.builder()
                .recoveryCaseId(UUID.randomUUID())
                .claimId(request.claimId())
                .customerId(request.customerId())
                .severity(request.severity())
                .status("INITIATED")
                .recoveryObjective(request.recoveryObjective())
                .targetRestoreDate(LocalDate.now().plusMonths(1)) // Default to 1 month
                .currentRestorePercent(BigDecimal.ZERO)
                .createdAt(now)
                .updatedAt(now)
                .isNew(true)
                .build();

        return recoveryCaseRepository.save(recoveryCase)
                .map(this::mapToResponse);
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
                recoveryCase.getCreatedAt(),
                recoveryCase.getUpdatedAt()
        );
    }
}
