package com.intellisure.quotepolicyservice.integration;

import com.intellisure.quotepolicyservice.entity.Policy;
import com.intellisure.quotepolicyservice.entity.PolicyCoverage;
import com.intellisure.quotepolicyservice.entity.Quote;
import com.intellisure.quotepolicyservice.entity.QuoteCoverage;
import com.intellisure.quotepolicyservice.entity.UnderwritingDecision;
import com.intellisure.quotepolicyservice.enums.PolicyStatus;
import com.intellisure.quotepolicyservice.enums.QuoteStatus;
import com.intellisure.quotepolicyservice.enums.UnderwritingDecisionType;
import com.intellisure.quotepolicyservice.repository.PolicyCoverageRepository;
import com.intellisure.quotepolicyservice.repository.PolicyRepository;
import com.intellisure.quotepolicyservice.repository.QuoteCoverageRepository;
import com.intellisure.quotepolicyservice.repository.QuoteRepository;
import com.intellisure.quotepolicyservice.repository.UnderwritingDecisionRepository;
import com.intellisure.quotepolicyservice.testsupport.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@TestPropertySource(
        properties = "spring.main.web-application-type=none"
)
@DisplayName("R2DBC repositories (H2 integration)")
class RepositoryIntegrationTest {

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private QuoteCoverageRepository quoteCoverageRepository;

    @Autowired
    private UnderwritingDecisionRepository underwritingDecisionRepository;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private PolicyCoverageRepository policyCoverageRepository;

    @Autowired
    private R2dbcEntityTemplate entityTemplate;

    @BeforeEach
    void cleanDatabase() {
        entityTemplate.delete(PolicyCoverage.class).all().block();
        entityTemplate.delete(Policy.class).all().block();
        entityTemplate.delete(UnderwritingDecision.class).all().block();
        entityTemplate.delete(QuoteCoverage.class).all().block();
        entityTemplate.delete(Quote.class).all().block();
    }

    private <T> T insert(T entity) {
        return entityTemplate.insert(entity).block();
    }

    private <T> T update(T entity) {
        return entityTemplate.update(entity).block();
    }

    private Quote saveQuote(QuoteStatus status) {
        return insert(TestFixtures.quote(status));
    }

    private QuoteCoverage saveCoverage(
            Quote quote,
            String code
    ) {
        QuoteCoverage coverage = TestFixtures.quoteCoverage(code);
        coverage.setQuoteId(quote.getQuoteId());

        return insert(coverage);
    }

    @Nested
    @DisplayName("QuoteRepository")
    class Quotes {

        @Test
        @DisplayName("persists and reads back every column")
        void roundTrip() {
            Quote quote = saveQuote(QuoteStatus.DRAFT);

            Quote loaded = quoteRepository.findById(quote.getQuoteId())
                    .block();

            assertNotNull(loaded);
            assertEquals(quote.getQuoteId(), loaded.getQuoteId());
            assertEquals(quote.getQuoteNumber(), loaded.getQuoteNumber());
            assertEquals(quote.getCustomerId(), loaded.getCustomerId());
            assertEquals(quote.getProductCode(), loaded.getProductCode());
            assertEquals(quote.getInsuranceNeed(), loaded.getInsuranceNeed());
            assertEquals(
                    quote.getBusinessOperations(),
                    loaded.getBusinessOperations()
            );
            assertEquals(QuoteStatus.DRAFT, loaded.getStatus());
            assertEquals(
                    quote.getRequestedEffectiveDate(),
                    loaded.getRequestedEffectiveDate()
            );
            assertEquals(
                    quote.getCreatedAt(),
                    loaded.getCreatedAt()
            );
            assertNull(loaded.getQuoteExpiresAt());
        }

        @Test
        @DisplayName("returns empty for an unknown id")
        void findByIdMissing() {
            assertNull(
                    quoteRepository.findById(UUID.randomUUID()).block()
            );
        }

