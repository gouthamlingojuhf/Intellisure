package com.intellisure.recoveryservice.dto;

import com.intellisure.recoveryservice.entity.SupportPriority;
import com.intellisure.recoveryservice.entity.SupportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record CreateRecoverySupportRequest(
        @NotNull UUID recoveryCaseId,
        @NotNull SupportType supportType,
        @NotBlank String description,
        @NotNull SupportPriority priority,
        @NotNull LocalDate requiredByDate,
        @NotBlank String location,
        UUID vendorAssignmentId
) {}