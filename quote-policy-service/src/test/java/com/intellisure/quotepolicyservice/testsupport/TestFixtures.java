package com.intellisure.quotepolicyservice.testsupport;

import com.intellisure.quotepolicyservice.client.dto.RiskAssessmentStatusClient;
import com.intellisure.quotepolicyservice.client.dto.RiskBandClient;
import com.intellisure.quotepolicyservice.client.dto.UnderwritingOutcomeClient;
import com.intellisure.quotepolicyservice.client.dto.UnderwritingResultClientResponse;
import com.intellisure.quotepolicyservice.dto.request.CreateQuoteCoverageRequest;
import com.intellisure.quotepolicyservice.dto.request.CreateQuoteRequest;
import com.intellisure.quotepolicyservice.dto.request.OfferQuoteTermsRequest;
import com.intellisure.quotepolicyservice.dto.request.OfferedCoverageRequest;
import com.intellisure.quotepolicyservice.dto.request.RecordUnderwritingDecisionRequest;
import com.intellisure.quotepolicyservice.entity.Policy;
import com.intellisure.quotepolicyservice.entity.PolicyCoverage;
import com.intellisure.quotepolicyservice.entity.Quote;
import com.intellisure.quotepolicyservice.entity.QuoteCoverage;
import com.intellisure.quotepolicyservice.entity.UnderwritingDecision;
import com.intellisure.quotepolicyservice.enums.PolicyStatus;
import com.intellisure.quotepolicyservice.enums.QuoteStatus;
import com.intellisure.quotepolicyservice.enums.UnderwritingDecisionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Central fixture factory shared by the unit and integration tests.
 */
public final class TestFixtures {

    public static final UUID CUSTOMER_ID =
            UUID.fromString("11111111-1111-1111-1111-111111111111");

    public static final UUID USER_ID =
            UUID.fromString("22222222-2222-2222-2222-222222222222");

    public static final UUID UNDERWRITER_ID =
            UUID.fromString("33333333-3333-3333-3333-333333333333");

    public static final UUID OTHER_UNDERWRITER_ID =
            UUID.fromString("44444444-4444-4444-4444-444444444444");

    public static final UUID ADMIN_ID =
            UUID.fromString("55555555-5555-5555-5555-555555555555");

    public static final LocalDateTime NOW =
            LocalDateTime.of(2026, 1, 15, 10, 30);

    private TestFixtures() {
    }

    public static Quote quote(QuoteStatus status) {
        return Quote.builder()
                .quoteId(UUID.randomUUID())
                .quoteNumber(
                        "QTE-2026-"
                                + UUID.randomUUID()
                                        .toString()
                                        .substring(0, 8)
                                        .toUpperCase()
                )
                .customerId(CUSTOMER_ID)
                .productCode("COMMERCIAL-PROPERTY")
                .insuranceNeed("Fire and liability cover")
                .businessOperations("Warehousing")
                .status(status)
                .requestedEffectiveDate(LocalDate.now().plusDays(1))
                .version(0L)
                .subjectivities("[]")
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();
    }

    public static Quote quotedQuote() {
        Quote quote = quote(QuoteStatus.QUOTED);

        quote.setAssignedUnderwriterId(UNDERWRITER_ID);
        quote.setTotalPremium(new BigDecimal("1500.00"));
        quote.setQuoteExpiresAt(LocalDateTime.now().plusDays(7));
        quote.setQuotedAt(NOW);

        return quote;
    }

    public static Quote acceptedQuote() {
        Quote quote = quotedQuote();

        quote.setStatus(QuoteStatus.ACCEPTED);
        quote.setAcceptedByUserId(USER_ID);
        quote.setAcceptedAt(NOW);

        return quote;
    }

    public static Quote inReviewQuote() {
        Quote quote = quote(QuoteStatus.IN_REVIEW);

        quote.setAssignedUnderwriterId(UNDERWRITER_ID);
        quote.setSubmittedAt(NOW);

        return quote;
    }

    public static QuoteCoverage quoteCoverage(String coverageCode) {
        return QuoteCoverage.builder()
                .quoteCoverageId(UUID.randomUUID())
                .quoteId(UUID.randomUUID())
                .coverageCode(coverageCode)
                .coverageName(coverageCode + " cover")
                .requestedLimit(new BigDecimal("100000.00"))
                .requestedDeductible(new BigDecimal("500.00"))
                .waitingPeriodDays(30)
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();
    }