        @Test
        @DisplayName("stores the generated quote number as text")
        void findByQuoteNumber() {
            Quote quote = saveQuote(QuoteStatus.DRAFT);

            Quote loaded = quoteRepository
                    .findByQuoteNumber(quote.getQuoteNumber())
                    .block();

            assertNotNull(loaded);
            assertEquals(quote.getQuoteId(), loaded.getQuoteId());
        }

        @Test
        @DisplayName("finds all quotes of a customer")
        void findAllByCustomerId() {
            Quote first = saveQuote(QuoteStatus.DRAFT);
            Quote second = saveQuote(QuoteStatus.QUOTED);

            Quote otherCustomer = TestFixtures.quote(QuoteStatus.DRAFT);
            otherCustomer.setCustomerId(UUID.randomUUID());
            insert(otherCustomer);

            List<Quote> found = quoteRepository
                    .findAllByCustomerId(TestFixtures.CUSTOMER_ID)
                    .collectList()
                    .block();

            assertNotNull(found);
            assertEquals(2, found.size());
            assertTrue(
                    found.stream()
                            .anyMatch(
                                    quote -> quote.getQuoteId()
                                            .equals(first.getQuoteId())
                            )
            );
            assertTrue(
                    found.stream()
                            .anyMatch(
                                    quote -> quote.getQuoteId()
                                            .equals(second.getQuoteId())
                            )
            );
        }

        @Test
        @DisplayName("filters quotes by customer and status")
        void findAllByCustomerIdAndStatus() {
            saveQuote(QuoteStatus.DRAFT);
            saveQuote(QuoteStatus.QUOTED);

            List<Quote> found = quoteRepository
                    .findAllByCustomerIdAndStatus(
                            TestFixtures.CUSTOMER_ID,
                            QuoteStatus.QUOTED
                    )
                    .collectList()
                    .block();

            assertNotNull(found);
            assertEquals(1, found.size());
            assertEquals(QuoteStatus.QUOTED, found.get(0).getStatus());
        }

        @Test
        @DisplayName("counts only the active quotes of an underwriter")
        void countActiveQuotesByUnderwriterId() {
            Quote inReview = saveQuote(QuoteStatus.IN_REVIEW);
            inReview.setAssignedUnderwriterId(
                    TestFixtures.UNDERWRITER_ID
            );
            update(inReview);

            Quote needsInformation = saveQuote(
                    QuoteStatus.NEEDS_INFORMATION
            );
            needsInformation.setAssignedUnderwriterId(
                    TestFixtures.UNDERWRITER_ID
            );
            update(needsInformation);

            Quote quoted = saveQuote(QuoteStatus.QUOTED);
            quoted.setAssignedUnderwriterId(TestFixtures.UNDERWRITER_ID);
            update(quoted);

            Long count = quoteRepository
                    .countActiveQuotesByUnderwriterId(
                            TestFixtures.UNDERWRITER_ID
                    )
                    .block();

            assertEquals(2L, count);
        }

        @Test
        @DisplayName("returns zero for an underwriter without active quotes")
        void countActiveQuotesForIdleUnderwriter() {
            assertEquals(
                    0L,
                    quoteRepository
                            .countActiveQuotesByUnderwriterId(
                                    TestFixtures.UNDERWRITER_ID
                            )
                            .block()
            );
        }

        @Test
        @DisplayName("finds the quotes whose offer has expired")
        void findAllByStatusAndQuoteExpiresAtLessThanEqual() {
            Quote expired = TestFixtures.quotedQuote();
            expired.setQuoteExpiresAt(LocalDateTime.now().minusDays(1));
            insert(expired);

            Quote live = TestFixtures.quotedQuote();
            live.setQuoteExpiresAt(LocalDateTime.now().plusDays(1));
            insert(live);

            List<Quote> found = quoteRepository
                    .findAllByStatusAndQuoteExpiresAtLessThanEqual(
                            QuoteStatus.QUOTED,
                            LocalDateTime.now()
                    )
                    .collectList()
                    .block();

            assertNotNull(found);
            assertEquals(1, found.size());
            assertEquals(
                    expired.getQuoteId(),
                    found.get(0).getQuoteId()
            );
        }

