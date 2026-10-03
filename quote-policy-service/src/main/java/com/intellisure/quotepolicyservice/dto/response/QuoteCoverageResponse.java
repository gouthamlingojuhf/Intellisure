package com.intellisure.quotepolicyservice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record QuoteCoverageResponse(

        UUID quoteCoverageId,

        String coverageCode,

        String coverageName,

        BigDecimal requestedLimit,

        BigDecimal offeredLimit,

        BigDecimal requestedDeductible,

        BigDecimal offeredDeductible,

        BigDecimal coveragePremium,

        String conditions,

        String exclusions,

        Integer waitingPeriodDays,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}