package com.intellisure.workflownotificationservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateNotificationRequest(
        @NotNull UUID userId,
        @NotBlank String type,
        @NotBlank String title,
        @NotBlank String message,
        String referenceType,
        UUID referenceId,
        String channel
) {}