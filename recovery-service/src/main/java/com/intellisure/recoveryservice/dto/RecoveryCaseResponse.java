package com.intellisure.recoveryservice.dto;

import com.intellisure.recoveryservice.entity.RecoveryCaseStatus;
import com.intellisure.recoveryservice.entity.RecoverySeverity;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record RecoveryCaseResponse(
        UUID recoveryCaseId,
        UUID claimId,
        UUID customerId,
        RecoverySeverity severity,
        RecoveryCaseStatus status,
        String recoveryObjective,
        LocalDate targetRestoreDate,
        BigDecimal currentRestorePercent,
        UUID ownerId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}