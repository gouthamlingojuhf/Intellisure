package com.intellisure.quotepolicyservice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PolicyCoverageResponse(

        UUID policyCoverageId,

        String coverageCode,

        String coverageName,

        BigDecimal limitAmount,

        BigDecimal deductibleAmount,

        BigDecimal coveragePremium,

        String conditions,

        String exclusions,

        Integer waitingPeriodDays,

        LocalDate effectiveFrom,

        LocalDate effectiveTo,

        LocalDateTime createdAt
) {
}