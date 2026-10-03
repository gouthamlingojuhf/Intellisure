package com.intellisure.workflownotificationservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SendNotificationRequest(
        @NotNull UUID recipientId,
        @NotBlank String title,
        @NotBlank String message,
        @NotBlank String channel
) {}