package com.intellisure.riskunderwritingservice.dto.response;

import com.intellisure.riskunderwritingservice.enums.RiskAssessmentStatus;
import com.intellisure.riskunderwritingservice.enums.RiskBand;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record RiskAssessmentResponse(

        UUID assessmentId,

        String assessmentNumber,

        UUID quoteId,

        UUID policyId,

        UUID customerId,

        String assessmentType,

        RiskAssessmentStatus status,

        LocalDate assessmentDate,

        String location,

        String businessOperations,

        BigDecimal annualRevenue,

        BigDecimal annualPayroll,

        Integer employeeCount,

        BigDecimal assetValue,

        Integer priorClaimCount,

        BigDecimal priorLossAmount,

        BigDecimal riskScore,

        RiskBand riskBand,

        String summary,

        UUID createdBy,

        UUID assignedUnderwriterId,

        UUID assignedRiskEngineerId,

        LocalDateTime submittedAt,

        LocalDateTime completedAt,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}