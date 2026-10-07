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
        String recoveryPath,
        @NotBlank String taskDescription,
        @NotNull LocalDate dueDate,
        @NotBlank String priority
) {
    public CreateVendorAssignmentRequest(
            UUID vendorId, String assignmentType, UUID claimId, UUID recoveryCaseId,
            String taskDescription, LocalDate dueDate, String priority
    ) {
        this(vendorId, assignmentType, claimId, recoveryCaseId, null, taskDescription, dueDate, priority);
    }
}