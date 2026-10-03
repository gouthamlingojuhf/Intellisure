package com.intellisure.quotepolicyservice.mapper;

import com.intellisure.quotepolicyservice.dto.response.PolicyCoverageResponse;
import com.intellisure.quotepolicyservice.dto.response.PolicyResponse;
import com.intellisure.quotepolicyservice.dto.response.QuoteCoverageResponse;
import com.intellisure.quotepolicyservice.dto.response.QuoteResponse;
import com.intellisure.quotepolicyservice.entity.Policy;
import com.intellisure.quotepolicyservice.entity.PolicyCoverage;
import com.intellisure.quotepolicyservice.entity.Quote;
import com.intellisure.quotepolicyservice.entity.QuoteCoverage;
import com.intellisure.quotepolicyservice.enums.PolicyStatus;
import com.intellisure.quotepolicyservice.enums.QuoteStatus;
import com.intellisure.quotepolicyservice.testsupport.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Entity mappers")
class MapperTest {

    private final QuoteMapper quoteMapper = new QuoteMapper();
    private final PolicyMapper policyMapper = new PolicyMapper();

    @Test
    @DisplayName("QuoteMapper maps every quote field")
    void mapsQuote() {
        Quote quote = TestFixtures.quotedQuote();
        quote.setRiskAssessmentId(UUID.randomUUID());
        quote.setWithdrawalReason("withdrawn by broker");
        quote.setBoundByUserId(TestFixtures.UNDERWRITER_ID);
        quote.setBoundAt(TestFixtures.NOW);

        QuoteResponse response = quoteMapper.toResponse(quote, List.of());

        assertEquals(quote.getQuoteId(), response.quoteId());
        assertEquals(quote.getQuoteNumber(), response.quoteNumber());
        assertEquals(quote.getCustomerId(), response.customerId());
        assertEquals(quote.getProductCode(), response.productCode());
        assertEquals(
                quote.getInsuranceNeed(),
                response.insuranceNeed()
        );
        assertEquals(
                quote.getBusinessOperations(),
                response.businessOperations()
        );
        assertEquals(QuoteStatus.QUOTED, response.status());
        assertEquals(
                quote.getRequestedEffectiveDate(),
                response.requestedEffectiveDate()
        );
        assertEquals(
                quote.getQuoteExpiresAt(),
                response.quoteExpiresAt()
        );
        assertEquals(
                quote.getAssignedUnderwriterId(),
                response.assignedUnderwriterId()
        );
        assertEquals(
                quote.getRiskAssessmentId(),
                response.riskAssessmentId()
        );
        assertEquals(
                quote.getTotalPremium(),
                response.totalPremium()
        );
        assertEquals(quote.getSubmittedAt(), response.submittedAt());
        assertEquals(quote.getQuotedAt(), response.quotedAt());
        assertEquals(
                quote.getBoundByUserId(),
                response.boundByUserId()
        );
        assertEquals(quote.getBoundAt(), response.boundAt());
        assertEquals(
                "withdrawn by broker",
                response.withdrawalReason()
        );
        assertTrue(response.coverages().isEmpty());
    }

    @Test
    @DisplayName("QuoteMapper maps coverage fields including the offered values")
    void mapsQuoteCoverage() {
        QuoteCoverage coverage = TestFixtures.offeredCoverage("FIRE");

        QuoteCoverageResponse response = quoteMapper.toCoverageResponse(
                coverage
        );

        assertEquals(
                coverage.getQuoteCoverageId(),
                response.quoteCoverageId()
        );
        assertEquals(coverage.getCoverageCode(), response.coverageCode());
        assertEquals(coverage.getCoverageName(), response.coverageName());
        assertEquals(
                coverage.getRequestedLimit(),
                response.requestedLimit()
        );
        assertEquals(coverage.getOfferedLimit(), response.offeredLimit());
        assertEquals(
                coverage.getRequestedDeductible(),
                response.requestedDeductible()
        );
        assertEquals(
                coverage.getOfferedDeductible(),
                response.offeredDeductible()
        );
        assertEquals(
                coverage.getCoveragePremium(),
                response.coveragePremium()
        );
        assertEquals(coverage.getCreatedAt(), response.createdAt());
        assertEquals(coverage.getUpdatedAt(), response.updatedAt());
    }

    @Test
    @DisplayName("QuoteMapper keeps unset offered values as null")
    void mapsQuoteCoverageWithoutOfferedTerms() {
        QuoteCoverage coverage = TestFixtures.quoteCoverage("FIRE");

        QuoteCoverageResponse response = quoteMapper.toCoverageResponse(
                coverage
        );

        assertNull(response.offeredLimit());
        assertNull(response.offeredDeductible());
        assertNull(response.coveragePremium());
        assertNull(response.conditions());
        assertNull(response.exclusions());
    }

