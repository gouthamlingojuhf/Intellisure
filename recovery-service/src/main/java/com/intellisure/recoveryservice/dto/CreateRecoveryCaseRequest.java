package com.intellisure.recoveryservice.dto;

import com.intellisure.recoveryservice.entity.RecoverySeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record CreateRecoveryCaseRequest(
        @NotNull UUID claimId,
        @NotNull UUID customerId,
        @NotNull RecoverySeverity severity,
        @NotBlank String recoveryObjective,
        LocalDate targetRestoreDate,
        UUID ownerId
) {}