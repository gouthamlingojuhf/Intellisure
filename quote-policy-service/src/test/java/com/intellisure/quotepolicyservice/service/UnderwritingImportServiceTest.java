package com.intellisure.quotepolicyservice.service;

import com.intellisure.quotepolicyservice.client.RiskUnderwritingClient;
import com.intellisure.quotepolicyservice.client.dto.RiskAssessmentStatusClient;
import com.intellisure.quotepolicyservice.client.dto.RiskBandClient;
import com.intellisure.quotepolicyservice.client.dto.UnderwritingOutcomeClient;
import com.intellisure.quotepolicyservice.client.dto.UnderwritingResultClientResponse;
import com.intellisure.quotepolicyservice.entity.Quote;
import com.intellisure.quotepolicyservice.entity.UnderwritingDecision;
import com.intellisure.quotepolicyservice.enums.QuoteStatus;
import com.intellisure.quotepolicyservice.enums.UnderwritingDecisionType;
import com.intellisure.quotepolicyservice.exception.BusinessException;
import com.intellisure.quotepolicyservice.exception.ResourceNotFoundException;
import com.intellisure.quotepolicyservice.repository.QuoteRepository;
import com.intellisure.quotepolicyservice.repository.UnderwritingDecisionRepository;
import com.intellisure.quotepolicyservice.testsupport.EntityTemplateStubber;
import com.intellisure.quotepolicyservice.testsupport.TestAssertions;
import com.intellisure.quotepolicyservice.testsupport.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("UnderwritingImportService")
class UnderwritingImportServiceTest {

    private static final UUID ASSESSMENT_ID =
            UUID.fromString("66666666-6666-6666-6666-666666666666");

    private static final UUID DECISION_ID =
            UUID.fromString("77777777-7777-7777-7777-777777777777");

    private QuoteRepository quoteRepository;
    private UnderwritingDecisionRepository decisionRepository;
    private RiskUnderwritingClient riskUnderwritingClient;
    private R2dbcEntityTemplate entityTemplate;

    private UnderwritingImportService service;

    @BeforeEach
    void setUp() {
        quoteRepository = mock(QuoteRepository.class);
        decisionRepository = mock(UnderwritingDecisionRepository.class);
        riskUnderwritingClient = mock(RiskUnderwritingClient.class);
        entityTemplate = mock(R2dbcEntityTemplate.class);

        service = new UnderwritingImportService(
                quoteRepository,
                decisionRepository,
                riskUnderwritingClient,
                entityTemplate
        );

        when(
                entityTemplate.update(any(Quote.class))
        ).thenAnswer(
                invocation -> Mono.just(invocation.getArgument(0))
        );
    }

    private Quote quote() {
        Quote quote = TestFixtures.inReviewQuote();

        quote.setRiskAssessmentId(null);

        return quote;
    }

    private UnderwritingResultClientResponse resultFor(
            Quote quote,
            UnderwritingOutcomeClient outcome
    ) {
        return new UnderwritingResultClientResponse(
                ASSESSMENT_ID,
                "RA-2026-0001",
                quote.getQuoteId(),
                RiskAssessmentStatusClient.COMPLETED,
                new BigDecimal("42.50"),
                RiskBandClient.MODERATE,
                DECISION_ID,
                outcome,
                "Automated rationale",
                "AUTOMATED",
                Boolean.TRUE,
                new BigDecimal("90000.00"),
                new BigDecimal("750.00"),
                new BigDecimal("1450.00"),
                "Survey",
                Boolean.FALSE,
                "RULES-2026.01",
                TestFixtures.NOW
        );
    }

    private void givenRemoteResult(
            Quote quote,
            UnderwritingOutcomeClient outcome
    ) {
        when(
                riskUnderwritingClient.getUnderwritingResult(
                        quote.getQuoteId(),
                        "Bearer token"
                )
        ).thenReturn(Mono.just(resultFor(quote, outcome)));
    }

    @Nested
    @DisplayName("fail fast validation")
    class Validation {

