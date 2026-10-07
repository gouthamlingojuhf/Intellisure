package com.intellisure.workflownotificationservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateWorkflowTaskRequest(
        @NotNull UUID workflowId,
        @NotBlank String taskType,
        UUID assigneeUserId,
        LocalDateTime dueAt,
        String completionNote
) {}
