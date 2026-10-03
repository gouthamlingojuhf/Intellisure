package com.intellisure.customerpartyservice.dto;

import java.util.UUID;

public record AdminUserResponse(
        UUID userId,
        UUID customerId,
        String email,
        String displayName,
        String role,
        String accountStatus
) {}