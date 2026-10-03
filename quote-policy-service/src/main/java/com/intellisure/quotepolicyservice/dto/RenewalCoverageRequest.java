package com.intellisure.quotepolicyservice.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record RenewalCoverageRequest(
        @NotBlank String coverageCode,
        String coverageName,
        BigDecimal limitAmount,
        BigDecimal deductibleAmount,
        BigDecimal coveragePremium,
        String conditions,
        String exclusions,
        Integer waitingPeriodDays
) {}