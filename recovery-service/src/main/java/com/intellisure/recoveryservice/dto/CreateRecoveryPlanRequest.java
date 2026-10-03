package com.intellisure.recoveryservice.dto;

import com.intellisure.recoveryservice.entity.RecoveryPlanStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record CreateRecoveryPlanRequest(
        @NotNull UUID recoveryCaseId,
        @NotBlank String planSummary,
        @NotNull List<String> priorityActions,
        @NotNull List<UUID> vendorAssignmentIds,
        @NotNull List<String> temporaryResourceNeeds,
        @NotBlank String targetMilestones,
        RecoveryPlanStatus status
) {}