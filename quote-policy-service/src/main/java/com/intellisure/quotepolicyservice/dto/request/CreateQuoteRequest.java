package com.intellisure.quotepolicyservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateQuoteRequest(

        @NotNull(message = "Customer ID is required")
        UUID customerId,

        @NotBlank(message = "Product code is required")
        @Size(max = 100, message = "Product code must not exceed 100 characters")
        String productCode,

        @NotBlank(message = "Insurance need is required")
        @Size(
                max = 5000,
                message = "Insurance need must not exceed 5000 characters"
        )
        String insuranceNeed,

        @NotBlank(message = "Business operations are required")
        @Size(
                max = 5000,
                message = "Business operations must not exceed 5000 characters"
        )
        String businessOperations,

        @NotNull(message = "Requested effective date is required")
        @FutureOrPresent(
                message = "Requested effective date cannot be in the past"
        )
        LocalDate requestedEffectiveDate,

        @NotEmpty(message = "At least one coverage is required")
        List<@Valid CreateQuoteCoverageRequest> coverages
) {
}