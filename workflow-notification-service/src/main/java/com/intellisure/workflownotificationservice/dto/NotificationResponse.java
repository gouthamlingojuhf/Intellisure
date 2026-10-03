package com.intellisure.workflownotificationservice.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID notificationId,
        UUID userId,
        String type,
        String title,
        String message,
        String referenceType,
        UUID referenceId,
        boolean read,
        String channel,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {}