package com.intellisure.quotepolicyservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateQuoteCoverageRequest(

        @NotBlank(message = "Coverage code is required")
        @Size(max = 100, message = "Coverage code must not exceed 100 characters")
        String coverageCode,

        @NotBlank(message = "Coverage name is required")
        @Size(max = 255, message = "Coverage name must not exceed 255 characters")
        String coverageName,

        @NotNull(message = "Requested limit is required")
        @DecimalMin(
                value = "0.01",
                message = "Requested limit must be greater than zero"
        )
        BigDecimal requestedLimit,

        @NotNull(message = "Requested deductible is required")
        @PositiveOrZero(
                message = "Requested deductible cannot be negative"
        )
        BigDecimal requestedDeductible,

        @PositiveOrZero(
                message = "Waiting period cannot be negative"
        )
        Integer waitingPeriodDays
) {
}
