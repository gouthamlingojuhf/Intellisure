package com.intellisure.workflownotificationservice.dto;

import com.intellisure.workflownotificationservice.entity.ChatChannelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateChannelRequest(
        @NotBlank String name,
        @NotNull ChatChannelType channelType,
        String entityType,
        UUID entityId,
        List<UUID> participantUserIds
) {
}
