package com.intellisure.quotepolicyservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record QuoteResponse(

        UUID quoteId,

        UUID customerId,

        String businessName,

        String businessType,

        BigDecimal annualRevenue,

        Integer employeeCount,

        BigDecimal requestedCoverageAmount,

        BigDecimal estimatedPremium,

        String quoteStatus,

        LocalDateTime validUntil,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}
