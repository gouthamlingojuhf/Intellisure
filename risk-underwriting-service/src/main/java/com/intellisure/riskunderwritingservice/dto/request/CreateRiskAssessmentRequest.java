package com.intellisure.riskunderwritingservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateRiskAssessmentRequest(

        @NotNull(message = "Quote ID is required")
        UUID quoteId,

        UUID policyId,

        @NotNull(message = "Customer ID is required")
        UUID customerId,

        @NotBlank(message = "Assessment type is required")
        @Size(max = 100)
        String assessmentType,

        @NotNull(message = "Assessment date is required")
        LocalDate assessmentDate,

        @NotBlank(message = "Location is required")
        @Size(max = 500)
        String location,

        @NotBlank(message = "Business operations are required")
        @Size(max = 5000)
        String businessOperations,

        @PositiveOrZero(message = "Annual revenue cannot be negative")
        BigDecimal annualRevenue,

        @PositiveOrZero(message = "Annual payroll cannot be negative")
        BigDecimal annualPayroll,

        @PositiveOrZero(message = "Employee count cannot be negative")
        Integer employeeCount,

        @PositiveOrZero(message = "Asset value cannot be negative")
        BigDecimal assetValue,

        @PositiveOrZero(message = "Prior claim count cannot be negative")
        Integer priorClaimCount,

        @PositiveOrZero(message = "Prior loss amount cannot be negative")
        BigDecimal priorLossAmount,

        @NotNull(message = "Created-by user ID is required")
        UUID createdBy
) {
}