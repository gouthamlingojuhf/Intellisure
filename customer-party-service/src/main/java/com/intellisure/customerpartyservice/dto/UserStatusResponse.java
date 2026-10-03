package com.intellisure.customerpartyservice.dto;

import java.util.UUID;

public record UserStatusResponse(
        UUID userId,
        String oldStatus,
        String newStatus,
        String changedBy,
        String changedAt
) {}