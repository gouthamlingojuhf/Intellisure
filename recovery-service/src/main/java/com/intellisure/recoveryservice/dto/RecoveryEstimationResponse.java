package com.intellisure.recoveryservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record RecoveryEstimationResponse(
        UUID estimationId,
        UUID claimId,
        BigDecimal estimatedRecoveryAmount,
        BigDecimal subrogationEstimate,
        BigDecimal salvageEstimate,
        BigDecimal reinsuranceEstimate,
        String estimationNotes,
        LocalDateTime createdAt
) {}