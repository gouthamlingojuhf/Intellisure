package com.intellisure.quotepolicyservice.service;

import com.intellisure.quotepolicyservice.dto.response.CoverageCheckResponse;
import com.intellisure.quotepolicyservice.dto.response.PolicyCoverageResponse;
import com.intellisure.quotepolicyservice.dto.response.PolicyStatusResponse;
import com.intellisure.quotepolicyservice.entity.Policy;
import com.intellisure.quotepolicyservice.entity.PolicyCoverage;
import com.intellisure.quotepolicyservice.entity.Quote;
import com.intellisure.quotepolicyservice.entity.QuoteCoverage;
import com.intellisure.quotepolicyservice.enums.PolicyStatus;
import com.intellisure.quotepolicyservice.enums.QuoteStatus;
import com.intellisure.quotepolicyservice.exception.AccessDeniedBusinessException;
import com.intellisure.quotepolicyservice.exception.BusinessException;
import com.intellisure.quotepolicyservice.exception.ResourceNotFoundException;
import com.intellisure.quotepolicyservice.mapper.PolicyMapper;
import com.intellisure.quotepolicyservice.repository.PolicyCoverageRepository;
import com.intellisure.quotepolicyservice.repository.PolicyRepository;
import com.intellisure.quotepolicyservice.repository.QuoteCoverageRepository;
import com.intellisure.quotepolicyservice.repository.QuoteRepository;
import com.intellisure.quotepolicyservice.security.SecurityActorService;
import com.intellisure.quotepolicyservice.testsupport.EntityTemplateStubber;
import com.intellisure.quotepolicyservice.testsupport.TestAssertions;
import com.intellisure.quotepolicyservice.testsupport.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("PolicyService")
class PolicyServiceTest {

    private QuoteRepository quoteRepository;
    private QuoteCoverageRepository quoteCoverageRepository;
    private PolicyRepository policyRepository;
    private PolicyCoverageRepository policyCoverageRepository;
    private R2dbcEntityTemplate entityTemplate;
    private SecurityActorService securityActorService;

    private PolicyService policyService;

    @BeforeEach
    void setUp() {
        quoteRepository = mock(QuoteRepository.class);
        quoteCoverageRepository = mock(QuoteCoverageRepository.class);
        policyRepository = mock(PolicyRepository.class);
        policyCoverageRepository = mock(PolicyCoverageRepository.class);
        entityTemplate = mock(R2dbcEntityTemplate.class);
        securityActorService = mock(SecurityActorService.class);

        policyService = new PolicyService(
                quoteRepository,
                quoteCoverageRepository,
                policyRepository,
                policyCoverageRepository,
                entityTemplate,
                new PolicyMapper(),
                securityActorService
        );

        lenient().when(
                entityTemplate.update(any(Policy.class))
        ).thenAnswer(
                invocation -> Mono.just(invocation.getArgument(0))
        );
        lenient().when(
                entityTemplate.update(any(Quote.class))
        ).thenAnswer(
                invocation -> Mono.just(invocation.getArgument(0))
        );
    }

    @Nested
    @DisplayName("bindQuote")
    class BindQuote {

        private Quote quote;

        @BeforeEach
        void prepare() {
            quote = TestFixtures.acceptedQuote();
        }

        private void givenBindableQuote() {
            when(policyRepository.existsByQuoteId(quote.getQuoteId()))
                    .thenReturn(Mono.just(false));
            when(quoteRepository.findById(quote.getQuoteId())).thenReturn(
                    Mono.just(quote)
            );
            when(securityActorService.currentUserId()).thenReturn(
                    Mono.just(TestFixtures.UNDERWRITER_ID)
            );
        }

        private void givenOfferedCoverages() {
            QuoteCoverage first = TestFixtures.offeredCoverage("FIRE");
            QuoteCoverage second = TestFixtures.offeredCoverage("LIABILITY");

            first.setQuoteId(quote.getQuoteId());
            second.setQuoteId(quote.getQuoteId());

            lenient().when(
                    quoteCoverageRepository.findAllByQuoteId(
                            quote.getQuoteId()
                    )
            ).thenReturn(Flux.just(first, second));

            EntityTemplateStubber.stubInsert(
                    entityTemplate,
                    Policy.class
            );
            EntityTemplateStubber.stubInsert(
                    entityTemplate,
                    PolicyCoverage.class
            );
        }

