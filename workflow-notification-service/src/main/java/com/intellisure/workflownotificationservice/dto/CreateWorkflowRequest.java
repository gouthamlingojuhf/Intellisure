package com.intellisure.workflownotificationservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateWorkflowRequest(
        @NotBlank String workflowType,
        @NotNull UUID referenceId,
        @NotBlank String referenceType,
        @NotBlank String initiatedBy,
        String currentStep
) {}