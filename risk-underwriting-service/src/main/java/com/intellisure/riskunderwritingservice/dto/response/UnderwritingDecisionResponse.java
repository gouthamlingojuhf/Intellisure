package com.intellisure.riskunderwritingservice.dto.response;

import com.intellisure.riskunderwritingservice.enums.UnderwritingOutcome;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record UnderwritingDecisionResponse(

        UUID decisionId,

        UUID assessmentId,

        UUID quoteId,

        UUID underwriterId,

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

        LocalDateTime decidedAt,

        LocalDateTime createdAt
) {
}