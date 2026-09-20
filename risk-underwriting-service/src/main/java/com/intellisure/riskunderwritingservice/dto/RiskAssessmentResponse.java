package com.intellisure.riskunderwritingservice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record RiskAssessmentResponse(
        UUID assessmentId,
        UUID quoteId,
        UUID policyId,
        UUID customerId,
        String assessmentType,
        String status,
        LocalDate assessmentDate,
        String location,
        String businessOperations,
        BigDecimal riskScore,
        String summary,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
