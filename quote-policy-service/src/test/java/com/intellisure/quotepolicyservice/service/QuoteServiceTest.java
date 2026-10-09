package com.intellisure.quotepolicyservice.service;

import com.intellisure.quotepolicyservice.dto.request.AssignUnderwriterRequest;
import com.intellisure.quotepolicyservice.dto.request.CreateQuoteCoverageRequest;
import com.intellisure.quotepolicyservice.dto.request.CreateQuoteRequest;
import com.intellisure.quotepolicyservice.dto.request.DeclineQuoteRequest;
import com.intellisure.quotepolicyservice.dto.request.OfferQuoteTermsRequest;
import com.intellisure.quotepolicyservice.dto.request.OfferedCoverageRequest;
import com.intellisure.quotepolicyservice.dto.request.RecordUnderwritingDecisionRequest;
import com.intellisure.quotepolicyservice.entity.Quote;
import com.intellisure.quotepolicyservice.entity.QuoteCoverage;
import com.intellisure.quotepolicyservice.entity.UnderwritingDecision;
import com.intellisure.quotepolicyservice.enums.QuoteStatus;
import com.intellisure.quotepolicyservice.enums.UnderwritingDecisionType;
import com.intellisure.quotepolicyservice.exception.AccessDeniedBusinessException;
import com.intellisure.quotepolicyservice.exception.BusinessException;
import com.intellisure.quotepolicyservice.exception.ResourceNotFoundException;
import com.intellisure.quotepolicyservice.mapper.QuoteMapper;
import com.intellisure.quotepolicyservice.repository.QuoteCoverageRepository;
import com.intellisure.quotepolicyservice.repository.QuoteRepository;
import com.intellisure.quotepolicyservice.repository.QuoteVersionRepository;
import com.intellisure.quotepolicyservice.repository.SubjectivityRepository;
import com.intellisure.quotepolicyservice.repository.UnderwritingDecisionRepository;
import com.intellisure.quotepolicyservice.security.SecurityActorService;
import com.intellisure.quotepolicyservice.service.RatingService;
import com.intellisure.quotepolicyservice.service.assignment.UnderwriterAssignmentService;
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

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("QuoteService")
class QuoteServiceTest {

    private QuoteRepository quoteRepository;
    private QuoteCoverageRepository quoteCoverageRepository;
    private R2dbcEntityTemplate entityTemplate;
    private UnderwriterAssignmentService underwriterAssignmentService;
    private UnderwritingDecisionRepository underwritingDecisionRepository;
    private SecurityActorService securityActorService;
    private QuoteVersionRepository quoteVersionRepository;
    private SubjectivityRepository subjectivityRepository;
    private RatingService ratingService;

    private QuoteService quoteService;

    @BeforeEach
    void setUp() {
        quoteRepository = mock(QuoteRepository.class);
        quoteCoverageRepository = mock(QuoteCoverageRepository.class);
        entityTemplate = mock(R2dbcEntityTemplate.class);
        underwriterAssignmentService = mock(
                UnderwriterAssignmentService.class
        );
        underwritingDecisionRepository = mock(
                UnderwritingDecisionRepository.class
        );
        securityActorService = mock(SecurityActorService.class);
        quoteVersionRepository = mock(QuoteVersionRepository.class);
        subjectivityRepository = mock(SubjectivityRepository.class);
        ratingService = mock(RatingService.class);

        quoteService = new QuoteService(
                quoteRepository,
                quoteCoverageRepository,
                entityTemplate,
                new QuoteMapper(),
                underwriterAssignmentService,
                underwritingDecisionRepository,
                securityActorService,
                quoteVersionRepository,
                subjectivityRepository,
                ratingService
        );

        lenient().when(
                securityActorService.assertCustomerAccess(any(UUID.class))
        ).thenReturn(Mono.empty());

        lenient().when(
                quoteVersionRepository.save(any())
        ).thenAnswer(
                invocation -> Mono.just(invocation.getArgument(0))
        );

        lenient().when(
                subjectivityRepository.save(any())
        ).thenAnswer(
                invocation -> Mono.just(invocation.getArgument(0))
        );

        lenient().when(
                entityTemplate.update(any(Quote.class))
        ).thenAnswer(
                invocation -> Mono.just(invocation.getArgument(0))
        );

        lenient().when(
                entityTemplate.update(any(QuoteCoverage.class))
        ).thenAnswer(
                invocation -> Mono.just(invocation.getArgument(0))
        );
    }

    private void givenCoverages(
            Quote quote,
            QuoteCoverage... coverages
    ) {
        for (QuoteCoverage coverage : coverages) {
            coverage.setQuoteId(quote.getQuoteId());
        }

        lenient().when(
                quoteCoverageRepository.findAllByQuoteId(
                        quote.getQuoteId()
                )
        ).thenReturn(Flux.just(coverages));
    }

    private void givenDefaultCoverages(Quote quote) {
        givenCoverages(
                quote,
                TestFixtures.offeredCoverage("FIRE"),
                TestFixtures.offeredCoverage("LIABILITY")
        );
    }

    private void givenOwnedByAuthenticatedCustomer() {
        lenient().when(
                securityActorService.currentCustomerId()
        ).thenReturn(Mono.just(TestFixtures.CUSTOMER_ID));
    }

    private void givenLatestDecision(UnderwritingDecisionType type) {
        lenient().when(
                underwritingDecisionRepository
                        .findFirstByQuoteIdOrderByDecidedAtDesc(
                                any()
                        )
        ).thenAnswer(
                invocation -> Mono.just(TestFixtures.decision(type))
        );
    }

    private static CreateQuoteCoverageRequest requestedCoverage(
            String code
    ) {
        return new CreateQuoteCoverageRequest(
                code,
                code + " cover",
                new BigDecimal("100000.00"),
                new BigDecimal("500.00"),
                30
        );
    }

    private static CreateQuoteRequest requestWithCoverages(
            CreateQuoteCoverageRequest... coverages
    ) {
        return new CreateQuoteRequest(
                TestFixtures.CUSTOMER_ID,
                "COMMERCIAL-PROPERTY",
                "Cover",
                "Operations",
                LocalDate.now().plusDays(1),
                List.of(coverages)
        );
    }

    @Nested
    @DisplayName("createDraftQuote")
    class CreateDraftQuote {

        @Test
        @DisplayName("rejects a policyholder creating for another customer")
        void rejectsCrossCustomerCreation() {
            when(securityActorService.currentCustomerId()).thenReturn(
                    Mono.just(TestFixtures.USER_ID)
            );

            StepVerifier.create(
                            quoteService.createDraftQuote(
                                    TestFixtures.createQuoteRequest()
                            )
                    )
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            AccessDeniedBusinessException.class,
                            containsString(
                                    "only create quotes for the "
                                            + "authenticated"
                            ),
                            error
                    ))
                    .verify();

