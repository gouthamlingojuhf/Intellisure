package com.intellisure.recoveryservice.dto;

import com.intellisure.recoveryservice.entity.RecoveryCaseStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateRecoveryStatusRequest(
        @NotNull RecoveryCaseStatus status
) {}