        @Test
        @DisplayName("enforces the unique quote number constraint")
        void enforcesUniqueQuoteNumber() {
            Quote first = saveQuote(QuoteStatus.DRAFT);

            Quote duplicate = TestFixtures.quote(QuoteStatus.DRAFT);
            duplicate.setQuoteNumber(first.getQuoteNumber());

            assertTrue(
                    org.junit.jupiter.api.Assertions.assertThrows(
                            org.springframework.dao.DuplicateKeyException
                                    .class,
                            () -> insert(duplicate)
                    ).getMessage() != null
            );
        }

        @Test
        @DisplayName("reports existence by quote number")
        void existsByQuoteNumber() {
            Quote quote = saveQuote(QuoteStatus.DRAFT);

            assertTrue(
                    quoteRepository.existsByQuoteNumber(
                            quote.getQuoteNumber()
                    ).block()
            );
            assertFalse(
                    quoteRepository.existsByQuoteNumber("QTE-NOPE").block()
            );
        }
    }

    @Nested
    @DisplayName("QuoteCoverageRepository")
    class QuoteCoverages {

        @Test
        @DisplayName("finds every coverage of a quote")
        void findAllByQuoteId() {
            Quote quote = saveQuote(QuoteStatus.DRAFT);

            saveCoverage(quote, "FIRE");
            saveCoverage(quote, "LIABILITY");

            List<QuoteCoverage> found = quoteCoverageRepository
                    .findAllByQuoteId(quote.getQuoteId())
                    .collectList()
                    .block();

            assertNotNull(found);
            assertEquals(2, found.size());
        }

        @Test
        @DisplayName("finds a coverage by its code")
        void findByQuoteIdAndCoverageCode() {
            Quote quote = saveQuote(QuoteStatus.DRAFT);

            saveCoverage(quote, "FIRE");

            QuoteCoverage found = quoteCoverageRepository
                    .findByQuoteIdAndCoverageCode(
                            quote.getQuoteId(),
                            "FIRE"
                    )
                    .block();

            assertNotNull(found);
            assertEquals("FIRE", found.getCoverageCode());
            assertEquals(
                    new BigDecimal("100000.00"),
                    found.getRequestedLimit()
            );
        }

        @Test
        @DisplayName("returns empty for an unknown coverage code")
        void findByQuoteIdAndCoverageCodeMissing() {
            Quote quote = saveQuote(QuoteStatus.DRAFT);

            assertNull(
                    quoteCoverageRepository
                            .findByQuoteIdAndCoverageCode(
                                    quote.getQuoteId(),
                                    "FLOOD"
                            )
                            .block()
            );
        }

        @Test
        @DisplayName("enforces the unique coverage code per quote")
        void enforcesUniqueCoverageCode() {
            Quote quote = saveQuote(QuoteStatus.DRAFT);

            saveCoverage(quote, "FIRE");

            QuoteCoverage duplicate = TestFixtures.quoteCoverage("FIRE");
            duplicate.setQuoteId(quote.getQuoteId());

            org.junit.jupiter.api.Assertions.assertThrows(
                    org.springframework.dao.DuplicateKeyException.class,
                    () -> insert(duplicate)
            );
        }

        @Test
        @DisplayName("deletes every coverage of a quote")
        void deleteAllByQuoteId() {
            Quote quote = saveQuote(QuoteStatus.DRAFT);

            saveCoverage(quote, "FIRE");
            saveCoverage(quote, "LIABILITY");

            quoteCoverageRepository.deleteAllByQuoteId(
                    quote.getQuoteId()
            ).block();

            assertEquals(
                    0,
                    quoteCoverageRepository.findAllByQuoteId(
                            quote.getQuoteId()
                    ).collectList()
                            .block()
                            .size()
            );
        }
    }

    @Nested
    @DisplayName("UnderwritingDecisionRepository")
    class Decisions {

