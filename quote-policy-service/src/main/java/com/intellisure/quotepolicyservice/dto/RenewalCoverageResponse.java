package com.intellisure.quotepolicyservice.dto;

import java.math.BigDecimal;

public record RenewalCoverageResponse(
        String coverageCode,
        String coverageName,
        BigDecimal limitAmount,
        BigDecimal deductibleAmount,
        BigDecimal coveragePremium,
        String conditions,
        String exclusions,
        Integer waitingPeriodDays
) {}