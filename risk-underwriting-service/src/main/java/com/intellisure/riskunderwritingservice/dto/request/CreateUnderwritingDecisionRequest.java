package com.intellisure.riskunderwritingservice.dto.request;

import com.intellisure.riskunderwritingservice.enums.UnderwritingOutcome;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateUnderwritingDecisionRequest(

        @NotNull(message = "Underwriting outcome is required")
        UnderwritingOutcome outcome,

        @NotBlank(message = "Decision rationale is required")
        @Size(
                max = 5000,
                message = "Decision rationale must not exceed 5000 characters"
        )
        String decisionRationale,

        @NotBlank(message = "Authority level is required")
        @Size(
                max = 100,
                message = "Authority level must not exceed 100 characters"
        )
        String authorityLevel,

        @NotNull(message = "Within-authority value is required")
        Boolean withinAuthority,

        @DecimalMin(
                value = "0.01",
                message = "Approved limit must be greater than zero"
        )
        BigDecimal approvedLimit,

        @DecimalMin(
                value = "0.00",
                message = "Approved deductible cannot be negative"
        )
        BigDecimal approvedDeductible,

        @DecimalMin(
                value = "0.01",
                message = "Indicated premium must be greater than zero"
        )
        BigDecimal indicatedPremium,

        @Size(
                max = 5000,
                message = "Conditions must not exceed 5000 characters"
        )
        String conditions,

        @Size(
                max = 255,
                message = "Rule version reference must not exceed 255 characters"
        )
        String ruleVersionReference
) {
}