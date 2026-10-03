package com.intellisure.customerpartyservice.dto;

import java.util.Set;
import java.util.UUID;

public record RoleAssignmentResponse(
        UUID userId,
        Set<String> oldRoles,
        Set<String> newRoles,
        String changedBy,
        String changedAt
) {}