        @Test
        @DisplayName("returns the newest decision first")
        void findFirstByQuoteIdOrderByDecidedAtDesc() {
            Quote quote = saveQuote(QuoteStatus.IN_REVIEW);

            UnderwritingDecision older = TestFixtures.decision(
                    UnderwritingDecisionType.MORE_INFORMATION_REQUIRED
            );
            older.setQuoteId(quote.getQuoteId());
            older.setDecidedAt(TestFixtures.NOW.minusDays(1));
            insert(older);

            UnderwritingDecision newer = TestFixtures.decision(
                    UnderwritingDecisionType.APPROVED
            );
            newer.setQuoteId(quote.getQuoteId());
            newer.setDecidedAt(TestFixtures.NOW);
            insert(newer);

            UnderwritingDecision latest =
                    underwritingDecisionRepository
                            .findFirstByQuoteIdOrderByDecidedAtDesc(
                                    quote.getQuoteId()
                            )
                            .block();

            assertNotNull(latest);
            assertEquals(
                    UnderwritingDecisionType.APPROVED,
                    latest.getDecision()
            );
        }

        @Test
        @DisplayName("stores the downstream source decision id")
        void findBySourceDecisionId() {
            Quote quote = saveQuote(QuoteStatus.IN_REVIEW);
            UUID sourceDecisionId = UUID.randomUUID();

            UnderwritingDecision decision = TestFixtures.decision(
                    UnderwritingDecisionType.APPROVED
            );
            decision.setQuoteId(quote.getQuoteId());
            decision.setSourceDecisionId(sourceDecisionId);
            insert(decision);

            UnderwritingDecision found = underwritingDecisionRepository
                    .findBySourceDecisionId(sourceDecisionId)
                    .block();

            assertNotNull(found);
            assertEquals(
                    decision.getUnderwritingDecisionId(),
                    found.getUnderwritingDecisionId()
            );
        }

        @Test
        @DisplayName("enforces the unique source decision id")
        void enforcesUniqueSourceDecisionId() {
            Quote quote = saveQuote(QuoteStatus.IN_REVIEW);
            UUID sourceDecisionId = UUID.randomUUID();

            UnderwritingDecision first = TestFixtures.decision(
                    UnderwritingDecisionType.APPROVED
            );
            first.setQuoteId(quote.getQuoteId());
            first.setSourceDecisionId(sourceDecisionId);
            insert(first);

            UnderwritingDecision duplicate = TestFixtures.decision(
                    UnderwritingDecisionType.DECLINED
            );
            duplicate.setQuoteId(quote.getQuoteId());
            duplicate.setSourceDecisionId(sourceDecisionId);

            org.junit.jupiter.api.Assertions.assertThrows(
                    org.springframework.dao.DuplicateKeyException.class,
                    () -> insert(duplicate)
            );
        }

        @Test
        @DisplayName("finds every decision of a quote")
        void findAllByQuoteId() {
            Quote quote = saveQuote(QuoteStatus.IN_REVIEW);

            for (UnderwritingDecisionType type : List.of(
                    UnderwritingDecisionType.APPROVED,
                    UnderwritingDecisionType.REFERRED
            )) {
                UnderwritingDecision decision = TestFixtures.decision(type);
                decision.setQuoteId(quote.getQuoteId());
                insert(decision);
            }

            assertEquals(
                    2,
                    underwritingDecisionRepository.findAllByQuoteId(
                            quote.getQuoteId()
                    ).collectList()
                            .block()
                            .size()
            );
        }
    }

    private Policy newPolicy(PolicyStatus status) {
        Policy policy = TestFixtures.policy(status);
        Quote quote = saveQuote(QuoteStatus.BOUND);
        policy.setQuoteId(quote.getQuoteId());

        return policy;
    }

    private Policy savePolicy(PolicyStatus status) {
        return insert(newPolicy(status));
    }

    @Nested
    @DisplayName("PolicyRepository")
    class Policies {

