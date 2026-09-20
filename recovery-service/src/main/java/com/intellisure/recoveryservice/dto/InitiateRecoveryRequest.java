package com.intellisure.recoveryservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record InitiateRecoveryRequest(
        @NotNull UUID claimId,
        @NotNull UUID customerId,
        @NotBlank String severity,
        @NotBlank String recoveryObjective
) {}
