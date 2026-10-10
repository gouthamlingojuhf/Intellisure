package com.intellisure.workflownotificationservice.dto;

import com.intellisure.workflownotificationservice.entity.ChatChannelType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ChatChannelResponse(
        UUID channelId,
        String name,
        ChatChannelType channelType,
        String entityType,
        UUID entityId,
        UUID createdBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ChatParticipantResponse> participants,
        ChatMessageResponse lastMessage,
        long unreadCount
) {
}
