package com.intellisure.workflownotificationservice.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID notificationId,
        UUID recipientId,
        String title,
        String message,
        String status,
        String channel,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {}
