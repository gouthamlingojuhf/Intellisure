package com.intellisure.quotepolicyservice.dto.response;

import com.intellisure.quotepolicyservice.enums.QuoteStatus;
import com.intellisure.quotepolicyservice.enums.UnderwritingDecisionType;

import java.math.BigDecimal;
import java.util.UUID;

public record ImportedUnderwritingResultResponse(

        UUID quoteId,

        String quoteNumber,

        QuoteStatus quoteStatus,

        UUID riskAssessmentId,

        UUID sourceDecisionId,

        UnderwritingDecisionType importedDecision,

        BigDecimal riskScore,

        String riskBand,

        BigDecimal approvedLimit,

        BigDecimal approvedDeductible,

        BigDecimal indicatedPremium,

        Boolean subjectivitiesOutstanding,

        String ruleVersionReference,

        boolean alreadyImported
) {
}