        @Test
        @DisplayName("fails when the quote does not exist")
        void failsForUnknownQuote() {
            when(quoteRepository.findById(any(UUID.class))).thenReturn(
                    Mono.empty()
            );

            StepVerifier.create(
                            service.importResult(
                                    UUID.randomUUID(),
                                    "Bearer token"
                            )
                    )
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            ResourceNotFoundException.class,
                            org.hamcrest.Matchers.containsString(
                                    "Quote not found with ID:"
                            ),
                            error
                    ))
                    .verify();

            verify(riskUnderwritingClient, never())
                    .getUnderwritingResult(any(), anyString());
        }

        @Test
        @DisplayName("rejects a result that belongs to a different quote")
        void rejectsMismatchedQuote() {
            Quote quote = quote();

            when(quoteRepository.findById(quote.getQuoteId())).thenReturn(
                    Mono.just(quote)
            );

            UnderwritingResultClientResponse mismatched =
                    new UnderwritingResultClientResponse(
                            ASSESSMENT_ID,
                            "RA-2026-0001",
                            UUID.randomUUID(),
                            RiskAssessmentStatusClient.COMPLETED,
                            null,
                            RiskBandClient.LOW,
                            DECISION_ID,
                            UnderwritingOutcomeClient.APPROVED,
                            "rationale",
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            Boolean.FALSE,
                            null,
                            TestFixtures.NOW
                    );

            when(
                    riskUnderwritingClient.getUnderwritingResult(
                            quote.getQuoteId(),
                            "Bearer token"
                    )
            ).thenReturn(Mono.just(mismatched));

            StepVerifier.create(
                            service.importResult(
                                    quote.getQuoteId(),
                                    "Bearer token"
                            )
                    )
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            BusinessException.class,
                            "The underwriting result does not belong to "
                                    + "the requested quote",
                            error
                    ))
                    .verify();
        }

        @Test
        @DisplayName("rejects an incomplete result")
        void rejectsIncompleteResult() {
            Quote quote = quote();

            when(quoteRepository.findById(quote.getQuoteId())).thenReturn(
                    Mono.just(quote)
            );

            UnderwritingResultClientResponse incomplete =
                    new UnderwritingResultClientResponse(
                            null,
                            "RA-2026-0001",
                            quote.getQuoteId(),
                            RiskAssessmentStatusClient.COMPLETED,
                            null,
                            null,
                            null,
                            UnderwritingOutcomeClient.APPROVED,
                            "rationale",
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            Boolean.FALSE,
                            null,
                            TestFixtures.NOW
                    );

            when(
                    riskUnderwritingClient.getUnderwritingResult(
                            quote.getQuoteId(),
                            "Bearer token"
                    )
            ).thenReturn(Mono.just(incomplete));

            StepVerifier.create(
                            service.importResult(
                                    quote.getQuoteId(),
                                    "Bearer token"
                            )
                    )
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            BusinessException.class,
                            "The underwriting result is incomplete",
                            error
                    ))
                    .verify();
        }

        @Test
        @DisplayName("rejects a risk assessment that is not COMPLETED")
        void rejectsNonCompletedAssessment() {
            Quote quote = quote();

            when(quoteRepository.findById(quote.getQuoteId())).thenReturn(
                    Mono.just(quote)
            );

            UnderwritingResultClientResponse inProgress = new UnderwritingResultClientResponse(
                    ASSESSMENT_ID,
                    "RA-2026-0001",
                    quote.getQuoteId(),
                    RiskAssessmentStatusClient.IN_PROGRESS,
                    null,
                    null,
                    DECISION_ID,
                    UnderwritingOutcomeClient.APPROVED,
                    "rationale",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    Boolean.FALSE,
                    null,
                    TestFixtures.NOW
            );

            when(
                    riskUnderwritingClient.getUnderwritingResult(
                            quote.getQuoteId(),
                            "Bearer token"
                    )
            ).thenReturn(Mono.just(inProgress));

            StepVerifier.create(
                            service.importResult(
                                    quote.getQuoteId(),
                                    "Bearer token"
                            )
                    )
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            BusinessException.class,
                            org.hamcrest.Matchers.containsString(
                                    "Current status: IN_PROGRESS"
                            ),
                            error
                    ))
                    .verify();
        }

        @Test
        @DisplayName("rejects a non final outcome")
        void rejectsNonFinalOutcome() {
            Quote quote = quote();

            when(quoteRepository.findById(quote.getQuoteId())).thenReturn(
                    Mono.just(quote)
            );
            givenRemoteResult(quote, UnderwritingOutcomeClient.REFERRED);

            StepVerifier.create(
                            service.importResult(
                                    quote.getQuoteId(),
                                    "Bearer token"
                            )
                    )
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            BusinessException.class,
                            "Only APPROVED, APPROVED_WITH_CONDITIONS or "
                                    + "DECLINED results can be imported",
                            error
                    ))
                    .verify();
        }

        @Test
        @DisplayName("rejects a result with outstanding subjectivities")
        void rejectsOutstandingSubjectivities() {
            Quote quote = quote();

            when(quoteRepository.findById(quote.getQuoteId())).thenReturn(
                    Mono.just(quote)
            );

            UnderwritingResultClientResponse blocked =
                    new UnderwritingResultClientResponse(
                            ASSESSMENT_ID,
                            "RA-2026-0001",
                            quote.getQuoteId(),
                            RiskAssessmentStatusClient.COMPLETED,
                            null,
                            null,
                            DECISION_ID,
                            UnderwritingOutcomeClient.APPROVED,
                            "rationale",
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            Boolean.TRUE,
                            null,
                            TestFixtures.NOW
                    );

            when(
                    riskUnderwritingClient.getUnderwritingResult(
                            quote.getQuoteId(),
                            "Bearer token"
                    )
            ).thenReturn(Mono.just(blocked));

            StepVerifier.create(
                            service.importResult(
                                    quote.getQuoteId(),
                                    "Bearer token"
                            )
                    )
                    .expectErrorSatisfies(error -> TestAssertions.errorIs(
                            BusinessException.class,
                            "The underwriting result cannot be imported "
                                    + "while bind-blocking subjectivities "
                                    + "remain outstanding",
                            error
                    ))
                    .verify();
        }

        @Test
        @DisplayName("a null subjectivities flag is treated as clear")
        void acceptsNullSubjectivities() {
            Quote quote = quote();

            when(quoteRepository.findById(quote.getQuoteId())).thenReturn(
                    Mono.just(quote)
            );
            when(
                    decisionRepository.findBySourceDecisionId(DECISION_ID)
            ).thenReturn(Mono.empty());
            EntityTemplateStubber.stubInsert(
                    entityTemplate,
                    UnderwritingDecision.class
            );

            UnderwritingResultClientResponse cleared =
                    new UnderwritingResultClientResponse(
                            ASSESSMENT_ID,
                            "RA-2026-0001",
                            quote.getQuoteId(),
                            RiskAssessmentStatusClient.COMPLETED,
                            new BigDecimal("10.00"),
                            RiskBandClient.LOW,
                            DECISION_ID,
                            UnderwritingOutcomeClient.APPROVED,
                            "rationale",
                            "AUTOMATED",
                            Boolean.TRUE,
                            new BigDecimal("1000.00"),
                            new BigDecimal("100.00"),
                            new BigDecimal("100.00"),
                            null,
                            null,
                            "RULES",
                            TestFixtures.NOW
                    );

            when(
                    riskUnderwritingClient.getUnderwritingResult(
                            quote.getQuoteId(),
                            "Bearer token"
                    )
            ).thenReturn(Mono.just(cleared));

            StepVerifier.create(
                            service.importResult(
                                    quote.getQuoteId(),
                                    "Bearer token"
                            )
                    )
                    .assertNext(response -> assertNull(
                            response.subjectivitiesOutstanding()
                    ))
                    .verifyComplete();
        }

        @Test
        @DisplayName("propagates downstream failures untouched")
        void propagatesDownstreamFailure() {
            Quote quote = quote();

            when(quoteRepository.findById(quote.getQuoteId())).thenReturn(
                    Mono.just(quote)
            );
            when(
                    riskUnderwritingClient.getUnderwritingResult(
                            quote.getQuoteId(),
                            "Bearer token"
                    )
            ).thenReturn(
                    Mono.error(
                            new com.intellisure.quotepolicyservice
                                    .exception
                                    .DownstreamServiceUnavailableException(
                                    "down",
                                    new RuntimeException("root")
                            )
                    )
            );

            StepVerifier.create(
                            service.importResult(
                                    quote.getQuoteId(),
                                    "Bearer token"
                            )
                    )
                    .expectError(
                            com.intellisure.quotepolicyservice.exception
                                    .DownstreamServiceUnavailableException
                                    .class
                    )
                    .verify();
        }
    }

    @Nested
    @DisplayName("successful import")
    class SuccessfulImport {

        @BeforeEach
        void prepare() {
            when(
                    decisionRepository.findBySourceDecisionId(DECISION_ID)
            ).thenReturn(Mono.empty());
            EntityTemplateStubber.stubInsert(
                    entityTemplate,
                    UnderwritingDecision.class
            );
        }

        @Test
        @DisplayName("APPROVED keeps the quote IN_REVIEW and clears the decline reason")
        void importsApproval() {
            Quote quote = quote();
            quote.setDeclineReason("stale reason");

            when(quoteRepository.findById(quote.getQuoteId())).thenReturn(
                    Mono.just(quote)
            );
            givenRemoteResult(quote, UnderwritingOutcomeClient.APPROVED);

            StepVerifier.create(
                            service.importResult(
                                    quote.getQuoteId(),
                                    "Bearer token"
                            )
                    )
                    .assertNext(response -> {
                        assertEquals(
                                quote.getQuoteId(),
                                response.quoteId()
                        );
                        assertEquals(
                                ASSESSMENT_ID,
                                response.riskAssessmentId()
                        );
                        assertEquals(
                                DECISION_ID,
                                response.sourceDecisionId()
                        );
                        assertEquals(
                                UnderwritingDecisionType.APPROVED,
                                response.importedDecision()
                        );
                        assertEquals("MODERATE", response.riskBand());
                        assertEquals(
                                new BigDecimal("42.50"),
                                response.riskScore()
                        );
                        assertEquals(
                                new BigDecimal("90000.00"),
                                response.approvedLimit()
                        );
                        assertEquals(
                                Boolean.FALSE,
                                response.subjectivitiesOutstanding()
                        );
                        assertEquals(
                                "RULES-2026.01",
                                response.ruleVersionReference()
                        );
                        assertTrue(!response.alreadyImported());
                    })
                    .verifyComplete();

            assertEquals(
                    ASSESSMENT_ID,
                    quote.getRiskAssessmentId()
            );
            assertEquals(QuoteStatus.IN_REVIEW, quote.getStatus());
            assertNull(quote.getDeclineReason());
        }

        @Test
        @DisplayName("APPROVED_WITH_CONDITIONS maps to APPROVED_WITH_MODIFIED_TERMS")
        void importsConditionalApproval() {
            Quote quote = quote();

            when(quoteRepository.findById(quote.getQuoteId())).thenReturn(
                    Mono.just(quote)
            );
            givenRemoteResult(
                    quote,
                    UnderwritingOutcomeClient.APPROVED_WITH_CONDITIONS
            );

            StepVerifier.create(
                            service.importResult(
                                    quote.getQuoteId(),
                                    "Bearer token"
                            )
                    )
                    .assertNext(response -> {
                        assertEquals(
                                UnderwritingDecisionType
                                        .APPROVED_WITH_MODIFIED_TERMS,
                                response.importedDecision()
                        );
                        assertEquals(
                                QuoteStatus.IN_REVIEW,
                                response.quoteStatus()
                        );
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("DECLINED declines the quote and stores the rationale")
        void importsDecline() {
            Quote quote = quote();

            when(quoteRepository.findById(quote.getQuoteId())).thenReturn(
                    Mono.just(quote)
            );
            givenRemoteResult(quote, UnderwritingOutcomeClient.DECLINED);

            StepVerifier.create(
                            service.importResult(
                                    quote.getQuoteId(),
                                    "Bearer token"
                            )
                    )
                    .assertNext(response -> {
                        assertEquals(
                                UnderwritingDecisionType.DECLINED,
                                response.importedDecision()
                        );
                        assertEquals(
                                QuoteStatus.DECLINED_BY_INSURER,
                                response.quoteStatus()
                        );
                    })
                    .verifyComplete();

            assertEquals(
                    QuoteStatus.DECLINED_BY_INSURER,
                    quote.getStatus()
            );
            assertEquals("Automated rationale", quote.getDeclineReason());
        }

        @Test
        @DisplayName("a null risk band is returned as null rather than NPE")
        void handlesNullRiskBand() {
            Quote quote = quote();

            when(quoteRepository.findById(quote.getQuoteId())).thenReturn(
                    Mono.just(quote)
            );

            UnderwritingResultClientResponse noBand =
                    new UnderwritingResultClientResponse(
                            ASSESSMENT_ID,
                            "RA-2026-0001",
                            quote.getQuoteId(),
                            RiskAssessmentStatusClient.COMPLETED,
                            null,
                            null,
                            DECISION_ID,
                            UnderwritingOutcomeClient.APPROVED,
                            "rationale",
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            Boolean.FALSE,
                            null,
                            TestFixtures.NOW
                    );

            when(
                    riskUnderwritingClient.getUnderwritingResult(
                            quote.getQuoteId(),
                            "Bearer token"
                    )
            ).thenReturn(Mono.just(noBand));

            StepVerifier.create(
                            service.importResult(
                                    quote.getQuoteId(),
                                    "Bearer token"
                            )
                    )
                    .assertNext(response -> assertNull(response.riskBand()))
                    .verifyComplete();
        }

        @Test
        @DisplayName("the snapshot inherits the assigned underwriter of the quote")
        void storesSnapshotForAssignedUnderwriter() {
            Quote quote = quote();

            when(quoteRepository.findById(quote.getQuoteId())).thenReturn(
                    Mono.just(quote)
            );
            givenRemoteResult(quote, UnderwritingOutcomeClient.APPROVED);

            UnderwritingDecision[] captured = new UnderwritingDecision[1];

            EntityTemplateStubber.stubInsert(
                    entityTemplate,
                    UnderwritingDecision.class,
                    decision -> {
                        captured[0] = decision;
                        return decision;
                    }
            );

            service.importResult(quote.getQuoteId(), "Bearer token")
                    .block();

            assertNotNull(captured[0]);
            assertEquals(
                    quote.getAssignedUnderwriterId(),
                    captured[0].getUnderwriterId()
            );
            assertEquals(DECISION_ID, captured[0].getSourceDecisionId());
            assertEquals(
                    quote.getQuoteId(),
                    captured[0].getQuoteId()
            );
        }

        @Test
        @DisplayName("an already imported decision is not stored twice")
        void doesNotDuplicateImport() {
            Quote quote = quote();

            when(quoteRepository.findById(quote.getQuoteId())).thenReturn(
                    Mono.just(quote)
            );
            givenRemoteResult(quote, UnderwritingOutcomeClient.APPROVED);
            when(
                    decisionRepository.findBySourceDecisionId(DECISION_ID)
            ).thenReturn(
                    Mono.just(
                            TestFixtures.decision(
                                    UnderwritingDecisionType.APPROVED
                            )
                    )
            );

            StepVerifier.create(
                            service.importResult(
                                    quote.getQuoteId(),
                                    "Bearer token"
                            )
                    )
                    .assertNext(response -> {
                        assertTrue(response.alreadyImported());
                        assertEquals(
                                UnderwritingDecisionType.APPROVED,
                                response.importedDecision()
                        );
                    })
                    .verifyComplete();

            verify(
                    entityTemplate,
                    never()
            ).insert(UnderwritingDecision.class);
            verify(entityTemplate, never()).update(any(Quote.class));
        }

        @Test
        @DisplayName("the created snapshot is timestamped")
        void timestampsSnapshot() {
            Quote quote = quote();

            when(quoteRepository.findById(quote.getQuoteId())).thenReturn(
                    Mono.just(quote)
            );
            givenRemoteResult(quote, UnderwritingOutcomeClient.APPROVED);

            LocalDateTime before = LocalDateTime.now().minusSeconds(1);

            service.importResult(quote.getQuoteId(), "Bearer token")
                    .block();

            assertTrue(
                    quote.getUpdatedAt() != null
                            && quote.getUpdatedAt().isAfter(before)
            );
        }
    }
}