            verify(quoteRepository, never()).save(any());
            verify(
                    entityTemplate,
                    never()
            ).insert(Quote.class);
        }

        @Test
        @DisplayName("rejects duplicate coverage codes regardless of case or padding")
        void rejectsDuplicateCoverageCodes() {
            when(securityActorService.currentCustomerId()).thenReturn(
                    Mono.just(TestFixtures.CUSTOMER_ID)
            );

            StepVerifier.create(
                            quoteService.createDraftQuote(
                                    requestWithCoverages(
                                            requestedCoverage("fire"),
                                            requestedCoverage("  FIRE  ")
                                    )
                            )
                    )
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            BusinessException.class,
                            "Duplicate coverage code: FIRE",
                            error
                    ))
                    .verify();
        }

        @Test
        @DisplayName("normalises text, generates a number and persists coverages")
        void createsDraftQuote() {
            when(securityActorService.currentCustomerId()).thenReturn(
                    Mono.just(TestFixtures.CUSTOMER_ID)
            );

            EntityTemplateStubber.stubInsert(
                    entityTemplate,
                    Quote.class
            );
            EntityTemplateStubber.stubInsert(
                    entityTemplate,
                    QuoteCoverage.class
            );

            CreateQuoteRequest request = new CreateQuoteRequest(
                    TestFixtures.CUSTOMER_ID,
                    "  COMMERCIAL-PROPERTY  ",
                    "  Fire cover  ",
                    "  Warehousing  ",
                    LocalDate.now().plusDays(2),
                    List.of(
                            requestedCoverage("fire"),
                            requestedCoverage("liability")
                    )
            );

            StepVerifier.create(
                            quoteService.createDraftQuote(request)
                    )
                    .assertNext(response -> {
                        assertNotNull(response.quoteId());
                        assertEquals(
                                TestFixtures.CUSTOMER_ID,
                                response.customerId()
                        );
                        assertEquals(
                                "COMMERCIAL-PROPERTY",
                                response.productCode()
                        );
                        assertEquals(
                                "Fire cover",
                                response.insuranceNeed()
                        );
                        assertEquals(
                                "Warehousing",
                                response.businessOperations()
                        );
                        assertEquals(
                                QuoteStatus.DRAFT,
                                response.status()
                        );
                        assertEquals(
                                LocalDate.now().plusDays(2),
                                response.requestedEffectiveDate()
                        );
                        assertNull(response.assignedUnderwriterId());
                        assertNull(response.totalPremium());
                        assertTrue(
                                response.quoteNumber()
                                        .matches(
                                                "QTE-\\d{4}-[0-9A-F]{8}"
                                        )
                        );
                        assertEquals(2, response.coverages().size());
                        assertEquals(
                                "FIRE",
                                response.coverages()
                                        .get(0)
                                        .coverageCode()
                        );
                        assertEquals(
                                "LIABILITY",
                                response.coverages()
                                        .get(1)
                                        .coverageCode()
                        );
                        assertEquals(
                                "fire cover",
                                response.coverages()
                                        .get(0)
                                        .coverageName()
                        );
                        assertNotNull(response.createdAt());
                        assertEquals(
                                response.createdAt(),
                                response.updatedAt()
                        );
                    })
                    .verifyComplete();

            verify(entityTemplate).insert(Quote.class);
            verify(entityTemplate, times(2)).insert(
                    QuoteCoverage.class
            );
        }

        @Test
        @DisplayName("generated quote numbers are unique per quote")
        void generatesUniqueQuoteNumbers() {
            when(securityActorService.currentCustomerId()).thenReturn(
                    Mono.just(TestFixtures.CUSTOMER_ID)
            );

            EntityTemplateStubber.stubInsert(
                    entityTemplate,
                    Quote.class
            );
            EntityTemplateStubber.stubInsert(
                    entityTemplate,
                    QuoteCoverage.class
            );

            CreateQuoteRequest request = requestWithCoverages(
                    requestedCoverage("fire")
            );

            String first = quoteService.createDraftQuote(request)
                    .block()
                    .quoteNumber();

            String second = quoteService.createDraftQuote(request)
                    .block()
                    .quoteNumber();

            assertTrue(
                    first.matches("QTE-\\d{4}-[0-9A-F]{8}")
            );
            assertTrue(
                    second.matches("QTE-\\d{4}-[0-9A-F]{8}")
            );
            assertTrue(!first.equals(second));
        }
    }

    @Nested
    @DisplayName("submitQuote")
    class SubmitQuote {

        @Test
        @DisplayName("fails with 404 semantics when the quote is unknown")
        void failsWhenQuoteMissing() {
            when(quoteRepository.findById(any(UUID.class))).thenReturn(
                    Mono.empty()
            );

            StepVerifier.create(
                            quoteService.submitQuote(UUID.randomUUID())
                    )
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            ResourceNotFoundException.class,
                            containsString("Quote not found with ID:"),
                            error
                    ))
                    .verify();
        }

        @Test
        @DisplayName("rejects a customer that does not own the quote")
        void rejectsNonOwner() {
            Quote quote = TestFixtures.quote(QuoteStatus.DRAFT);

            when(quoteRepository.findById(quote.getQuoteId())).thenReturn(
                    Mono.just(quote)
            );
            when(securityActorService.currentCustomerId()).thenReturn(
                    Mono.just(TestFixtures.USER_ID)
            );

            StepVerifier.create(
                            quoteService.submitQuote(quote.getQuoteId())
                    )
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            AccessDeniedBusinessException.class,
                            "The authenticated customer does not own "
                                    + "this quote",
                            error
                    ))
                    .verify();
        }

        @Test
        @DisplayName("only a DRAFT quote can be submitted")
        void rejectsNonDraftQuote() {
            Quote quote = TestFixtures.quote(QuoteStatus.ACCEPTED);

            when(quoteRepository.findById(quote.getQuoteId())).thenReturn(
                    Mono.just(quote)
            );
            givenOwnedByAuthenticatedCustomer();
            givenDefaultCoverages(quote);

            StepVerifier.create(
                            quoteService.submitQuote(quote.getQuoteId())
                    )
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            BusinessException.class,
                            "Only a DRAFT quote can be submitted. "
                                    + "Current status: ACCEPTED",
                            error
                    ))
                    .verify();

            verify(entityTemplate, never()).update(any(Quote.class));
        }

        @Test
        @DisplayName("stays SUBMITTED when the underwriter pool is empty")
        void staysSubmittedWhenNoUnderwriterAvailable() {
            Quote quote = TestFixtures.quote(QuoteStatus.DRAFT);

            when(quoteRepository.findById(quote.getQuoteId())).thenReturn(
                    Mono.just(quote)
            );
            givenOwnedByAuthenticatedCustomer();
            givenDefaultCoverages(quote);
            when(
                    underwriterAssignmentService
                            .selectLeastLoadedUnderwriter()
            ).thenReturn(Mono.empty());

            StepVerifier.create(
                            quoteService.submitQuote(quote.getQuoteId())
                    )
                    .assertNext(response -> {
                        assertEquals(
                                QuoteStatus.SUBMITTED,
                                response.status()
                        );
                        assertNotNull(response.submittedAt());
                        assertNull(response.assignedUnderwriterId());
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("moves to IN_REVIEW and assigns the selected underwriter")
        void assignsUnderwriter() {
            Quote quote = TestFixtures.quote(QuoteStatus.DRAFT);

            when(quoteRepository.findById(quote.getQuoteId())).thenReturn(
                    Mono.just(quote)
            );
            givenOwnedByAuthenticatedCustomer();
            givenDefaultCoverages(quote);
            when(
                    underwriterAssignmentService
                            .selectLeastLoadedUnderwriter()
            ).thenReturn(Mono.just(TestFixtures.UNDERWRITER_ID));

            StepVerifier.create(
                            quoteService.submitQuote(quote.getQuoteId())
                    )
                    .assertNext(response -> {
                        assertEquals(
                                QuoteStatus.IN_REVIEW,
                                response.status()
                        );
                        assertEquals(
                                TestFixtures.UNDERWRITER_ID,
                                response.assignedUnderwriterId()
                        );
                        assertNotNull(response.submittedAt());
                    })
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("reassignUnderwriter")
    class ReassignUnderwriter {

        @Test
        @DisplayName("fails when the quote does not exist")
        void failsWhenQuoteMissing() {
            when(quoteRepository.findById(any(UUID.class))).thenReturn(
                    Mono.empty()
            );

            StepVerifier.create(
                            quoteService.reassignUnderwriter(
                                    UUID.randomUUID(),
                                    new AssignUnderwriterRequest(
                                            TestFixtures.OTHER_UNDERWRITER_ID
                                    )
                            )
                    )
                    .expectError(ResourceNotFoundException.class)
                    .verify();
        }

        @Test
        @DisplayName("reassignment is not allowed once the quote is accepted")
        void rejectsTerminalStatus() {
            Quote quote = TestFixtures.acceptedQuote();

            when(quoteRepository.findById(quote.getQuoteId())).thenReturn(
                    Mono.just(quote)
            );

            StepVerifier.create(
                            quoteService.reassignUnderwriter(
                                    quote.getQuoteId(),
                                    new AssignUnderwriterRequest(
                                            TestFixtures.OTHER_UNDERWRITER_ID
                                    )
                            )
                    )
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            BusinessException.class,
                            containsString(
                                    "not allowed while the quote is in "
                                            + "status ACCEPTED"
                            ),
                            error
                    ))
                    .verify();
        }

        @Test
        @DisplayName("SUBMITTED is promoted to IN_REVIEW on reassignment")
        void promotesSubmittedQuote() {
            Quote quote = TestFixtures.quote(QuoteStatus.SUBMITTED);

            when(quoteRepository.findById(quote.getQuoteId())).thenReturn(
                    Mono.just(quote)
            );
            givenDefaultCoverages(quote);

            StepVerifier.create(
                            quoteService.reassignUnderwriter(
                                    quote.getQuoteId(),
                                    new AssignUnderwriterRequest(
                                            TestFixtures.OTHER_UNDERWRITER_ID
                                    )
                            )
                    )
                    .assertNext(response -> {
                        assertEquals(
                                QuoteStatus.IN_REVIEW,
                                response.status()
                        );
                        assertEquals(
                                TestFixtures.OTHER_UNDERWRITER_ID,
                                response.assignedUnderwriterId()
                        );
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("IN_REVIEW, NEEDS_INFORMATION and RISK_ASSESSMENT are allowed and keep their status")
        void allowsRiskStatuses() {
            for (QuoteStatus status : List.of(
                    QuoteStatus.IN_REVIEW,
                    QuoteStatus.NEEDS_INFORMATION,
                    QuoteStatus.RISK_ASSESSMENT
            )) {
                Quote quote = TestFixtures.quote(status);

                when(
                        quoteRepository.findById(quote.getQuoteId())
                ).thenReturn(Mono.just(quote));
                givenDefaultCoverages(quote);

                StepVerifier.create(
                                quoteService.reassignUnderwriter(
                                        quote.getQuoteId(),
                                        new AssignUnderwriterRequest(
                                                TestFixtures
                                                        .OTHER_UNDERWRITER_ID
                                        )
                                )
                        )
                        .assertNext(response -> {
                            assertEquals(status, response.status());
                            assertEquals(
                                    TestFixtures.OTHER_UNDERWRITER_ID,
                                    response.assignedUnderwriterId()
                            );
                        })
                        .verifyComplete();
            }
        }
    }

    @Nested
    @DisplayName("read operations")
    class ReadOperations {

        @Test
        @DisplayName("getQuoteById returns the mapped quote with coverages")
        void getQuoteById() {
            Quote quote = TestFixtures.quote(QuoteStatus.DRAFT);

            when(quoteRepository.findById(quote.getQuoteId())).thenReturn(
                    Mono.just(quote)
            );
            givenDefaultCoverages(quote);

            StepVerifier.create(
                            quoteService.getQuoteById(quote.getQuoteId())
                    )
                    .assertNext(response -> {
                        assertEquals(2, response.coverages().size());
                        assertEquals(
                                quote.getQuoteNumber(),
                                response.quoteNumber()
                        );
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("getQuoteById fails for an unknown quote")
        void getQuoteByIdMissing() {
            UUID quoteId = UUID.randomUUID();

            when(quoteRepository.findById(quoteId)).thenReturn(
                    Mono.empty()
            );

            StepVerifier.create(quoteService.getQuoteById(quoteId))
                    .expectError(ResourceNotFoundException.class)
                    .verify();
        }

        @Test
        @DisplayName("getQuoteById maps an empty coverage list")
        void getQuoteByIdWithoutCoverages() {
            Quote quote = TestFixtures.quote(QuoteStatus.DRAFT);

            when(quoteRepository.findById(quote.getQuoteId())).thenReturn(
                    Mono.just(quote)
            );
            when(
                    quoteCoverageRepository.findAllByQuoteId(
                            quote.getQuoteId()
                    )
            ).thenReturn(Flux.empty());

            StepVerifier.create(
                            quoteService.getQuoteById(quote.getQuoteId())
                    )
                    .assertNext(response -> assertTrue(
                            response.coverages().isEmpty()
                    ))
                    .verifyComplete();
        }

        @Test
        @DisplayName("getQuoteByNumber fails with a not found error")
        void getQuoteByNumberMissing() {
            when(quoteRepository.findByQuoteNumber("QTE-2026-MISSING"))
                    .thenReturn(Mono.empty());

            StepVerifier.create(
                            quoteService.getQuoteByNumber(
                                    "QTE-2026-MISSING"
                            )
                    )
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            ResourceNotFoundException.class,
                            "Quote not found with number: QTE-2026-MISSING",
                            error
                    ))
                    .verify();
        }

        @Test
        @DisplayName("getQuoteByNumber maps the quote when it exists")
        void getQuoteByNumberFound() {
            Quote quote = TestFixtures.quote(QuoteStatus.DRAFT);

            when(quoteRepository.findByQuoteNumber(quote.getQuoteNumber()))
                    .thenReturn(Mono.just(quote));
            givenDefaultCoverages(quote);

            StepVerifier.create(
                            quoteService.getQuoteByNumber(
                                    quote.getQuoteNumber()
                            )
                    )
                    .assertNext(response -> assertEquals(
                            quote.getQuoteId(),
                            response.quoteId()
                    ))
                    .verifyComplete();
        }

        @Test
        @DisplayName("getQuotesByCustomerId streams every quote of the customer")
        void getQuotesByCustomerId() {
            Quote first = TestFixtures.quote(QuoteStatus.DRAFT);
            Quote second = TestFixtures.quote(QuoteStatus.QUOTED);

            when(
                    quoteRepository.findAllByCustomerId(
                            TestFixtures.CUSTOMER_ID
                    )
            ).thenReturn(Flux.just(first, second));
            givenDefaultCoverages(first);
            givenDefaultCoverages(second);

            StepVerifier.create(
                            quoteService.getQuotesByCustomerId(
                                    TestFixtures.CUSTOMER_ID
                            )
                    )
                    .expectNextCount(2)
                    .verifyComplete();
        }

        @Test
        @DisplayName("getQuotesByCustomerId completes empty when the customer has no quotes")
        void getQuotesByCustomerIdEmpty() {
            when(
                    quoteRepository.findAllByCustomerId(
                            TestFixtures.CUSTOMER_ID
                    )
            ).thenReturn(Flux.empty());

            StepVerifier.create(
                            quoteService.getQuotesByCustomerId(
                                    TestFixtures.CUSTOMER_ID
                            )
                    )
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("offerQuoteTerms")
    class OfferQuoteTerms {

        private Quote quote;

        @BeforeEach
        void prepare() {
            quote = TestFixtures.inReviewQuote();
        }

        private void givenAssignedUnderwriter() {
            when(securityActorService.currentUserId()).thenReturn(
                    Mono.just(TestFixtures.UNDERWRITER_ID)
            );
        }

        @Test
        @DisplayName("rejects duplicate offered coverage codes")
        void rejectsDuplicateOfferedCodes() {
            givenAssignedUnderwriter();

            OfferedCoverageRequest first = TestFixtures
                    .offerTermsRequest("FIRE")
                    .coverages()
                    .get(0);

            OfferQuoteTermsRequest request = new OfferQuoteTermsRequest(
                    LocalDateTime.now().plusDays(5),
                    List.of(
                            first,
                            TestFixtures.offerTermsRequest(" fire ")
                                    .coverages()
                                    .get(0)
                    )
            );

            assertEquals(
                    "Duplicate offered coverage code: FIRE",
                    assertThrows(
                            BusinessException.class,
                            () -> quoteService.offerQuoteTerms(
                                    quote.getQuoteId(),
                                    request
                            )
                    ).getMessage()
            );

            verify(quoteRepository, never()).findById(any(UUID.class));
        }

        @Test
        @DisplayName("rejects an underwriter that is not assigned to the quote")
        void rejectsUnassignedUnderwriter() {
            when(securityActorService.currentUserId()).thenReturn(
                    Mono.just(TestFixtures.OTHER_UNDERWRITER_ID)
            );
            when(quoteRepository.findById(quote.getQuoteId())).thenReturn(
                    Mono.just(quote)
            );

            StepVerifier.create(
                            quoteService.offerQuoteTerms(
                                    quote.getQuoteId(),
                                    TestFixtures.offerTermsRequest("FIRE")
                            )
                    )
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            AccessDeniedBusinessException.class,
                            "The authenticated underwriter is not "
                                    + "assigned to this quote",
                            error
                    ))
                    .verify();
        }

        @Test
        @DisplayName("terms can only be offered while IN_REVIEW")
        void rejectsWrongStatus() {
            quote.setStatus(QuoteStatus.QUOTED);

            givenAssignedUnderwriter();
            when(quoteRepository.findById(quote.getQuoteId())).thenReturn(
                    Mono.just(quote)
            );

            StepVerifier.create(
                            quoteService.offerQuoteTerms(
                                    quote.getQuoteId(),
                                    TestFixtures.offerTermsRequest("FIRE")
                            )
                    )
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            BusinessException.class,
                            "Terms can only be offered while the quote "
                                    + "is IN_REVIEW. Current status: QUOTED",
                            error
                    ))
                    .verify();
        }

        @Test
        @DisplayName("requires an existing underwriting decision")
        void requiresDecision() {
            givenAssignedUnderwriter();
            when(quoteRepository.findById(quote.getQuoteId())).thenReturn(
                    Mono.just(quote)
            );
            when(
                    underwritingDecisionRepository
                            .findFirstByQuoteIdOrderByDecidedAtDesc(
                                    quote.getQuoteId()
                            )
            ).thenReturn(Mono.empty());

            StepVerifier.create(
                            quoteService.offerQuoteTerms(
                                    quote.getQuoteId(),
                                    TestFixtures.offerTermsRequest("FIRE")
                            )
                    )
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            BusinessException.class,
                            containsString(
                                    "An approved underwriting decision "
                                            + "is required"
                            ),
                            error
                    ))
                    .verify();
        }

        @Test
        @DisplayName("the latest decision must be APPROVED or APPROVED_WITH_MODIFIED_TERMS")
        void rejectsDeclinedDecision() {
            givenAssignedUnderwriter();
            when(quoteRepository.findById(quote.getQuoteId())).thenReturn(
                    Mono.just(quote)
            );
            when(
                    underwritingDecisionRepository
                            .findFirstByQuoteIdOrderByDecidedAtDesc(
                                    quote.getQuoteId()
                            )
            ).thenReturn(
                    Mono.just(
                            TestFixtures.decision(
                                    UnderwritingDecisionType.DECLINED
                            )
                    )
            );

            StepVerifier.create(
                            quoteService.offerQuoteTerms(
                                    quote.getQuoteId(),
                                    TestFixtures.offerTermsRequest("FIRE")
                            )
                    )
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            BusinessException.class,
                            containsString(
                                    "Current decision: DECLINED"
                            ),
                            error
                    ))
                    .verify();
        }

        @Test
        @DisplayName("APPROVED_WITH_MODIFIED_TERMS counts as approved")
        void acceptsModifiedApproval() {
            givenAssignedUnderwriter();
            when(quoteRepository.findById(quote.getQuoteId())).thenReturn(
                    Mono.just(quote)
            );
            givenDefaultCoverages(quote);
            givenLatestDecision(
                    UnderwritingDecisionType.APPROVED_WITH_MODIFIED_TERMS
            );

            StepVerifier.create(
                            quoteService.offerQuoteTerms(
                                    quote.getQuoteId(),
                                    TestFixtures.offerTermsRequest(
                                            "FIRE",
                                            "LIABILITY"
                                    )
                            )
                    )
                    .assertNext(response -> {
                        assertEquals(
                                QuoteStatus.QUOTED,
                                response.status()
                        );
                        assertEquals(
                                new BigDecimal("1500.00"),
                                response.totalPremium()
                        );
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("fails when the quote has no persisted coverages")
        void failsWhenNoCoverages() {
            givenAssignedUnderwriter();
            when(quoteRepository.findById(quote.getQuoteId())).thenReturn(
                    Mono.just(quote)
            );
            givenLatestDecision(UnderwritingDecisionType.APPROVED);
            when(
                    quoteCoverageRepository.findAllByQuoteId(
                            quote.getQuoteId()
                    )
            ).thenReturn(Flux.empty());

            StepVerifier.create(
                            quoteService.offerQuoteTerms(
                                    quote.getQuoteId(),
                                    TestFixtures.offerTermsRequest("FIRE")
                            )
                    )
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            BusinessException.class,
                            "The quote does not contain any requested "
                                    + "coverages",
                            error
                    ))
                    .verify();
        }

        @Test
        @DisplayName("fails when offered coverages do not cover every requested coverage")
        void failsWhenOfferedCoveragesAreIncomplete() {
            givenAssignedUnderwriter();
            when(quoteRepository.findById(quote.getQuoteId())).thenReturn(
                    Mono.just(quote)
            );
            givenLatestDecision(UnderwritingDecisionType.APPROVED);
            givenDefaultCoverages(quote);

            StepVerifier.create(
                            quoteService.offerQuoteTerms(
                                    quote.getQuoteId(),
                                    TestFixtures.offerTermsRequest("FIRE")
                            )
                    )
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            BusinessException.class,
                            allOf(
                                    containsString("Missing: [LIABILITY]"),
                                    containsString("Unknown: []")
                            ),
                            error
                    ))
                    .verify();
        }

        @Test
        @DisplayName("fails when an unknown coverage is offered")
        void failsWhenOfferedCoverageIsUnknown() {
            givenAssignedUnderwriter();
            when(quoteRepository.findById(quote.getQuoteId())).thenReturn(
                    Mono.just(quote)
            );
            givenLatestDecision(UnderwritingDecisionType.APPROVED);
            givenDefaultCoverages(quote);

            StepVerifier.create(
                            quoteService.offerQuoteTerms(
                                    quote.getQuoteId(),
                                    TestFixtures.offerTermsRequest(
                                            "FIRE",
                                            "LIABILITY",
                                            "FLOOD"
                                    )
                            )
                    )
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            BusinessException.class,
                            containsString("Unknown: [FLOOD]"),
                            error
                    ))
                    .verify();
        }

        @Test
        @DisplayName("applies offered terms, trims free text and quotes the quote")
        void appliesOfferedTerms() {
            givenAssignedUnderwriter();
            when(quoteRepository.findById(quote.getQuoteId())).thenReturn(
                    Mono.just(quote)
            );
            givenLatestDecision(UnderwritingDecisionType.APPROVED);
            givenDefaultCoverages(quote);

            LocalDateTime expiresAt = LocalDateTime.now().plusDays(10);

            OfferQuoteTermsRequest request = new OfferQuoteTermsRequest(
                    expiresAt,
                    List.of(
                            new OfferedCoverageRequest(
                                    "fire",
                                    new BigDecimal("90000.00"),
                                    new BigDecimal("750.00"),
                                    new BigDecimal("700.00"),
                                    "  Survey required  ",
                                    "     ",
                                    45
                            ),
                            new OfferedCoverageRequest(
                                    "  liability ",
                                    new BigDecimal("450000.00"),
                                    new BigDecimal("1500.00"),
                                    new BigDecimal("800.00"),
                                    null,
                                    "Flood",
                                    null
                            )
                    )
            );

            StepVerifier.create(
                            quoteService.offerQuoteTerms(
                                    quote.getQuoteId(),
                                    request
                            )
                    )
                    .assertNext(response -> {
                        assertEquals(
                                QuoteStatus.QUOTED,
                                response.status()
                        );
                        assertEquals(
                                expiresAt,
                                response.quoteExpiresAt()
                        );
                        assertNotNull(response.quotedAt());
                        assertEquals(
                                new BigDecimal("1500.00"),
                                response.totalPremium()
                        );
                        assertEquals(
                                "Survey required",
                                response.coverages()
                                        .get(0)
                                        .conditions()
                        );
                        assertNull(
                                response.coverages()
                                        .get(0)
                                        .exclusions()
                        );
                        assertEquals(
                                45,
                                response.coverages()
                                        .get(0)
                                        .waitingPeriodDays()
                        );
                        assertEquals(
                                "Flood",
                                response.coverages()
                                        .get(1)
                                        .exclusions()
                        );
                        assertNull(
                                response.coverages()
                                        .get(1)
                                        .conditions()
                        );
                    })
                    .verifyComplete();

            verify(entityTemplate, times(2)).update(
                    any(QuoteCoverage.class)
            );
        }
    }

    @Nested
    @DisplayName("acceptQuote")
    class AcceptQuote {

        private Quote quote;

        @BeforeEach
        void prepare() {
            quote = TestFixtures.quotedQuote();

            when(quoteRepository.findById(quote.getQuoteId())).thenReturn(
                    Mono.just(quote)
            );
            givenOwnedByAuthenticatedCustomer();
            when(securityActorService.currentUserId()).thenReturn(
                    Mono.just(TestFixtures.USER_ID)
            );
        }

        @Test
        @DisplayName("only a QUOTED quote can be accepted")
        void rejectsWrongStatus() {
            quote.setStatus(QuoteStatus.IN_REVIEW);

            StepVerifier.create(
                            quoteService.acceptQuote(quote.getQuoteId())
                    )
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            BusinessException.class,
                            "Only a QUOTED quote can be accepted. "
                                    + "Current status: IN_REVIEW",
                            error
                    ))
                    .verify();
        }

        @Test
        @DisplayName("rejects a quote without an expiration date")
        void rejectsMissingExpiration() {
            quote.setQuoteExpiresAt(null);

            StepVerifier.create(
                            quoteService.acceptQuote(quote.getQuoteId())
                    )
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            BusinessException.class,
                            "The quote does not have an expiration date",
                            error
                    ))
                    .verify();
        }

        @Test
        @DisplayName("rejects an already expired quote")
        void rejectsExpiredQuote() {
            quote.setQuoteExpiresAt(
                    LocalDateTime.now().minusMinutes(1)
            );

            StepVerifier.create(
                            quoteService.acceptQuote(quote.getQuoteId())
                    )
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            BusinessException.class,
                            "The quote has expired and can no longer be "
                                    + "accepted or declined",
                            error
                    ))
                    .verify();
        }

        @Test
        @DisplayName("accepts a quote that expires within a second")
        void acceptsQuoteExpiringSoon() {
            quote.setQuoteExpiresAt(
                    LocalDateTime.now().plusSeconds(1)
            );
            givenDefaultCoverages(quote);

            StepVerifier.create(
                            quoteService.acceptQuote(quote.getQuoteId())
                    )
                    .assertNext(response -> assertEquals(
                            QuoteStatus.ACCEPTED,
                            response.status()
                    ))
                    .verifyComplete();
        }

        @Test
        @DisplayName("rejects a quote without a final premium")
        void rejectsMissingPremium() {
            quote.setTotalPremium(null);

            StepVerifier.create(
                            quoteService.acceptQuote(quote.getQuoteId())
                    )
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            BusinessException.class,
                            "The quote does not have a final premium",
                            error
                    ))
                    .verify();
        }

        @Test
        @DisplayName("records the accepting user and the acceptance timestamp")
        void acceptsQuote() {
            givenDefaultCoverages(quote);

            StepVerifier.create(
                            quoteService.acceptQuote(quote.getQuoteId())
                    )
                    .assertNext(response -> {
                        assertEquals(
                                QuoteStatus.ACCEPTED,
                                response.status()
                        );
                        assertEquals(
                                TestFixtures.USER_ID,
                                response.acceptedByUserId()
                        );
                        assertNotNull(response.acceptedAt());
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("a customer cannot accept another customer's quote")
        void rejectsCrossCustomerAcceptance() {
            when(securityActorService.currentCustomerId()).thenReturn(
                    Mono.just(TestFixtures.USER_ID)
            );

            StepVerifier.create(
                            quoteService.acceptQuote(quote.getQuoteId())
                    )
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            AccessDeniedBusinessException.class,
                            "The authenticated customer does not own "
                                    + "this quote",
                            error
                    ))
                    .verify();
        }
    }

    @Nested
    @DisplayName("declineQuoteByCustomer")
    class DeclineQuote {

        private Quote quote;

        @BeforeEach
        void prepare() {
            quote = TestFixtures.quotedQuote();

            when(quoteRepository.findById(quote.getQuoteId())).thenReturn(
                    Mono.just(quote)
            );
            givenOwnedByAuthenticatedCustomer();
        }

        @Test
        @DisplayName("only a QUOTED quote can be declined by the customer")
        void rejectsWrongStatus() {
            quote.setStatus(QuoteStatus.DRAFT);

            StepVerifier.create(
                            quoteService.declineQuoteByCustomer(
                                    quote.getQuoteId(),
                                    new DeclineQuoteRequest("Too dear")
                            )
                    )
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            BusinessException.class,
                            "Only a QUOTED quote can be declined by the "
                                    + "customer. Current status: DRAFT",
                            error
                    ))
                    .verify();
        }

        @Test
        @DisplayName("an expired quote cannot be declined")
        void rejectsExpiredQuote() {
            quote.setQuoteExpiresAt(LocalDateTime.now().minusDays(1));

            StepVerifier.create(
                            quoteService.declineQuoteByCustomer(
                                    quote.getQuoteId(),
                                    new DeclineQuoteRequest("Too dear")
                            )
                    )
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            BusinessException.class,
                            "The quote has expired and can no longer be "
                                    + "accepted or declined",
                            error
                    ))
                    .verify();
        }

        @Test
        @DisplayName("stores the trimmed decline reason")
        void declinesQuote() {
            givenDefaultCoverages(quote);

            StepVerifier.create(
                            quoteService.declineQuoteByCustomer(
                                    quote.getQuoteId(),
                                    new DeclineQuoteRequest(
                                            "   Price too high   "
                                    )
                            )
                    )
                    .assertNext(response -> {
                        assertEquals(
                                QuoteStatus.DECLINED_BY_CUSTOMER,
                                response.status()
                        );
                        assertEquals(
                                "Price too high",
                                response.declineReason()
                        );
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("a customer cannot decline another customer's quote")
        void rejectsCrossCustomerDecline() {
            when(securityActorService.currentCustomerId()).thenReturn(
                    Mono.just(TestFixtures.USER_ID)
            );

            StepVerifier.create(
                            quoteService.declineQuoteByCustomer(
                                    quote.getQuoteId(),
                                    new DeclineQuoteRequest("Too dear")
                            )
                    )
                    .expectError(AccessDeniedBusinessException.class)
                    .verify();
        }
    }

    @Nested
    @DisplayName("expireQuotedOffers")
    class ExpireQuotedOffers {

        @Test
        @DisplayName("expires every QUOTED offer whose expiry has passed")
        void expiresOffers() {
            Quote first = TestFixtures.quotedQuote();
            Quote second = TestFixtures.quotedQuote();

            when(
                    quoteRepository
                            .findAllByStatusAndQuoteExpiresAtLessThanEqual(
                                    eq(QuoteStatus.QUOTED),
                                    any(LocalDateTime.class)
                            )
            ).thenReturn(Flux.just(first, second));

            StepVerifier.create(quoteService.expireQuotedOffers())
                    .expectNext(2L)
                    .verifyComplete();

            assertEquals(QuoteStatus.EXPIRED, first.getStatus());
            assertEquals(QuoteStatus.EXPIRED, second.getStatus());
        }

        @Test
        @DisplayName("returns zero when nothing has expired")
        void expiresNothing() {
            when(
                    quoteRepository
                            .findAllByStatusAndQuoteExpiresAtLessThanEqual(
                                    eq(QuoteStatus.QUOTED),
                                    any(LocalDateTime.class)
                            )
            ).thenReturn(Flux.empty());

            StepVerifier.create(quoteService.expireQuotedOffers())
                    .expectNext(0L)
                    .verifyComplete();
        }

        @Test
        @DisplayName("only QUOTED quotes are considered")
        void queriesOnlyQuotedStatus() {
            when(
                    quoteRepository
                            .findAllByStatusAndQuoteExpiresAtLessThanEqual(
                                    eq(QuoteStatus.QUOTED),
                                    any(LocalDateTime.class)
                            )
            ).thenReturn(Flux.empty());

            StepVerifier.create(quoteService.expireQuotedOffers())
                    .expectNext(0L)
                    .verifyComplete();

            verify(quoteRepository)
                    .findAllByStatusAndQuoteExpiresAtLessThanEqual(
                            eq(QuoteStatus.QUOTED),
                            any(LocalDateTime.class)
                    );
        }
    }

    @Nested
    @DisplayName("recordUnderwritingDecision")
    class RecordDecision {

        private Quote quote;

        @BeforeEach
        void prepare() {
            quote = TestFixtures.inReviewQuote();

            when(quoteRepository.findById(quote.getQuoteId())).thenReturn(
                    Mono.just(quote)
            );
            when(securityActorService.currentUserId()).thenReturn(
                    Mono.just(TestFixtures.UNDERWRITER_ID)
            );
            EntityTemplateStubber.stubInsert(
                    entityTemplate,
                    UnderwritingDecision.class
            );
        }

        @Test
        @DisplayName("fails when the quote does not exist")
        void failsWhenQuoteMissing() {
            when(quoteRepository.findById(any(UUID.class))).thenReturn(
                    Mono.empty()
            );

            StepVerifier.create(
                            quoteService.recordUnderwritingDecision(
                                    quote.getQuoteId(),
                                    TestFixtures.decisionRequest(
                                            UnderwritingDecisionType
                                                    .APPROVED
                                    )
                            )
                    )
                    .expectError(ResourceNotFoundException.class)
                    .verify();
        }

        @Test
        @DisplayName("rejects an underwriter that is not assigned to the quote")
        void rejectsUnassignedUnderwriter() {
            when(securityActorService.currentUserId()).thenReturn(
                    Mono.just(TestFixtures.OTHER_UNDERWRITER_ID)
            );

            StepVerifier.create(
                            quoteService.recordUnderwritingDecision(
                                    quote.getQuoteId(),
                                    TestFixtures.decisionRequest(
                                            UnderwritingDecisionType
                                                    .APPROVED
                                    )
                            )
                    )
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            AccessDeniedBusinessException.class,
                            "The authenticated underwriter is not "
                                    + "assigned to this quote",
                            error
                    ))
                    .verify();
        }

        @Test
        @DisplayName("a decision can only be recorded while IN_REVIEW")
        void rejectsWrongStatus() {
            quote.setStatus(QuoteStatus.QUOTED);

            StepVerifier.create(
                            quoteService.recordUnderwritingDecision(
                                    quote.getQuoteId(),
                                    TestFixtures.decisionRequest(
                                            UnderwritingDecisionType
                                                    .APPROVED
                                    )
                            )
                    )
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            BusinessException.class,
                            "An underwriting decision can only be recorded "
                                    + "while the quote is IN_REVIEW. Current "
                                    + "status: QUOTED",
                            error
                    ))
                    .verify();

            verify(
                    entityTemplate,
                    never()
            ).insert(UnderwritingDecision.class);
        }

        @Test
        @DisplayName("MORE_INFORMATION_REQUIRED moves the quote to NEEDS_INFORMATION and clears any decline reason")
        void moreInformationRequired() {
            quote.setDeclineReason("previous reason");

            StepVerifier.create(
                            quoteService.recordUnderwritingDecision(
                                    quote.getQuoteId(),
                                    TestFixtures.decisionRequest(
                                            UnderwritingDecisionType
                                                    .MORE_INFORMATION_REQUIRED
                                    )
                            )
                    )
                    .assertNext(response -> {
                        assertEquals(
                                UnderwritingDecisionType
                                        .MORE_INFORMATION_REQUIRED,
                                response.decision()
                        );
                        assertEquals(
                                "Inspected the risk",
                                response.decisionReason()
                        );
                        assertEquals(
                                "SENIOR",
                                response.authorityLevel()
                        );
                        assertEquals(
                                "Monthly reporting",
                                response.conditions()
                        );
                        assertEquals(
                                quote.getQuoteId(),
                                response.quoteId()
                        );
                        assertNotNull(response.underwritingDecisionId());
                    })
                    .verifyComplete();

            assertEquals(
                    QuoteStatus.NEEDS_INFORMATION,
                    quote.getStatus()
            );
            assertNull(quote.getDeclineReason());
        }

        @Test
        @DisplayName("DECLINED moves the quote to DECLINED_BY_INSURER and stores the reason")
        void declined() {
            StepVerifier.create(
                            quoteService.recordUnderwritingDecision(
                                    quote.getQuoteId(),
                                    TestFixtures.decisionRequest(
                                            UnderwritingDecisionType
                                                    .DECLINED
                                    )
                            )
                    )
                    .assertNext(response -> assertEquals(
                            UnderwritingDecisionType.DECLINED,
                            response.decision()
                    ))
                    .verifyComplete();

            assertEquals(
                    QuoteStatus.DECLINED_BY_INSURER,
                    quote.getStatus()
            );
            assertEquals(
                    "Inspected the risk",
                    quote.getDeclineReason()
            );
        }

        @Test
        @DisplayName("APPROVED, APPROVED_WITH_MODIFIED_TERMS and REFERRED keep the quote IN_REVIEW")
        void keepsInReviewForNonTerminalDecisions() {
            for (UnderwritingDecisionType type : List.of(
                    UnderwritingDecisionType.APPROVED,
                    UnderwritingDecisionType
                            .APPROVED_WITH_MODIFIED_TERMS,
                    UnderwritingDecisionType.REFERRED
            )) {
                Quote inReview = TestFixtures.inReviewQuote();

                when(
                        quoteRepository.findById(inReview.getQuoteId())
                ).thenReturn(Mono.just(inReview));

                StepVerifier.create(
                                quoteService.recordUnderwritingDecision(
                                        inReview.getQuoteId(),
                                        TestFixtures.decisionRequest(type)
                                )
                        )
                        .assertNext(response -> {
                            assertEquals(type, response.decision());
                            assertEquals(
                                    TestFixtures.UNDERWRITER_ID,
                                    response.underwriterId()
                            );
                        })
                        .verifyComplete();

                assertEquals(
                        QuoteStatus.IN_REVIEW,
                        inReview.getStatus()
                );
            }
        }

        @Test
        @DisplayName("blank authority level and conditions are stored as null")
        void normalisesBlankOptionalText() {
            StepVerifier.create(
                            quoteService.recordUnderwritingDecision(
                                    quote.getQuoteId(),
                                    new RecordUnderwritingDecisionRequest(
                                            UnderwritingDecisionType
                                                    .APPROVED,
                                            "Standard risk",
                                            "   ",
                                            ""
                                    )
                            )
                    )
                    .assertNext(response -> {
                        assertNull(response.authorityLevel());
                        assertNull(response.conditions());
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("a quote without an assigned underwriter cannot receive a decision")
        void failsWhenUnderwriterMissing() {
            Quote unassigned = TestFixtures.quote(QuoteStatus.IN_REVIEW);

            when(
                    quoteRepository.findById(unassigned.getQuoteId())
            ).thenReturn(Mono.just(unassigned));
            when(securityActorService.currentUserId()).thenReturn(
                    Mono.just(TestFixtures.USER_ID)
            );

            StepVerifier.create(
                            quoteService.recordUnderwritingDecision(
                                    unassigned.getQuoteId(),
                                    TestFixtures.decisionRequest(
                                            UnderwritingDecisionType
                                                    .APPROVED
                                    )
                            )
                    )
                    .expectError(AccessDeniedBusinessException.class)
                    .verify();
        }
    }

    @Nested
    @DisplayName("getUnderwritingDecisionHistory")
    class DecisionHistory {

        @Test
        @DisplayName("fails fast when the quote does not exist")
        void failsForUnknownQuote() {
            when(quoteRepository.findById(any(UUID.class))).thenReturn(
                    Mono.empty()
            );
            when(
                    underwritingDecisionRepository.findAllByQuoteId(any())
            ).thenReturn(Flux.empty());

            StepVerifier.create(
                            quoteService.getUnderwritingDecisionHistory(
                                    UUID.randomUUID()
                            )
                    )
                    .expectError(ResourceNotFoundException.class)
                    .verify();
        }

        @Test
        @DisplayName("returns the decisions newest first")
        void returnsNewestFirst() {
            Quote quote = TestFixtures.inReviewQuote();

            UnderwritingDecision oldest = TestFixtures.decision(
                    UnderwritingDecisionType.MORE_INFORMATION_REQUIRED
            );
            oldest.setDecidedAt(TestFixtures.NOW.minusDays(2));

            UnderwritingDecision middle = TestFixtures.decision(
                    UnderwritingDecisionType.REFERRED
            );
            middle.setDecidedAt(TestFixtures.NOW.minusDays(1));

            UnderwritingDecision newest = TestFixtures.decision(
                    UnderwritingDecisionType.APPROVED
            );
            newest.setDecidedAt(TestFixtures.NOW);

            when(quoteRepository.findById(quote.getQuoteId())).thenReturn(
                    Mono.just(quote)
            );
            when(
                    underwritingDecisionRepository.findAllByQuoteId(
                            quote.getQuoteId()
                    )
            ).thenReturn(Flux.just(oldest, middle, newest));

            StepVerifier.create(
                            quoteService.getUnderwritingDecisionHistory(
                                    quote.getQuoteId()
                            )
                    )
                    .expectNextMatches(
                            response -> response.decision()
                                    == UnderwritingDecisionType.APPROVED
                    )
                    .expectNextMatches(
                            response -> response.decision()
                                    == UnderwritingDecisionType.REFERRED
                    )
                    .expectNextMatches(
                            response -> response.decision()
                                    == UnderwritingDecisionType
                                    .MORE_INFORMATION_REQUIRED
                    )
                    .verifyComplete();
        }

        @Test
        @DisplayName("completes empty when no decision was recorded")
        void returnsEmptyHistory() {
            Quote quote = TestFixtures.inReviewQuote();

            when(quoteRepository.findById(quote.getQuoteId())).thenReturn(
                    Mono.just(quote)
            );
            when(
                    underwritingDecisionRepository.findAllByQuoteId(
                            quote.getQuoteId()
                    )
            ).thenReturn(Flux.empty());

            StepVerifier.create(
                            quoteService.getUnderwritingDecisionHistory(
                                    quote.getQuoteId()
                            )
                    )
                    .verifyComplete();
        }

        @Test
        @DisplayName("documents the null decidedAt failure point")
        void failsOnNullDecidedAt() {
            Quote quote = TestFixtures.inReviewQuote();

            UnderwritingDecision first = TestFixtures.decision(
                    UnderwritingDecisionType.APPROVED
            );
            first.setDecidedAt(null);

            UnderwritingDecision second = TestFixtures.decision(
                    UnderwritingDecisionType.DECLINED
            );
            second.setDecidedAt(TestFixtures.NOW);

            when(quoteRepository.findById(quote.getQuoteId())).thenReturn(
                    Mono.just(quote)
            );
            when(
                    underwritingDecisionRepository.findAllByQuoteId(
                            quote.getQuoteId()
                    )
            ).thenReturn(Flux.just(first, second));

            StepVerifier.create(
                            quoteService.getUnderwritingDecisionHistory(
                                    quote.getQuoteId()
                            )
                    )
                    .expectError(NullPointerException.class)
                    .verify();
        }
    }

    @Nested
    @DisplayName("quoteLifecycleTransitions")
    class QuoteLifecycleTransitions {

        @Test
        @DisplayName("valid transitions succeed")
        void validTransitionsSucceed() {
            org.junit.jupiter.api.Assertions.assertDoesNotThrow(() ->
                    quoteService.validateStatusTransition(QuoteStatus.DRAFT, QuoteStatus.SUBMITTED));
            org.junit.jupiter.api.Assertions.assertDoesNotThrow(() ->
                    quoteService.validateStatusTransition(QuoteStatus.SUBMITTED, QuoteStatus.IN_REVIEW));
            org.junit.jupiter.api.Assertions.assertDoesNotThrow(() ->
                    quoteService.validateStatusTransition(QuoteStatus.SUBMITTED, QuoteStatus.UNDER_REVIEW));
            org.junit.jupiter.api.Assertions.assertDoesNotThrow(() ->
                    quoteService.validateStatusTransition(QuoteStatus.IN_REVIEW, QuoteStatus.QUOTED));
            org.junit.jupiter.api.Assertions.assertDoesNotThrow(() ->
                    quoteService.validateStatusTransition(QuoteStatus.UNDER_REVIEW, QuoteStatus.QUOTED));
            org.junit.jupiter.api.Assertions.assertDoesNotThrow(() ->
                    quoteService.validateStatusTransition(QuoteStatus.QUOTED, QuoteStatus.ACCEPTED));
            org.junit.jupiter.api.Assertions.assertDoesNotThrow(() ->
                    quoteService.validateStatusTransition(QuoteStatus.ACCEPTED, QuoteStatus.BOUND));
            org.junit.jupiter.api.Assertions.assertDoesNotThrow(() ->
                    quoteService.validateStatusTransition(QuoteStatus.BOUND, QuoteStatus.ISSUED));
        }

        @Test
        @DisplayName("invalid direct transitions are rejected")
        void invalidTransitionsAreRejected() {
            // Cannot jump from DRAFT directly to ACCEPTED, BOUND, or ISSUED
            org.junit.jupiter.api.Assertions.assertThrows(
                    com.intellisure.quotepolicyservice.exception.BusinessException.class,
                    () -> quoteService.validateStatusTransition(QuoteStatus.DRAFT, QuoteStatus.ACCEPTED)
            );
            org.junit.jupiter.api.Assertions.assertThrows(
                    com.intellisure.quotepolicyservice.exception.BusinessException.class,
                    () -> quoteService.validateStatusTransition(QuoteStatus.DRAFT, QuoteStatus.BOUND)
            );
            org.junit.jupiter.api.Assertions.assertThrows(
                    com.intellisure.quotepolicyservice.exception.BusinessException.class,
                    () -> quoteService.validateStatusTransition(QuoteStatus.DRAFT, QuoteStatus.ISSUED)
            );

            // Cannot jump from SUBMITTED directly to BOUND or ISSUED
            org.junit.jupiter.api.Assertions.assertThrows(
                    com.intellisure.quotepolicyservice.exception.BusinessException.class,
                    () -> quoteService.validateStatusTransition(QuoteStatus.SUBMITTED, QuoteStatus.BOUND)
            );

            // Cannot jump from QUOTED directly to BOUND or ISSUED without ACCEPTED
            org.junit.jupiter.api.Assertions.assertThrows(
                    com.intellisure.quotepolicyservice.exception.BusinessException.class,
                    () -> quoteService.validateStatusTransition(QuoteStatus.QUOTED, QuoteStatus.BOUND)
            );

            // Cannot transition out of terminal state ISSUED
            org.junit.jupiter.api.Assertions.assertThrows(
                    com.intellisure.quotepolicyservice.exception.BusinessException.class,
                    () -> quoteService.validateStatusTransition(QuoteStatus.ISSUED, QuoteStatus.DRAFT)
            );
        }
    }
}
