package com.intellisure.recoveryservice.service;

import com.intellisure.recoveryservice.dto.RecoveryEstimationRequest;
import com.intellisure.recoveryservice.dto.RecoveryEstimationResponse;
import com.intellisure.recoveryservice.entity.RecoverySeverity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecoveryEstimationService {

    @Async("estimationExecutor")
    public CompletableFuture<RecoveryEstimationResponse> estimateRecoveryAsync(RecoveryEstimationRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Starting recovery estimation for claim: {}", request.claimId());

            // Subrogation estimate = claimPayout * faultPercentage (capped at third-party policy limit)
            BigDecimal subrogationEstimate = request.claimPayoutAmount()
                    .multiply(BigDecimal.valueOf(request.faultPercentage()))
                    .setScale(2, RoundingMode.HALF_UP);

            if (request.thirdPartyPolicyLimit() != null) {
                if (subrogationEstimate.compareTo(request.thirdPartyPolicyLimit()) > 0) {
                    subrogationEstimate = request.thirdPartyPolicyLimit();
                }
            }

            // Salvage estimate (if provided, otherwise estimate based on severity)
            BigDecimal salvageEstimate = request.estimatedSalvageValue() != null 
                    ? request.estimatedSalvageValue() 
                    : estimateSalvageValue(request.claimPayoutAmount(), request.severity());

            // Reinsurance estimate (for catastrophic claims over $250K)
            BigDecimal reinsuranceEstimate = BigDecimal.ZERO;
            if (request.claimPayoutAmount().compareTo(BigDecimal.valueOf(250000)) > 0) {
                reinsuranceEstimate = request.claimPayoutAmount()
                        .multiply(BigDecimal.valueOf(0.15)) // 15% reinsurance recovery
                        .setScale(2, RoundingMode.HALF_UP);
            }

            // Total estimated recovery
            BigDecimal totalEstimatedRecovery = subrogationEstimate
                    .add(salvageEstimate)
                    .add(reinsuranceEstimate);

            String notes = buildEstimationNotes(subrogationEstimate, salvageEstimate, reinsuranceEstimate, request);

            return new RecoveryEstimationResponse(
                    UUID.randomUUID(),
                    request.claimId(),
                    totalEstimatedRecovery,
                    subrogationEstimate,
                    salvageEstimate,
                    reinsuranceEstimate,
                    notes,
                    LocalDateTime.now()
            );
        });
    }

    private BigDecimal estimateSalvageValue(BigDecimal claimAmount, RecoverySeverity severity) {
        return switch (severity) {
            case LOW -> claimAmount.multiply(BigDecimal.valueOf(0.05));
            case MEDIUM -> claimAmount.multiply(BigDecimal.valueOf(0.10));
            case HIGH -> claimAmount.multiply(BigDecimal.valueOf(0.20));
            case CRITICAL -> claimAmount.multiply(BigDecimal.valueOf(0.02)); // Total loss, minimal salvage
        };
    }

    private String buildEstimationNotes(BigDecimal subrogation, BigDecimal salvage, BigDecimal reinsurance, RecoveryEstimationRequest request) {
        StringBuilder notes = new StringBuilder();
        notes.append("Recovery estimation for claim ").append(request.claimId()).append(". ");
        notes.append("Subrogation: $").append(subrogation).append(" (fault: ").append(request.faultPercentage() * 100).append("%). ");
        notes.append("Salvage: $").append(salvage).append(" (severity: ").append(request.severity()).append("). ");
        if (reinsurance.compareTo(BigDecimal.ZERO) > 0) {
            notes.append("Reinsurance: $").append(reinsurance).append(" (catastrophic claim). ");
        }
        notes.append("Third-party policy limit: ").append(request.thirdPartyPolicyLimit() != null ? "$" + request.thirdPartyPolicyLimit() : "N/A").append(".");
        return notes.toString();
    }
}