package com.intellisure.recoveryservice.dto;

import com.intellisure.recoveryservice.entity.RecoveryCaseStatus;
import com.intellisure.recoveryservice.entity.RecoveryPath;
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
        RecoveryPath recoveryPath,
        String recoveryObjective,
        String recoveryNotes,
        LocalDate targetRestoreDate,
        LocalDate actualRestorationDate,
        BigDecimal currentRestorePercent,
        UUID ownerId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}