package com.intellisure.quotepolicyservice.dto.response;

import com.intellisure.quotepolicyservice.enums.PolicyStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CoverageCheckResponse(

        UUID policyId,

        String policyNumber,

        UUID customerId,

        PolicyStatus policyStatus,

        LocalDate requestedDate,

        boolean policyEffectiveOnDate,

        String coverageCode,

        boolean coveragePresent,

        BigDecimal limitAmount,

        BigDecimal deductibleAmount,

        String conditions,

        String exclusions,

        Integer waitingPeriodDays,

        LocalDate coverageEffectiveFrom,

        LocalDate coverageEffectiveTo,

        String message
) {
}