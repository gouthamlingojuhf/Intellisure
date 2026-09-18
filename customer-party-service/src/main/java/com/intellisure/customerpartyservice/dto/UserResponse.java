package com.intellisure.customerpartyservice.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID userId,
        String email,
        String role,
        String accountStatus,
        String displayName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}
