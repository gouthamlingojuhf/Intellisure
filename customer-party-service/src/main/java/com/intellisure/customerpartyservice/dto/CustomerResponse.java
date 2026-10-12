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
        java.math.BigDecimal annualTurnover,
        Integer employeeCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public CustomerResponse(
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
    ) {
        this(customerId, userId, businessName, ownerName, phone, businessType, address, city, state, country, postalCode, null, null, createdAt, updatedAt);
    }
}
