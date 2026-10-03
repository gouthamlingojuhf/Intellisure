package com.intellisure.quotepolicyservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record OfferedCoverageRequest(

        @NotBlank(message = "Coverage code is required")
        @Size(
                max = 100,
                message = "Coverage code must not exceed 100 characters"
        )
        String coverageCode,

        @NotNull(message = "Offered limit is required")
        @DecimalMin(
                value = "0.01",
                message = "Offered limit must be greater than zero"
        )
        BigDecimal offeredLimit,

        @NotNull(message = "Offered deductible is required")
        @PositiveOrZero(
                message = "Offered deductible cannot be negative"
        )
        BigDecimal offeredDeductible,

        @NotNull(message = "Coverage premium is required")
        @DecimalMin(
                value = "0.01",
                message = "Coverage premium must be greater than zero"
        )
        BigDecimal coveragePremium,

        @Size(
                max = 5000,
                message = "Conditions must not exceed 5000 characters"
        )
        String conditions,

        @Size(
                max = 5000,
                message = "Exclusions must not exceed 5000 characters"
        )
        String exclusions,

        @PositiveOrZero(
                message = "Waiting period cannot be negative"
        )
        Integer waitingPeriodDays
) {
}
