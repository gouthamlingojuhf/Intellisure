package com.intellisure.workflownotificationservice.dto;

import java.util.UUID;

public record ChatContactResponse(
        UUID userId,
        String displayName,
        String email,
        String role,
        String contextType,
        UUID contextId,
        String contextReference
) {
}
