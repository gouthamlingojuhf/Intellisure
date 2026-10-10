package com.intellisure.workflownotificationservice.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChatParticipantResponse(
        UUID participantId,
        UUID userId,
        String role,
        String displayName,
        LocalDateTime joinedAt
) {
}