    public static QuoteCoverage offeredCoverage(String coverageCode) {
        QuoteCoverage coverage = quoteCoverage(coverageCode);

        coverage.setOfferedLimit(new BigDecimal("90000.00"));
        coverage.setOfferedDeductible(new BigDecimal("750.00"));
        coverage.setCoveragePremium(new BigDecimal("750.00"));
        coverage.setConditions("  Requires monthly inspection  ");
        coverage.setExclusions("   ");

        return coverage;
    }

    public static UnderwritingDecision decision(
            UnderwritingDecisionType type
    ) {
        return UnderwritingDecision.builder()
                .underwritingDecisionId(UUID.randomUUID())
                .quoteId(UUID.randomUUID())
                .underwriterId(UNDERWRITER_ID)
                .decision(type)
                .decisionReason("Looks acceptable")
                .authorityLevel("SENIOR")
                .conditions("None")
                .decidedAt(NOW)
                .createdAt(NOW)
                .build();
    }

    public static UnderwritingResultClientResponse clientResult(
            UnderwritingOutcomeClient outcome
    ) {
        return new UnderwritingResultClientResponse(
                UUID.fromString("66666666-6666-6666-6666-666666666666"),
                "RA-2026-0001",
                UUID.randomUUID(),
                RiskAssessmentStatusClient.COMPLETED,
                new BigDecimal("42.50"),
                RiskBandClient.MODERATE,
                UUID.fromString("77777777-7777-7777-7777-777777777777"),
                outcome,
                "Automated risk engine rationale",
                "AUTOMATED",
                Boolean.TRUE,
                new BigDecimal("90000.00"),
                new BigDecimal("750.00"),
                new BigDecimal("1450.00"),
                "Subject to survey",
                Boolean.FALSE,
                "RULES-2026.01",
                NOW
        );
    }

    public static CreateQuoteRequest createQuoteRequest() {
        return new CreateQuoteRequest(
                CUSTOMER_ID,
                "COMMERCIAL-PROPERTY",
                "Fire and liability cover",
                "Warehousing",
                LocalDate.now().plusDays(1),
                List.of(
                        new CreateQuoteCoverageRequest(
                                "fire",
                                "Fire cover",
                                new BigDecimal("100000.00"),
                                new BigDecimal("500.00"),
                                30
                        ),
                        new CreateQuoteCoverageRequest(
                                "liability",
                                "Public liability",
                                new BigDecimal("500000.00"),
                                new BigDecimal("1000.00"),
                                null
                        )
                )
        );
    }

    public static OfferQuoteTermsRequest offerTermsRequest(
            String... coverageCodes
    ) {
        return new OfferQuoteTermsRequest(
                LocalDateTime.now().plusDays(7),
                java.util.Arrays.stream(coverageCodes)
                        .map(code -> new OfferedCoverageRequest(
                                code,
                                new BigDecimal("90000.00"),
                                new BigDecimal("750.00"),
                                new BigDecimal("750.00"),
                                "  Survey required  ",
                                "  Flood excluded  ",
                                30
                        ))
                        .toList()
        );
    }

    public static RecordUnderwritingDecisionRequest decisionRequest(
            UnderwritingDecisionType type
    ) {
        return new RecordUnderwritingDecisionRequest(
                type,
                "  Inspected the risk  ",
                "  SENIOR  ",
                "  Monthly reporting  "
        );
    }

    public static Policy policy(PolicyStatus status) {
        return Policy.builder()
                .policyId(UUID.randomUUID())
                .policyNumber(
                        "POL-2026-"
                                + UUID.randomUUID()
                                        .toString()
                                        .substring(0, 8)
                                        .toUpperCase()
                )
                .quoteId(UUID.randomUUID())
                .customerId(CUSTOMER_ID)
                .productCode("COMMERCIAL-PROPERTY")
                .status(status)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(1).minusDays(1))
                .totalPremium(new BigDecimal("1500.00"))
                .issuedByUserId(UNDERWRITER_ID)
                .boundAt(NOW)
                .issuedAt(NOW)
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();
    }

    public static PolicyCoverage policyCoverage(
            String code,
            LocalDate from,
            LocalDate to
    ) {
        return PolicyCoverage.builder()
                .policyCoverageId(UUID.randomUUID())
                .policyId(UUID.randomUUID())
                .coverageCode(code)
                .coverageName(code + " cover")
                .limitAmount(new BigDecimal("90000.00"))
                .deductibleAmount(new BigDecimal("750.00"))
                .coveragePremium(new BigDecimal("750.00"))
                .conditions("Survey required")
                .exclusions("Flood")
                .waitingPeriodDays(30)
                .effectiveFrom(from)
                .effectiveTo(to)
                .createdAt(NOW)
                .build();
    }
}
