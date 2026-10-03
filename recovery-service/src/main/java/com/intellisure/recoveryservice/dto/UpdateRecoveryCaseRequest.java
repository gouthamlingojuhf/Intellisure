package com.intellisure.recoveryservice.dto;

import com.intellisure.recoveryservice.entity.RecoverySeverity;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateRecoveryCaseRequest(
        RecoverySeverity severity,
        String recoveryObjective,
        LocalDate targetRestoreDate,
        BigDecimal currentRestorePercent,
        UUID ownerId
) {}