        private Policy newPolicy(PolicyStatus status) {
            Policy policy = TestFixtures.policy(status);
            Quote quote = saveQuote(QuoteStatus.BOUND);
            policy.setQuoteId(quote.getQuoteId());
            return policy;
        }

        private Policy savePolicy(PolicyStatus status) {
            return insert(newPolicy(status));
        }

        @Test
        @DisplayName("round trips every policy column")
        void roundTrip() {
            Policy policy = savePolicy(PolicyStatus.IN_FORCE);

            Policy loaded = policyRepository.findById(
                    policy.getPolicyId()
            ).block();

            assertNotNull(loaded);
            assertEquals(PolicyStatus.IN_FORCE, loaded.getStatus());
            assertEquals(
                    policy.getPolicyNumber(),
                    loaded.getPolicyNumber()
            );
            assertEquals(
                    policy.getTotalPremium(),
                    loaded.getTotalPremium()
            );
            assertEquals(
                    policy.getIssuedByUserId(),
                    loaded.getIssuedByUserId()
            );
            assertEquals(
                    policy.getStartDate(),
                    loaded.getStartDate()
            );
            assertEquals(
                    policy.getEndDate(),
                    loaded.getEndDate()
            );
        }

        @Test
        @DisplayName("finds a policy by its number")
        void findByPolicyNumber() {
            Policy policy = savePolicy(PolicyStatus.IN_FORCE);

            assertNotNull(
                    policyRepository.findByPolicyNumber(
                            policy.getPolicyNumber()
                    ).block()
            );
            assertNull(
                    policyRepository.findByPolicyNumber("POL-NOPE").block()
            );
        }

        @Test
        @DisplayName("reports whether a quote is already bound")
        void existsByQuoteId() {
            Quote quote = saveQuote(QuoteStatus.BOUND);

            assertFalse(
                    policyRepository.existsByQuoteId(quote.getQuoteId())
                            .block()
            );

            Policy policy = newPolicy(PolicyStatus.IN_FORCE);
            policy.setQuoteId(quote.getQuoteId());
            insert(policy);

            assertTrue(
                    policyRepository.existsByQuoteId(quote.getQuoteId())
                            .block()
            );
        }

        @Test
        @DisplayName("finds the policies due for activation")
        void findAllByStatusAndStartDateLessThanEqual() {
            Policy pending = newPolicy(PolicyStatus.PENDING_ISSUANCE);
            pending.setStartDate(LocalDate.now());
            pending.setEndDate(LocalDate.now().plusYears(1));
            insert(pending);

            Policy future = newPolicy(PolicyStatus.PENDING_ISSUANCE);
            future.setStartDate(LocalDate.now().plusMonths(2));
            future.setEndDate(LocalDate.now().plusYears(2));
            insert(future);

            List<Policy> found = policyRepository
                    .findAllByStatusAndStartDateLessThanEqual(
                            PolicyStatus.PENDING_ISSUANCE,
                            LocalDate.now()
                    )
                    .collectList()
                    .block();

            assertNotNull(found);
            assertEquals(1, found.size());
            assertEquals(
                    pending.getPolicyId(),
                    found.get(0).getPolicyId()
            );
        }

        @Test
        @DisplayName("finds the policies past their end date")
        void findAllByStatusAndEndDateBefore() {
            Policy expired = newPolicy(PolicyStatus.IN_FORCE);
            expired.setStartDate(LocalDate.now().minusYears(1));
            expired.setEndDate(LocalDate.now().minusDays(1));
            insert(expired);

            Policy live = newPolicy(PolicyStatus.IN_FORCE);
            live.setStartDate(LocalDate.now());
            live.setEndDate(LocalDate.now().plusYears(1));
            insert(live);

            List<Policy> found = policyRepository
                    .findAllByStatusAndEndDateBefore(
                            PolicyStatus.IN_FORCE,
                            LocalDate.now()
                    )
                    .collectList()
                    .block();

            assertNotNull(found);
            assertEquals(1, found.size());
            assertEquals(
                    expired.getPolicyId(),
                    found.get(0).getPolicyId()
            );
        }

