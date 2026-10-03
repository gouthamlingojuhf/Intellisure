package com.intellisure.riskunderwritingservice.dto.response;

import com.intellisure.riskunderwritingservice.enums.RiskAssessmentStatus;
import com.intellisure.riskunderwritingservice.enums.RiskBand;
import com.intellisure.riskunderwritingservice.enums.UnderwritingOutcome;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record UnderwritingResultResponse(

        UUID assessmentId,

        String assessmentNumber,

        UUID quoteId,

        RiskAssessmentStatus assessmentStatus,

        BigDecimal riskScore,

        RiskBand riskBand,

        UUID decisionId,

        UnderwritingOutcome outcome,

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