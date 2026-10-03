package com.intellisure.riskunderwritingservice.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CompleteRiskScoreRequest(

        @NotNull(message = "Risk score is required")
        @DecimalMin(value = "0.00")
        @DecimalMax(value = "100.00")
        BigDecimal riskScore,

        @NotBlank(message = "Assessment summary is required")
        @Size(max = 5000)
        String summary
) {
}