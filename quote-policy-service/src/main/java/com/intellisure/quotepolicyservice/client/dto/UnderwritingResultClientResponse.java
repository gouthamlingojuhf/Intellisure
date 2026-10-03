package com.intellisure.quotepolicyservice.client.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record UnderwritingResultClientResponse(

        UUID assessmentId,

        String assessmentNumber,

        UUID quoteId,

        RiskAssessmentStatusClient assessmentStatus,

        BigDecimal riskScore,

        RiskBandClient riskBand,

        UUID decisionId,

        UnderwritingOutcomeClient outcome,

        String decisionRationale,

        String authorityLevel,

        Boolean withinAuthority,

        BigDecimal approvedLimit,

        BigDecimal approvedDeductible,

        BigDecimal indicatedPremium,

        String conditions,

        Boolean subjectivitiesOutstanding,

        String ruleVersionReference,

        LocalDateTime decidedAt
) {
}