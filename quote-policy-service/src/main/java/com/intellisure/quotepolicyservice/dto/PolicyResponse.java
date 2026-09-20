package com.intellisure.quotepolicyservice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PolicyResponse(
        UUID policyId,
        UUID quoteId,
        UUID customerId,
        String policyNumber,
        String policyStatus,
        LocalDate effectiveDate,
        LocalDate expiryDate,
        BigDecimal totalPremium,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
