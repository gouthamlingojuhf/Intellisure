package com.intellisure.workflownotificationservice.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChatMessageResponse(
        UUID messageId,
        UUID channelId,
        UUID senderId,
        String senderName,
        String senderRole,
        String content,
        LocalDateTime createdAt
) {
}
