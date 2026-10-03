package com.intellisure.customerpartyservice.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CustomerResponse(
        UUID customerId,
        UUID userId,
        String businessName,
        String ownerName,
        String phone,
        String businessType,
        String address,
        String city,
        String state,
        String country,
        String postalCode,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