        @Test
        @DisplayName("finds every policy of a customer")
        void findAllByCustomerId() {
            savePolicy(PolicyStatus.IN_FORCE);
            savePolicy(PolicyStatus.PENDING_ISSUANCE);

            assertEquals(
                    2,
                    policyRepository.findAllByCustomerId(
                            TestFixtures.CUSTOMER_ID
                    ).collectList()
                            .block()
                            .size()
            );
        }

        @Test
        @DisplayName("enforces one policy per quote")
        void enforcesUniqueQuoteId() {
            Quote quote = saveQuote(QuoteStatus.BOUND);

            Policy first = newPolicy(PolicyStatus.IN_FORCE);
            first.setQuoteId(quote.getQuoteId());
            insert(first);

            Policy duplicate = TestFixtures.policy(PolicyStatus.IN_FORCE);
            duplicate.setQuoteId(quote.getQuoteId());

            org.junit.jupiter.api.Assertions.assertThrows(
                    org.springframework.dao.DuplicateKeyException.class,
                    () -> insert(duplicate)
            );
        }
    }

    @Nested
    @DisplayName("PolicyCoverageRepository")
    class PolicyCoverages {

        @Test
        @DisplayName("finds a policy coverage by code")
        void findByPolicyIdAndCoverageCode() {
            Policy policy = savePolicy(PolicyStatus.IN_FORCE);


            PolicyCoverage coverage = TestFixtures.policyCoverage(
                    "FIRE",
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 12, 31)
            );
            coverage.setPolicyId(policy.getPolicyId());
            insert(coverage);

            PolicyCoverage found = policyCoverageRepository
                    .findByPolicyIdAndCoverageCode(
                            policy.getPolicyId(),
                            "FIRE"
                    )
                    .block();

            assertNotNull(found);
            assertEquals(
                    new BigDecimal("90000.00"),
                    found.getLimitAmount()
            );
            assertEquals(
                    LocalDate.of(2026, 1, 1),
                    found.getEffectiveFrom()
            );
        }

        @Test
        @DisplayName("finds the coverages effective on a date")
        void findAllByPolicyIdAndEffectivePeriod() {
            Policy policy = savePolicy(PolicyStatus.IN_FORCE);


            PolicyCoverage first = TestFixtures.policyCoverage(
                    "FIRE",
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 12, 31)
            );
            first.setPolicyId(policy.getPolicyId());
            insert(first);

            PolicyCoverage second = TestFixtures.policyCoverage(
                    "FLOOD",
                    LocalDate.of(2025, 1, 1),
                    LocalDate.of(2025, 12, 31)
            );
            second.setPolicyId(policy.getPolicyId());
            insert(second);

            List<PolicyCoverage> found = policyCoverageRepository
                    .findAllByPolicyIdAndEffectiveFromLessThanEqualAndEffectiveToGreaterThanEqual(
                            policy.getPolicyId(),
                            LocalDate.of(2026, 6, 1),
                            LocalDate.of(2026, 6, 1)
                    )
                    .collectList()
                    .block();

            assertNotNull(found);
            assertEquals(1, found.size());
            assertEquals("FIRE", found.get(0).getCoverageCode());
        }

        @Test
        @DisplayName("reports whether a coverage exists on a policy")
        void existsByPolicyIdAndCoverageCode() {
            Policy policy = savePolicy(PolicyStatus.IN_FORCE);


            PolicyCoverage coverage = TestFixtures.policyCoverage(
                    "FIRE",
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 12, 31)
            );
            coverage.setPolicyId(policy.getPolicyId());
            insert(coverage);

            assertTrue(
                    policyCoverageRepository
                            .existsByPolicyIdAndCoverageCode(
                                    policy.getPolicyId(),
                                    "FIRE"
                            )
                            .block()
            );
            assertFalse(
                    policyCoverageRepository
                            .existsByPolicyIdAndCoverageCode(
                                    policy.getPolicyId(),
                                    "FLOOD"
                            )
                            .block()
            );
        }
    }
}