    @Test
    @DisplayName("QuoteMapper preserves the coverage order")
    void preservesCoverageOrder() {
        Quote quote = TestFixtures.quote(QuoteStatus.DRAFT);
        QuoteCoverage first = TestFixtures.quoteCoverage("FIRE");
        QuoteCoverage second = TestFixtures.quoteCoverage("LIABILITY");

        QuoteResponse response = quoteMapper.toResponse(
                quote,
                List.of(first, second)
        );

        assertEquals(
                "FIRE",
                response.coverages().get(0).coverageCode()
        );
        assertEquals(
                "LIABILITY",
                response.coverages().get(1).coverageCode()
        );
    }

    @Test
    @DisplayName("PolicyMapper maps every policy field")
    void mapsPolicy() {
        Policy policy = TestFixtures.policy(PolicyStatus.IN_FORCE);
        policy.setExpiredAt(TestFixtures.NOW);

        PolicyResponse response = policyMapper.toResponse(policy, List.of());

        assertEquals(policy.getPolicyId(), response.policyId());
        assertEquals(policy.getPolicyNumber(), response.policyNumber());
        assertEquals(policy.getQuoteId(), response.quoteId());
        assertEquals(policy.getCustomerId(), response.customerId());
        assertEquals(policy.getProductCode(), response.productCode());
        assertEquals(PolicyStatus.IN_FORCE, response.status());
        assertEquals(policy.getStartDate(), response.startDate());
        assertEquals(policy.getEndDate(), response.endDate());
        assertEquals(
                new BigDecimal("1500.00"),
                response.totalPremium()
        );
        assertEquals(
                policy.getIssuedByUserId(),
                response.issuedByUserId()
        );
        assertEquals(policy.getBoundAt(), response.boundAt());
        assertEquals(policy.getIssuedAt(), response.issuedAt());
        assertEquals(policy.getExpiredAt(), response.expiredAt());
        assertEquals(policy.getCreatedAt(), response.createdAt());
        assertEquals(policy.getUpdatedAt(), response.updatedAt());
        assertTrue(response.coverages().isEmpty());
    }

    @Test
    @DisplayName("PolicyMapper maps coverage fields")
    void mapsPolicyCoverage() {
        PolicyCoverage coverage = TestFixtures.policyCoverage(
                "FIRE",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31)
        );

        PolicyCoverageResponse response = policyMapper.toCoverageResponse(
                coverage
        );

        assertEquals(
                coverage.getPolicyCoverageId(),
                response.policyCoverageId()
        );
        assertEquals(coverage.getCoverageCode(), response.coverageCode());
        assertEquals(coverage.getCoverageName(), response.coverageName());
        assertEquals(coverage.getLimitAmount(), response.limitAmount());
        assertEquals(
                coverage.getDeductibleAmount(),
                response.deductibleAmount()
        );
        assertEquals(
                coverage.getCoveragePremium(),
                response.coveragePremium()
        );
        assertEquals(coverage.getConditions(), response.conditions());
        assertEquals(coverage.getExclusions(), response.exclusions());
        assertEquals(
                coverage.getWaitingPeriodDays(),
                response.waitingPeriodDays()
        );
        assertEquals(
                coverage.getEffectiveFrom(),
                response.effectiveFrom()
        );
        assertEquals(
                coverage.getEffectiveTo(),
                response.effectiveTo()
        );
        assertEquals(coverage.getCreatedAt(), response.createdAt());
    }

    @Test
    @DisplayName("PolicyMapper maps a policy with several coverages")
    void mapsPolicyWithCoverages() {
        Policy policy = TestFixtures.policy(PolicyStatus.IN_FORCE);
        PolicyCoverage first = TestFixtures.policyCoverage(
                "FIRE",
                policy.getStartDate(),
                policy.getEndDate()
        );
        PolicyCoverage second = TestFixtures.policyCoverage(
                "LIABILITY",
                policy.getStartDate(),
                policy.getEndDate()
        );

        PolicyResponse response = policyMapper.toResponse(
                policy,
                List.of(first, second)
        );

        assertEquals(2, response.coverages().size());
        assertEquals(
                "FIRE",
                response.coverages().get(0).coverageCode()
        );
        assertEquals(
                "LIABILITY",
                response.coverages().get(1).coverageCode()
        );
    }
}
