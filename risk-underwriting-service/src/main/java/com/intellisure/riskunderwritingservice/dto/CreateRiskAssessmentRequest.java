package com.intellisure.riskunderwritingservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateRiskAssessmentRequest(
        UUID quoteId,
        UUID policyId,
        @NotNull UUID customerId,
        @NotBlank String assessmentType,
        String location,
        String businessOperations,
        BigDecimal riskScore,
        String summary
) {}
