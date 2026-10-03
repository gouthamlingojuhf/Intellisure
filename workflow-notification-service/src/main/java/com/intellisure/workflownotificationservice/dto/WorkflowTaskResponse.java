package com.intellisure.workflownotificationservice.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record WorkflowTaskResponse(
        UUID taskId,
        UUID workflowId,
        String taskType,
        UUID assigneeUserId,
        String status,
        LocalDateTime dueAt,
        String outcome,
        String completionNote,
        LocalDateTime completedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}