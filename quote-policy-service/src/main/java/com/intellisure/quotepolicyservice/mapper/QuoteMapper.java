package com.intellisure.quotepolicyservice.mapper;

import com.intellisure.quotepolicyservice.dto.response.QuoteCoverageResponse;
import com.intellisure.quotepolicyservice.dto.response.QuoteResponse;
import com.intellisure.quotepolicyservice.entity.Quote;
import com.intellisure.quotepolicyservice.entity.QuoteCoverage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class QuoteMapper {

    public QuoteCoverageResponse toCoverageResponse(
            QuoteCoverage coverage
    ) {
        return new QuoteCoverageResponse(
                coverage.getQuoteCoverageId(),
                coverage.getCoverageCode(),
                coverage.getCoverageName(),
                coverage.getRequestedLimit(),
                coverage.getOfferedLimit(),
                coverage.getRequestedDeductible(),
                coverage.getOfferedDeductible(),
                coverage.getCoveragePremium(),
                coverage.getConditions(),
                coverage.getExclusions(),
                coverage.getWaitingPeriodDays(),
                coverage.getCreatedAt(),
                coverage.getUpdatedAt()
        );
    }

    /**
     * Maps a Quote entity + its coverages to a QuoteResponse.
     *
     * @param boundPolicyId the policy UUID if this quote has been bound,
     *                      or {@code null} if no policy exists yet.
     */
    public QuoteResponse toResponse(
            Quote quote,
            List<QuoteCoverage> coverages,
            UUID boundPolicyId
    ) {
        List<QuoteCoverageResponse> coverageResponses =
                coverages.stream()
                        .map(this::toCoverageResponse)
                        .toList();

        return new QuoteResponse(
                quote.getQuoteId(),
                quote.getQuoteNumber(),
                quote.getCustomerId(),
                quote.getProductCode(),
                quote.getInsuranceNeed(),
                quote.getBusinessOperations(),
                quote.getStatus(),
                quote.getRequestedEffectiveDate(),
                quote.getQuoteExpiresAt(),
                quote.getAssignedUnderwriterId(),
                quote.getRiskAssessmentId(),
                quote.getTotalPremium(),
                quote.getSubmittedAt(),
                quote.getQuotedAt(),
                quote.getAcceptedByUserId(),
                quote.getAcceptedAt(),
                quote.getBoundByUserId(),
                quote.getBoundAt(),
                quote.getDeclineReason(),
                quote.getWithdrawalReason(),
                quote.getCreatedAt(),
                quote.getUpdatedAt(),
                coverageResponses,
                boundPolicyId
        );
    }

    /** Convenience overload for paths where no policy lookup is needed (e.g. draft creation). */
    public QuoteResponse toResponse(
            Quote quote,
            List<QuoteCoverage> coverages
    ) {
        return toResponse(quote, coverages, null);
    }
}