        @Test
        @DisplayName("refuses to bind the same quote twice")
        void refusesDuplicateBinding() {
            when(policyRepository.existsByQuoteId(quote.getQuoteId()))
                    .thenReturn(Mono.just(true));

            StepVerifier.create(
                            policyService.bindQuote(quote.getQuoteId())
                    )
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            BusinessException.class,
                            "A policy has already been created for this "
                                    + "quote",
                            error
                    ))
                    .verify();

            verify(quoteRepository, never()).findById(any(UUID.class));
        }

        @Test
        @DisplayName("fails with not found when the quote does not exist")
        void failsForMissingQuote() {
            when(policyRepository.existsByQuoteId(quote.getQuoteId()))
                    .thenReturn(Mono.just(false));
            when(quoteRepository.findById(quote.getQuoteId())).thenReturn(
                    Mono.empty()
            );

            StepVerifier.create(
                            policyService.bindQuote(quote.getQuoteId())
                    )
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            ResourceNotFoundException.class,
                            containsString("Quote not found with ID:"),
                            error
                    ))
                    .verify();
        }

        @Test
        @DisplayName("only the assigned underwriter can bind the quote")
        void rejectsUnassignedUnderwriter() {
            givenBindableQuote();
            when(securityActorService.currentUserId()).thenReturn(
                    Mono.just(TestFixtures.OTHER_UNDERWRITER_ID)
            );

            StepVerifier.create(
                            policyService.bindQuote(quote.getQuoteId())
                    )
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            AccessDeniedBusinessException.class,
                            "Only the assigned underwriter can bind this "
                                    + "quote",
                            error
                    ))
                    .verify();
        }

        @Test
        @DisplayName("only an ACCEPTED quote can be bound")
        void rejectsNonAcceptedQuote() {
            quote.setStatus(QuoteStatus.QUOTED);

            givenBindableQuote();

            StepVerifier.create(
                            policyService.bindQuote(quote.getQuoteId())
                    )
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            BusinessException.class,
                            "Only an ACCEPTED quote can be bound. "
                                    + "Current status: QUOTED",
                            error
                    ))
                    .verify();
        }

        @Test
        @DisplayName("an expired quote cannot be bound")
        void rejectsExpiredQuote() {
            quote.setQuoteExpiresAt(LocalDateTime.now().minusDays(1));

            givenBindableQuote();

            StepVerifier.create(
                            policyService.bindQuote(quote.getQuoteId())
                    )
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            BusinessException.class,
                            "The quote has expired and cannot be bound",
                            error
                    ))
                    .verify();
        }

        @Test
        @DisplayName("a quote without an expiration date cannot be bound")
        void rejectsQuoteWithoutExpiration() {
            quote.setQuoteExpiresAt(null);

            givenBindableQuote();

            StepVerifier.create(
                            policyService.bindQuote(quote.getQuoteId())
                    )
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            BusinessException.class,
                            "The quote has expired and cannot be bound",
                            error
                    ))
                    .verify();
        }

        @Test
        @DisplayName("requires the customer acceptance audit trail")
        void requiresAcceptanceInformation() {
            quote.setAcceptedAt(null);

            givenBindableQuote();

            StepVerifier.create(
                            policyService.bindQuote(quote.getQuoteId())
                    )
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            BusinessException.class,
                            "Customer acceptance information is missing",
                            error
                    ))
                    .verify();
        }

        @Test
        @DisplayName("requires a final premium")
        void requiresFinalPremium() {
            quote.setTotalPremium(null);

            givenBindableQuote();

            StepVerifier.create(
                            policyService.bindQuote(quote.getQuoteId())
                    )
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            BusinessException.class,
                            "The quote does not have a final premium",
                            error
                    ))
                    .verify();
        }

        @Test
        @DisplayName("rejects a quote without coverages")
        void rejectsQuoteWithoutCoverages() {
            givenBindableQuote();
            when(
                    quoteCoverageRepository.findAllByQuoteId(
                            quote.getQuoteId()
                    )
            ).thenReturn(Flux.empty());

            StepVerifier.create(
                            policyService.bindQuote(quote.getQuoteId())
                    )
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            BusinessException.class,
                            "The quote does not contain coverages",
                            error
                    ))
                    .verify();
        }

        @Test
        @DisplayName("rejects coverages that are missing offered terms")
        void rejectsIncompleteOfferedTerms() {
            givenBindableQuote();

            QuoteCoverage incomplete = TestFixtures.offeredCoverage("FIRE");
            incomplete.setCoveragePremium(null);
            incomplete.setQuoteId(quote.getQuoteId());

            when(
                    quoteCoverageRepository.findAllByQuoteId(
                            quote.getQuoteId()
                    )
            ).thenReturn(Flux.just(incomplete));

            StepVerifier.create(
                            policyService.bindQuote(quote.getQuoteId())
                    )
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            BusinessException.class,
                            "All quote coverages must have final offered "
                                    + "terms before binding",
                            error
                    ))
                    .verify();
        }

        @Test
        @DisplayName("creates a one year policy and marks the quote BOUND")
        void bindsQuote() {
            quote.setRequestedEffectiveDate(LocalDate.now());

            givenBindableQuote();
            givenOfferedCoverages();

            StepVerifier.create(
                            policyService.bindQuote(quote.getQuoteId())
                    )
                    .assertNext(response -> {
                        assertNotNull(response.policyId());
                        assertTrue(
                                response.policyNumber()
                                        .matches(
                                                "POL-\\d{4}-[0-9A-F]{8}"
                                        )
                        );
                        assertEquals(
                                quote.getQuoteId(),
                                response.quoteId()
                        );
                        assertEquals(
                                TestFixtures.CUSTOMER_ID,
                                response.customerId()
                        );
                        assertEquals(
                                TestFixtures.UNDERWRITER_ID,
                                response.issuedByUserId()
                        );
                        assertEquals(
                                PolicyStatus.IN_FORCE,
                                response.status()
                        );
                        assertEquals(
                                quote.getRequestedEffectiveDate(),
                                response.startDate()
                        );
                        assertEquals(
                                quote.getRequestedEffectiveDate()
                                        .plusYears(1)
                                        .minusDays(1),
                                response.endDate()
                        );
                        assertEquals(
                                new BigDecimal("1500.00"),
                                response.totalPremium()
                        );
                        assertEquals(2, response.coverages().size());
                        assertNotNull(response.boundAt());
                        assertNotNull(response.issuedAt());
                        assertNull(response.expiredAt());
                    })
                    .verifyComplete();

            assertEquals(QuoteStatus.BOUND, quote.getStatus());
            assertEquals(
                    TestFixtures.UNDERWRITER_ID,
                    quote.getBoundByUserId()
            );
            assertNotNull(quote.getBoundAt());
        }

        @Test
        @DisplayName("a future start date produces a PENDING_ISSUANCE policy")
        void bindsFutureQuoteAsPendingIssuance() {
            quote.setRequestedEffectiveDate(
                    LocalDate.now().plusDays(30)
            );

            givenBindableQuote();
            givenOfferedCoverages();

            StepVerifier.create(
                            policyService.bindQuote(quote.getQuoteId())
                    )
                    .assertNext(response -> assertEquals(
                            PolicyStatus.PENDING_ISSUANCE,
                            response.status()
                    ))
                    .verifyComplete();
        }

        @Test
        @DisplayName("policy coverages inherit the offered terms and the policy period")
        void copiesOfferedTermsIntoPolicyCoverages() {
            givenBindableQuote();

            QuoteCoverage first = TestFixtures.offeredCoverage("FIRE");
            first.setQuoteId(quote.getQuoteId());

            when(
                    quoteCoverageRepository.findAllByQuoteId(
                            quote.getQuoteId()
                    )
            ).thenReturn(Flux.just(first));

            EntityTemplateStubber.stubInsert(
                    entityTemplate,
                    Policy.class
            );
            EntityTemplateStubber.stubInsert(
                    entityTemplate,
                    PolicyCoverage.class
            );

            LocalDate startDate = quote.getRequestedEffectiveDate();

            StepVerifier.create(
                            policyService.bindQuote(quote.getQuoteId())
                    )
                    .assertNext(response -> {
                        PolicyCoverageResponse coverage = response
                                .coverages()
                                .get(0);

                        assertEquals(
                                first.getCoverageCode(),
                                coverage.coverageCode()
                        );
                        assertEquals(
                                first.getCoverageName(),
                                coverage.coverageName()
                        );
                        assertEquals(
                                first.getOfferedLimit(),
                                coverage.limitAmount()
                        );
                        assertEquals(
                                first.getOfferedDeductible(),
                                coverage.deductibleAmount()
                        );
                        assertEquals(
                                first.getCoveragePremium(),
                                coverage.coveragePremium()
                        );
                        assertEquals(
                                startDate,
                                coverage.effectiveFrom()
                        );
                        assertEquals(
                                startDate.plusYears(1).minusDays(1),
                                coverage.effectiveTo()
                        );
                    })
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("scheduled lifecycle transitions")
    class Lifecycle {

        @Test
        @DisplayName("activates pending policies whose start date has arrived")
        void activatesPolicies() {
            Policy policy = TestFixtures.policy(
                    PolicyStatus.PENDING_ISSUANCE
            );

            when(
                    policyRepository
                            .findAllByStatusAndStartDateLessThanEqual(
                                    eq(PolicyStatus.PENDING_ISSUANCE),
                                    any(LocalDate.class)
                            )
            ).thenReturn(Flux.just(policy));

            StepVerifier.create(policyService.activateScheduledPolicies())
                    .expectNext(1L)
                    .verifyComplete();

            assertEquals(PolicyStatus.IN_FORCE, policy.getStatus());
        }

        @Test
        @DisplayName("returns zero when nothing is pending activation")
        void activatesNothing() {
            when(
                    policyRepository
                            .findAllByStatusAndStartDateLessThanEqual(
                                    eq(PolicyStatus.PENDING_ISSUANCE),
                                    any(LocalDate.class)
                            )
            ).thenReturn(Flux.empty());

            StepVerifier.create(policyService.activateScheduledPolicies())
                    .expectNext(0L)
                    .verifyComplete();
        }

        @Test
        @DisplayName("expires in force policies past their end date")
        void expiresPolicies() {
            Policy policy = TestFixtures.policy(PolicyStatus.IN_FORCE);

            when(
                    policyRepository
                            .findAllByStatusAndEndDateBefore(
                                    eq(PolicyStatus.IN_FORCE),
                                    any(LocalDate.class)
                            )
            ).thenReturn(Flux.just(policy));

            StepVerifier.create(policyService.expirePolicies())
                    .expectNext(1L)
                    .verifyComplete();

            assertEquals(PolicyStatus.EXPIRED, policy.getStatus());
            assertNotNull(policy.getExpiredAt());
        }

        @Test
        @DisplayName("returns zero when nothing has expired")
        void expiresNothing() {
            when(
                    policyRepository
                            .findAllByStatusAndEndDateBefore(
                                    eq(PolicyStatus.IN_FORCE),
                                    any(LocalDate.class)
                            )
            ).thenReturn(Flux.empty());

            StepVerifier.create(policyService.expirePolicies())
                    .expectNext(0L)
                    .verifyComplete();
        }

        @Test
        @DisplayName("only PENDING_ISSUANCE and IN_FORCE policies are considered")
        void queriesOnlyTheExpectedStatuses() {
            when(
                    policyRepository
                            .findAllByStatusAndStartDateLessThanEqual(
                                    eq(PolicyStatus.PENDING_ISSUANCE),
                                    any(LocalDate.class)
                            )
            ).thenReturn(Flux.empty());
            when(
                    policyRepository
                            .findAllByStatusAndEndDateBefore(
                                    eq(PolicyStatus.IN_FORCE),
                                    any(LocalDate.class)
                            )
            ).thenReturn(Flux.empty());

            StepVerifier.create(policyService.activateScheduledPolicies())
                    .expectNext(0L)
                    .verifyComplete();
            StepVerifier.create(policyService.expirePolicies())
                    .expectNext(0L)
                    .verifyComplete();

            verify(policyRepository)
                    .findAllByStatusAndStartDateLessThanEqual(
                            eq(PolicyStatus.PENDING_ISSUANCE),
                            any(LocalDate.class)
                    );
            verify(policyRepository)
                    .findAllByStatusAndEndDateBefore(
                            eq(PolicyStatus.IN_FORCE),
                            any(LocalDate.class)
                    );
        }
    }

    @Nested
    @DisplayName("checkCoverage")
    class CheckCoverage {

        private Policy policy;
        private LocalDate startDate;
        private LocalDate endDate;

        @BeforeEach
        void prepare() {
            startDate = LocalDate.of(2026, 1, 1);
            endDate = LocalDate.of(2026, 12, 31);

            policy = TestFixtures.policy(PolicyStatus.IN_FORCE);
            policy.setStartDate(startDate);
            policy.setEndDate(endDate);
        }

        @Test
        @DisplayName("fails when the policy number is unknown")
        void unknownPolicy() {
            when(policyRepository.findByPolicyNumber("POL-MISSING"))
                    .thenReturn(Mono.empty());

            StepVerifier.create(
                            policyService.checkCoverage(
                                    "POL-MISSING",
                                    "fire",
                                    startDate
                            )
                    )
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            ResourceNotFoundException.class,
                            "Policy not found with number: POL-MISSING",
                            error
                    ))
                    .verify();
        }

        @Test
        @DisplayName("reports the policy as not effective outside the policy period")
        void policyNotEffective() {
            when(policyRepository.findByPolicyNumber(
                    policy.getPolicyNumber()
            )).thenReturn(Mono.just(policy));

            StepVerifier.create(
                            policyService.checkCoverage(
                                    policy.getPolicyNumber(),
                                    "  fire  ",
                                    startDate.minusDays(1)
                            )
                    )
                    .assertNext(response -> {
                        assertFalse(response.coveragePresent());
                        assertFalse(response.policyEffectiveOnDate());
                        assertEquals("FIRE", response.coverageCode());
                        assertEquals(
                                "The policy was not effective on the "
                                        + "requested date",
                                response.message()
                        );
                        assertNull(response.limitAmount());
                    })
                    .verifyComplete();

            verify(policyCoverageRepository, never())
                    .findByPolicyIdAndCoverageCode(
                            any(UUID.class),
                            any()
                    );
        }

        @Test
        @DisplayName("returns the coverage detail when the coverage is effective")
        void coverageAvailable() {
            PolicyCoverage coverage = TestFixtures.policyCoverage(
                    "FIRE",
                    startDate,
                    endDate
            );
            coverage.setPolicyId(policy.getPolicyId());

            when(policyRepository.findByPolicyNumber(
                    policy.getPolicyNumber()
            )).thenReturn(Mono.just(policy));
            when(
                    policyCoverageRepository
                            .findByPolicyIdAndCoverageCode(
                                    policy.getPolicyId(),
                                    "FIRE"
                            )
            ).thenReturn(Mono.just(coverage));

            StepVerifier.create(
                            policyService.checkCoverage(
                                    policy.getPolicyNumber(),
                                    "fire",
                                    LocalDate.of(2026, 6, 15)
                            )
                    )
                    .assertNext((CoverageCheckResponse response) -> {
                        assertTrue(response.coveragePresent());
                        assertTrue(response.policyEffectiveOnDate());
                        assertEquals(
                                new BigDecimal("90000.00"),
                                response.limitAmount()
                        );
                        assertEquals(
                                "Survey required",
                                response.conditions()
                        );
                        assertEquals("Flood", response.exclusions());
                        assertEquals(30, response.waitingPeriodDays());
                        assertEquals(
                                startDate,
                                response.coverageEffectiveFrom()
                        );
                        assertEquals(
                                endDate,
                                response.coverageEffectiveTo()
                        );
                        assertEquals(
                                "Coverage was present and effective on "
                                        + "the requested date",
                                response.message()
                        );
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("treats the coverage period boundaries as inclusive")
        void boundariesAreInclusive() {
            LocalDate coverageFrom = startDate.plusMonths(1);
            LocalDate coverageTo = endDate.minusMonths(1);

            PolicyCoverage coverage = TestFixtures.policyCoverage(
                    "FIRE",
                    coverageFrom,
                    coverageTo
            );
            coverage.setPolicyId(policy.getPolicyId());

            when(policyRepository.findByPolicyNumber(
                    policy.getPolicyNumber()
            )).thenReturn(Mono.just(policy));
            when(
                    policyCoverageRepository
                            .findByPolicyIdAndCoverageCode(
                                    policy.getPolicyId(),
                                    "FIRE"
                            )
            ).thenReturn(Mono.just(coverage));

            for (LocalDate date : List.of(
                    coverageFrom,
                    coverageTo
            )) {
                StepVerifier.create(
                                policyService.checkCoverage(
                                        policy.getPolicyNumber(),
                                        "FIRE",
                                        date
                                )
                        )
                        .assertNext(
                                CoverageCheckResponse::coveragePresent
                        )
                        .verifyComplete();
            }

            StepVerifier.create(
                            policyService.checkCoverage(
                                    policy.getPolicyNumber(),
                                    "FIRE",
                                    startDate
                            )
                    )
                    .assertNext(response -> {
                        assertFalse(response.coveragePresent());
                        assertTrue(response.policyEffectiveOnDate());
                        assertEquals(
                                "The requested coverage was not present "
                                        + "or effective on the requested "
                                        + "date",
                                response.message()
                        );
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("reports a missing coverage while the policy is effective")
        void coverageMissing() {
            when(policyRepository.findByPolicyNumber(
                    policy.getPolicyNumber()
            )).thenReturn(Mono.just(policy));
            when(
                    policyCoverageRepository
                            .findByPolicyIdAndCoverageCode(
                                    policy.getPolicyId(),
                                    "FLOOD"
                            )
            ).thenReturn(Mono.empty());

            StepVerifier.create(
                            policyService.checkCoverage(
                                    policy.getPolicyNumber(),
                                    "FLOOD",
                                    startDate
                            )
                    )
                    .assertNext(response -> {
                        assertFalse(response.coveragePresent());
                        assertTrue(response.policyEffectiveOnDate());
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("reports a coverage that is outside its own effective period")
        void coverageOutsideCoveragePeriod() {
            PolicyCoverage coverage = TestFixtures.policyCoverage(
                    "FIRE",
                    startDate.plusMonths(2),
                    endDate.minusMonths(2)
            );
            coverage.setPolicyId(policy.getPolicyId());

            when(policyRepository.findByPolicyNumber(
                    policy.getPolicyNumber()
            )).thenReturn(Mono.just(policy));
            when(
                    policyCoverageRepository
                            .findByPolicyIdAndCoverageCode(
                                    policy.getPolicyId(),
                                    "FIRE"
                            )
            ).thenReturn(Mono.just(coverage));

            StepVerifier.create(
                            policyService.checkCoverage(
                                    policy.getPolicyNumber(),
                                    "FIRE",
                                    startDate.plusDays(1)
                            )
                    )
                    .assertNext(response -> assertFalse(
                            response.coveragePresent()
                    ))
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("checkPolicyStatus")
    class CheckPolicyStatus {

        @Test
        @DisplayName("fails for an unknown policy number")
        void unknownPolicy() {
            when(policyRepository.findByPolicyNumber("POL-X")).thenReturn(
                    Mono.empty()
            );

            StepVerifier.create(
                            policyService.checkPolicyStatus(
                                    "POL-X",
                                    LocalDate.now()
                            )
                    )
                    .expectError(ResourceNotFoundException.class)
                    .verify();
        }

        @Test
        @DisplayName("flags a date inside the policy period as active")
        void activeOnDate() {
            Policy policy = TestFixtures.policy(PolicyStatus.IN_FORCE);
            policy.setStartDate(LocalDate.of(2026, 1, 1));
            policy.setEndDate(LocalDate.of(2026, 12, 31));

            when(policyRepository.findByPolicyNumber(
                    policy.getPolicyNumber()
            )).thenReturn(Mono.just(policy));

            StepVerifier.create(
                            policyService.checkPolicyStatus(
                                    policy.getPolicyNumber(),
                                    LocalDate.of(2026, 5, 1)
                            )
                    )
                    .assertNext((PolicyStatusResponse response) -> {
                        assertTrue(response.activeOnRequestedDate());
                        assertEquals(
                                PolicyStatus.IN_FORCE,
                                response.status()
                        );
                        assertEquals(
                                policy.getPolicyId(),
                                response.policyId()
                        );
                        assertEquals(
                                policy.getCustomerId(),
                                response.customerId()
                        );
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("flags a date outside the policy period as inactive")
        void inactiveOnDate() {
            Policy policy = TestFixtures.policy(PolicyStatus.IN_FORCE);
            policy.setStartDate(LocalDate.of(2026, 1, 1));
            policy.setEndDate(LocalDate.of(2026, 12, 31));

            when(policyRepository.findByPolicyNumber(
                    policy.getPolicyNumber()
            )).thenReturn(Mono.just(policy));

            StepVerifier.create(
                            policyService.checkPolicyStatus(
                                    policy.getPolicyNumber(),
                                    LocalDate.of(2027, 1, 1)
                            )
                    )
                    .assertNext(response -> assertFalse(
                            response.activeOnRequestedDate()
                    ))
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("read operations")
    class ReadOperations {

        @Test
        @DisplayName("getPolicyById returns the policy with its coverages")
        void getPolicyById() {
            Policy policy = TestFixtures.policy(PolicyStatus.IN_FORCE);
            PolicyCoverage coverage = TestFixtures.policyCoverage(
                    "FIRE",
                    policy.getStartDate(),
                    policy.getEndDate()
            );
            coverage.setPolicyId(policy.getPolicyId());

            when(policyRepository.findById(policy.getPolicyId())).thenReturn(
                    Mono.just(policy)
            );
            when(
                    policyCoverageRepository.findAllByPolicyId(
                            policy.getPolicyId()
                    )
            ).thenReturn(Flux.just(coverage));

            StepVerifier.create(
                            policyService.getPolicyById(
                                    policy.getPolicyId()
                            )
                    )
                    .assertNext(response -> {
                        assertEquals(1, response.coverages().size());
                        assertEquals(
                                coverage.getPolicyCoverageId(),
                                response.coverages()
                                        .get(0)
                                        .policyCoverageId()
                        );
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("getPolicyById fails when the policy is unknown")
        void getPolicyByIdMissing() {
            UUID policyId = UUID.randomUUID();

            when(policyRepository.findById(policyId)).thenReturn(
                    Mono.empty()
            );

            StepVerifier.create(policyService.getPolicyById(policyId))
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            ResourceNotFoundException.class,
                            "Policy not found with ID: " + policyId,
                            error
                    ))
                    .verify();
        }

        @Test
        @DisplayName("getPolicyByNumber maps the policy")
        void getPolicyByNumber() {
            Policy policy = TestFixtures.policy(PolicyStatus.IN_FORCE);

            when(policyRepository.findByPolicyNumber(
                    policy.getPolicyNumber()
            )).thenReturn(Mono.just(policy));
            when(
                    policyCoverageRepository.findAllByPolicyId(
                            policy.getPolicyId()
                    )
            ).thenReturn(Flux.empty());

            StepVerifier.create(
                            policyService.getPolicyByNumber(
                                    policy.getPolicyNumber()
                            )
                    )
                    .assertNext(response -> assertTrue(
                            response.coverages().isEmpty()
                    ))
                    .verifyComplete();
        }

        @Test
        @DisplayName("getPolicyByNumber fails when the policy is unknown")
        void getPolicyByNumberMissing() {
            when(policyRepository.findByPolicyNumber("NOPE")).thenReturn(
                    Mono.empty()
            );

            StepVerifier.create(policyService.getPolicyByNumber("NOPE"))
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            ResourceNotFoundException.class,
                            "Policy not found with number: NOPE",
                            error
                    ))
                    .verify();
        }

        @Test
        @DisplayName("getPoliciesByCustomerId streams every policy")
        void getPoliciesByCustomerId() {
            Policy first = TestFixtures.policy(PolicyStatus.IN_FORCE);
            Policy second = TestFixtures.policy(
                    PolicyStatus.PENDING_ISSUANCE
            );

            when(
                    policyRepository.findAllByCustomerId(
                            TestFixtures.CUSTOMER_ID
                    )
            ).thenReturn(Flux.just(first, second));
            lenient().when(
                    policyCoverageRepository.findAllByPolicyId(any())
            ).thenReturn(Flux.empty());

            StepVerifier.create(
                            policyService.getPoliciesByCustomerId(
                                    TestFixtures.CUSTOMER_ID
                            )
                    )
                    .expectNextCount(2)
                    .verifyComplete();
        }

        @Test
        @DisplayName("getPoliciesByCustomerId completes empty for unknown customers")
        void getPoliciesByCustomerIdEmpty() {
            when(
                    policyRepository.findAllByCustomerId(
                            TestFixtures.CUSTOMER_ID
                    )
            ).thenReturn(Flux.empty());

            StepVerifier.create(
                            policyService.getPoliciesByCustomerId(
                                    TestFixtures.CUSTOMER_ID
                            )
                    )
                    .verifyComplete();
        }
    }
}
