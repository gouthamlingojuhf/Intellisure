package com.intellisure.recoveryservice.dto;

import com.intellisure.recoveryservice.entity.RecoverySupportStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateSupportRequestStatusRequest(
        @NotNull RecoverySupportStatus status
) {}