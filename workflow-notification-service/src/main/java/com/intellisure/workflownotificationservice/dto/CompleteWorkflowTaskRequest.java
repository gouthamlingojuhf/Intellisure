package com.intellisure.workflownotificationservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CompleteWorkflowTaskRequest(
        @NotNull UUID taskId,
        @NotNull UUID workflowId,
        @NotBlank String outcome,
        String completionNote
) {}