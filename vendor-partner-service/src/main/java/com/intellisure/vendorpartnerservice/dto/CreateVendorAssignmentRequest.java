package com.intellisure.vendorpartnerservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record CreateVendorAssignmentRequest(
        @NotNull UUID vendorId,
        @NotBlank String assignmentType,
        UUID claimId,
        UUID recoveryCaseId,
        @NotBlank String taskDescription,
        @NotNull LocalDate dueDate,
        @NotBlank String priority
) {}