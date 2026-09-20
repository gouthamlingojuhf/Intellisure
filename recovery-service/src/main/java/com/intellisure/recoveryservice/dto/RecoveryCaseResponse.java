package com.intellisure.recoveryservice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record RecoveryCaseResponse(
        UUID recoveryCaseId,
        UUID claimId,
        UUID customerId,
        String severity,
        String status,
        String recoveryObjective,
        LocalDate targetRestoreDate,
        BigDecimal currentRestorePercent,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
