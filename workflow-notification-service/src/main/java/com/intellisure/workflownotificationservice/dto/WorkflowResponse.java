package com.intellisure.workflownotificationservice.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record WorkflowResponse(
        UUID workflowId,
        String workflowType,
        UUID referenceId,
        String referenceType,
        String status,
        String currentStep,
        String initiatedBy,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}