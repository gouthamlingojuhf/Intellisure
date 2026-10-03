package com.intellisure.quotepolicyservice.dto.response;

import com.intellisure.quotepolicyservice.enums.QuoteStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record QuoteResponse(

        UUID quoteId,

        String quoteNumber,

        UUID customerId,

        String productCode,

        String insuranceNeed,

        String businessOperations,

        QuoteStatus status,

        LocalDate requestedEffectiveDate,

        LocalDateTime quoteExpiresAt,

        UUID assignedUnderwriterId,

        UUID riskAssessmentId,

        BigDecimal totalPremium,

        LocalDateTime submittedAt,

        LocalDateTime quotedAt,

        UUID acceptedByUserId,

        LocalDateTime acceptedAt,

        UUID boundByUserId,

        LocalDateTime boundAt,

        String declineReason,

        String withdrawalReason,

        LocalDateTime createdAt,

        LocalDateTime updatedAt,

        List<QuoteCoverageResponse> coverages
) {
}