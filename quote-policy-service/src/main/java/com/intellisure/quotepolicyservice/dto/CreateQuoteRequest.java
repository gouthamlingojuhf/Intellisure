package com.intellisure.quotepolicyservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateQuoteRequest(

        @NotNull
        UUID customerId,

        @NotBlank
        String businessName,

        @NotBlank
        String businessType,

        @NotNull
        @DecimalMin("0.0")
        BigDecimal annualRevenue,

        @NotNull
        @Min(1)
        Integer employeeCount,

        @NotNull
        @DecimalMin("0.0")
        BigDecimal requestedCoverageAmount
) {
}
