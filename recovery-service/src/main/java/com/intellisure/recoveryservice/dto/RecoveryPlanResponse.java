package com.intellisure.recoveryservice.dto;

import com.intellisure.recoveryservice.entity.RecoveryPlanStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record RecoveryPlanResponse(
        UUID recoveryPlanId,
        UUID recoveryCaseId,
        String planSummary,
        List<String> priorityActions,
        List<UUID> vendorAssignmentIds,
        List<String> temporaryResourceNeeds,
        String targetMilestones,
        RecoveryPlanStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}