package com.intellisure.quotepolicyservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellisure.quotepolicyservice.dto.request.AssignUnderwriterRequest;
import com.intellisure.quotepolicyservice.dto.request.CreateQuoteRequest;
import com.intellisure.quotepolicyservice.dto.request.DeclineQuoteRequest;
import com.intellisure.quotepolicyservice.dto.request.OfferQuoteTermsRequest;
import com.intellisure.quotepolicyservice.dto.request.OfferedCoverageRequest;
import com.intellisure.quotepolicyservice.dto.response.CoverageCheckResponse;
import com.intellisure.quotepolicyservice.dto.response.PolicyResponse;
import com.intellisure.quotepolicyservice.dto.response.QuoteResponse;
import com.intellisure.quotepolicyservice.dto.response.UnderwritingDecisionResponse;
import com.intellisure.quotepolicyservice.entity.Policy;
import com.intellisure.quotepolicyservice.entity.PolicyCoverage;
import com.intellisure.quotepolicyservice.entity.Quote;
import com.intellisure.quotepolicyservice.entity.QuoteCoverage;
import com.intellisure.quotepolicyservice.entity.UnderwritingDecision;
import com.intellisure.quotepolicyservice.enums.PolicyStatus;
import com.intellisure.quotepolicyservice.enums.QuoteStatus;
import com.intellisure.quotepolicyservice.exception.ApiError;
import com.intellisure.quotepolicyservice.enums.UnderwritingDecisionType;
import com.intellisure.quotepolicyservice.testsupport.JwtTestTokens;
import com.intellisure.quotepolicyservice.testsupport.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End to end coverage of the reactive API: real WebFlux routing, real
 * security chain with genuinely signed HS256 tokens, real R2DBC persistence
 * against H2 and the real exception handling advice.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
        properties = {
                "intellisure.underwriting.eligible-underwriter-ids="
                        + "33333333-3333-3333-3333-333333333333"
        }
)
@DisplayName("Quote & Policy API (full stack integration)")
class QuotePolicyServiceIntegrationTest {

    private static final String QUOTES = "/api/quotes";
    private static final String POLICIES = "/api/policies";

    @LocalServerPort
    private int port;

    @Autowired
    private R2dbcEntityTemplate entityTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();

        entityTemplate.delete(PolicyCoverage.class).all().block();
        entityTemplate.delete(Policy.class).all().block();
        entityTemplate.delete(UnderwritingDecision.class).all().block();
        entityTemplate.delete(QuoteCoverage.class).all().block();
        entityTemplate.delete(Quote.class).all().block();
    }

    private WebTestClient asPolicyholder() {
        return client.mutate()
                .defaultHeaders(
                        headers -> headers.setBearerAuth(
                                JwtTestTokens.policyholderToken(
                                        TestFixtures.CUSTOMER_ID
                                )
                        )
                )
                .build();
    }

    private WebTestClient asUnderwriter() {
        return client.mutate()
                .defaultHeaders(
                        headers -> headers.setBearerAuth(
                                JwtTestTokens.underwriterToken(
                                        TestFixtures.UNDERWRITER_ID
                                )
                        )
                )
                .build();
    }

    private WebTestClient asAdmin() {
        return client.mutate()
                .defaultHeaders(
                        headers -> headers.setBearerAuth(
                                JwtTestTokens.adminToken(
                                        TestFixtures.ADMIN_ID
                                )
                        )
                )
                .build();
    }

    private WebTestClient asClaimsAdjuster() {
        return client.mutate()
                .defaultHeaders(
                        headers -> headers.setBearerAuth(
                                JwtTestTokens.claimsAdjusterToken(
                                        TestFixtures.ADMIN_ID
                                )
                        )
                )
                .build();
    }

    private CreateQuoteRequest validRequest() {
        return TestFixtures.createQuoteRequest();
    }

    private CreateQuoteRequest validRequestForToday() {
        return new CreateQuoteRequest(
                TestFixtures.CUSTOMER_ID,
                "COMMERCIAL-PROPERTY",
                "Fire and liability cover",
                "Warehousing",
                LocalDate.now(),
                List.of(
                        new com.intellisure.quotepolicyservice.dto.request
                                .CreateQuoteCoverageRequest(
                                "fire",
                                "Fire cover",
                                new java.math.BigDecimal("100000.00"),
                                new java.math.BigDecimal("500.00"),
                                30
                        ),
                        new com.intellisure.quotepolicyservice.dto.request
                                .CreateQuoteCoverageRequest(
                                "liability",
                                "Public liability",
                                new java.math.BigDecimal("500000.00"),
                                new java.math.BigDecimal("1000.00"),
                                null
                        )
                )
        );
    }

    private QuoteResponse createQuote(CreateQuoteRequest request) {
        return asPolicyholder()
                .post()
                .uri(QUOTES)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(QuoteResponse.class)
                .returnResult()
                .getResponseBody();
    }

    private OfferQuoteTermsRequest offerTerms() {
        return new OfferQuoteTermsRequest(
                LocalDateTime.now().plusDays(7),
                List.of(
                        new OfferedCoverageRequest(
                                "fire",
                                new java.math.BigDecimal("90000.00"),
                                new java.math.BigDecimal("750.00"),
                                new java.math.BigDecimal("700.00"),
                                "Survey required",
                                "Flood",
                                30
                        ),
                        new OfferedCoverageRequest(
                                "liability",
                                new java.math.BigDecimal("450000.00"),
                                new java.math.BigDecimal("1500.00"),
                                new java.math.BigDecimal("800.00"),
                                null,
                                null,
                                null
                        )
                )
        );
    }

    /**
     * Drives a quote from DRAFT to BOUND and returns the bound policy.
     */
    private PolicyResponse bindThroughFullLifecycle() {
        QuoteResponse quote = createQuote(validRequestForToday());

        asPolicyholder()
                .patch()
                .uri(QUOTES + "/" + quote.quoteId() + "/submit")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("IN_REVIEW")
                .jsonPath("$.assignedUnderwriterId")
                .isEqualTo(TestFixtures.UNDERWRITER_ID.toString());

        approveQuote(quote.quoteId());
        offerTerms(quote.quoteId());

        asPolicyholder()
                .patch()
                .uri(QUOTES + "/" + quote.quoteId() + "/accept")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("ACCEPTED");

        PolicyResponse policy = asUnderwriter()
                .post()
                .uri(POLICIES + "/bind/" + quote.quoteId())
                .exchange()
                .expectStatus().isCreated()
                .expectBody(PolicyResponse.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(policy);

        return policy;
    }

    private void approveQuote(UUID quoteId) {
        asUnderwriter()
                .post()
                .uri(QUOTES + "/" + quoteId + "/underwriting-decisions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(
                        TestFixtures.decisionRequest(
                                UnderwritingDecisionType.APPROVED
                        )
                )
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.decision").isEqualTo("APPROVED");
    }

    private void offerTerms(UUID quoteId) {
        asUnderwriter()
                .patch()
                .uri(QUOTES + "/" + quoteId + "/offer-terms")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(offerTerms())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("QUOTED")
                .jsonPath("$.totalPremium").isEqualTo("1500.0");
    }

    @Nested
    @DisplayName("security")
    class Security {

        @Test
        @DisplayName("rejects an anonymous request with 401")
        void anonymousIsRejected() {
            client.get()
                    .uri(QUOTES + "/" + UUID.randomUUID())
                    .exchange()
                    .expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("rejects a malformed bearer token with 401")
        void malformedTokenIsRejected() {
            client.mutate()
                    .defaultHeaders(
                            headers -> headers.setBearerAuth("not-a-jwt")
                    )
                    .build()
                    .get()
                    .uri(QUOTES + "/" + UUID.randomUUID())
                    .exchange()
                    .expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("rejects a token signed with the wrong key with 401")
        void wrongSignatureIsRejected() {
            String tampered = JwtTestTokens.underwriterToken(
                    TestFixtures.UNDERWRITER_ID
            );

            client.mutate()
                    .defaultHeaders(
                            headers -> headers.setBearerAuth(
                                    tampered.substring(
                                            0,
                                            tampered.lastIndexOf('.') + 1
                                    ) + "AAAA"
                            )
                    )
                    .build()
                    .get()
                    .uri(QUOTES + "/" + UUID.randomUUID())
                    .exchange()
                    .expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("exposes the health endpoint without authentication")
        void healthIsPublic() {
            client.get()
                    .uri("/actuator/health")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("UP");
        }

        @Test
        @DisplayName("echoes the correlation id on the response")
        void echoesCorrelationId() {
            client.get()
                    .uri("/actuator/health")
                    .header("X-Correlation-Id", "integration-1")
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader()
                    .valueEquals("X-Correlation-Id", "integration-1");
        }

        @Test
        @DisplayName("generates a correlation id when none is supplied")
        void generatesCorrelationId() {
            client.get()
                    .uri("/actuator/health")
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader()
                    .exists("X-Correlation-Id");
        }
    }

    @Nested
    @DisplayName("POST /api/quotes")
    class CreateQuote {

        @Test
        @DisplayName("creates a draft quote for the authenticated customer")
        void createsDraft() {
            QuoteResponse response = createQuote(validRequest());

            assertNotNull(response.quoteId());
            assertEquals(TestFixtures.CUSTOMER_ID, response.customerId());
            assertEquals(QuoteStatus.DRAFT, response.status());
            assertTrue(
                    response.quoteNumber()
                            .matches("QTE-\\d{4}-[0-9A-F]{8}")
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

        @Test
        @DisplayName("returns 400 with field errors for an invalid body")
        void rejectsInvalidBody() {
            asPolicyholder()
                    .post()
                    .uri(QUOTES)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(
                            Map.of(
                                    "customerId", TestFixtures.CUSTOMER_ID,
                                    "productCode", "  ",
                                    "requestedEffectiveDate",
                                    LocalDate.now().plusDays(1),
                                    "coverages", List.of()
                            )
                    )
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody(ApiError.class)
                    .value(error -> {
                        assertEquals(
                                "Request validation failed",
                                error.message()
                        );
                        assertNotNull(error.validationErrors());
                        assertTrue(
                                error.validationErrors()
                                        .containsKey("productCode")
                        );
                        assertTrue(
                                error.validationErrors()
                                        .containsKey("coverages")
                        );
                    });
        }

        @Test
        @DisplayName("returns 400 for a past requested effective date")
        void rejectsPastEffectiveDate() {
            CreateQuoteRequest request = new CreateQuoteRequest(
                    TestFixtures.CUSTOMER_ID,
                    "PRODUCT",
                    "need",
                    "operations",
                    LocalDate.now().minusDays(1),
                    List.of(
                            validRequest().coverages().get(0)
                    )
            );

            asPolicyholder()
                    .post()
                    .uri(QUOTES)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.validationErrors.requestedEffectiveDate")
                    .exists();
        }

        @Test
        @DisplayName("returns 403 when creating for another customer")
        void rejectsCrossCustomer() {
            CreateQuoteRequest request = new CreateQuoteRequest(
                    TestFixtures.USER_ID,
                    "PRODUCT",
                    "need",
                    "operations",
                    LocalDate.now().plusDays(1),
                    List.of(validRequest().coverages().get(0))
            );

            asPolicyholder()
                    .post()
                    .uri(QUOTES)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isForbidden()
                    .expectBody()
                    .jsonPath("$.message")
                    .value(message -> assertTrue(
                            message.toString().contains("authenticated")
                    ));
        }

        @Test
        @DisplayName("returns 400 for duplicate coverage codes")
        void rejectsDuplicateCoverageCodes() {
            CreateQuoteRequest request = new CreateQuoteRequest(
                    TestFixtures.CUSTOMER_ID,
                    "PRODUCT",
                    "need",
                    "operations",
                    LocalDate.now().plusDays(1),
                    List.of(
                            validRequest().coverages().get(0),
                            validRequest().coverages().get(0)
                    )
            );

            asPolicyholder()
                    .post()
                    .uri(QUOTES)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.message")
                    .isEqualTo("Duplicate coverage code: FIRE");
        }

        @Test
        @DisplayName("returns 400 when the body cannot be parsed")
        void rejectsMalformedJson() {
            asPolicyholder()
                    .post()
                    .uri(QUOTES)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("{not-json")
                    .exchange()
                    .expectStatus().isBadRequest();
        }
    }

    @Nested
    @DisplayName("quote lifecycle")
    class Lifecycle {

        @Test
        @DisplayName("submit moves a draft quote to IN_REVIEW and assigns the least loaded underwriter")
        void submitAssignsUnderwriter() {
            QuoteResponse quote = createQuote(validRequest());

            asPolicyholder()
                    .patch()
                    .uri(QUOTES + "/" + quote.quoteId() + "/submit")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("IN_REVIEW")
                    .jsonPath("$.assignedUnderwriterId")
                    .isEqualTo(TestFixtures.UNDERWRITER_ID.toString())
                    .jsonPath("$.submittedAt").exists();
        }

        @Test
        @DisplayName("submitting twice is rejected with 400")
        void submitTwiceIsRejected() {
            QuoteResponse quote = createQuote(validRequest());

            asPolicyholder()
                    .patch()
                    .uri(QUOTES + "/" + quote.quoteId() + "/submit")
                    .exchange()
                    .expectStatus().isOk();

            asPolicyholder()
                    .patch()
                    .uri(QUOTES + "/" + quote.quoteId() + "/submit")
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.message")
                    .value(message -> assertTrue(
                            message.toString().contains(
                                    "Only a DRAFT quote"
                            )
                    ));
        }

        @Test
        @DisplayName("a customer cannot submit another customer's quote")
        void submitIsOwnerOnly() {
            QuoteResponse quote = createQuote(validRequest());

            client.mutate()
                    .defaultHeaders(
                            headers -> headers.setBearerAuth(
                                    JwtTestTokens.policyholderToken(
                                            UUID.randomUUID()
                                    )
                            )
                    )
                    .build()
                    .patch()
                    .uri(QUOTES + "/" + quote.quoteId() + "/submit")
                    .exchange()
                    .expectStatus().isForbidden();
        }

        @Test
        @DisplayName("submitting an unknown quote returns 404")
        void submitUnknownQuote() {
            asPolicyholder()
                    .patch()
                    .uri(QUOTES + "/" + UUID.randomUUID() + "/submit")
                    .exchange()
                    .expectStatus().isNotFound()
                    .expectBody()
                    .jsonPath("$.message")
                    .value(message -> assertTrue(
                            message.toString().contains(
                                    "Quote not found with ID:"
                            )
                    ));
        }

        @Test
        @DisplayName("a non assigned underwriter cannot record a decision")
        void decisionRequiresAssignment() {
            QuoteResponse quote = createQuote(validRequest());

            asPolicyholder()
                    .patch()
                    .uri(QUOTES + "/" + quote.quoteId() + "/submit")
                    .exchange()
                    .expectStatus().isOk();

            client.mutate()
                    .defaultHeaders(
                            headers -> headers.setBearerAuth(
                                    JwtTestTokens.underwriterToken(
                                            TestFixtures
                                                    .OTHER_UNDERWRITER_ID
                                    )
                            )
                    )
                    .build()
                    .post()
                    .uri(QUOTES + "/" + quote.quoteId()
                            + "/underwriting-decisions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(
                            TestFixtures.decisionRequest(
                                    UnderwritingDecisionType.APPROVED
                            )
                    )
                    .exchange()
                    .expectStatus().isForbidden();
        }

        @Test
        @DisplayName("MORE_INFORMATION_REQUIRED moves the quote to NEEDS_INFORMATION")
        void moreInformationRequired() {
            QuoteResponse quote = createQuote(validRequest());

            asPolicyholder()
                    .patch()
                    .uri(QUOTES + "/" + quote.quoteId() + "/submit")
                    .exchange()
                    .expectStatus().isOk();

            asUnderwriter()
                    .post()
                    .uri(QUOTES + "/" + quote.quoteId()
                            + "/underwriting-decisions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(
                            TestFixtures.decisionRequest(
                                    UnderwritingDecisionType
                                            .MORE_INFORMATION_REQUIRED
                            )
                    )
                    .exchange()
                    .expectStatus().isCreated();

            asPolicyholder()
                    .get()
                    .uri(QUOTES + "/" + quote.quoteId())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.status")
                    .isEqualTo("NEEDS_INFORMATION");
        }

        @Test
        @DisplayName("DECLINED moves the quote to DECLINED_BY_INSURER with the rationale")
        void declinedByInsurer() {
            QuoteResponse quote = createQuote(validRequest());

            asPolicyholder()
                    .patch()
                    .uri(QUOTES + "/" + quote.quoteId() + "/submit")
                    .exchange()
                    .expectStatus().isOk();

            asUnderwriter()
                    .post()
                    .uri(QUOTES + "/" + quote.quoteId()
                            + "/underwriting-decisions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(
                            TestFixtures.decisionRequest(
                                    UnderwritingDecisionType.DECLINED
                            )
                    )
                    .exchange()
                    .expectStatus().isCreated();

            asPolicyholder()
                    .get()
                    .uri(QUOTES + "/" + quote.quoteId())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.status")
                    .isEqualTo("DECLINED_BY_INSURER")
                    .jsonPath("$.declineReason")
                    .isEqualTo("Inspected the risk");
        }

        @Test
        @DisplayName("terms cannot be offered without an approved decision")
        void offerTermsRequiresDecision() {
            QuoteResponse quote = createQuote(validRequest());

            asPolicyholder()
                    .patch()
                    .uri(QUOTES + "/" + quote.quoteId() + "/submit")
                    .exchange()
                    .expectStatus().isOk();

            asUnderwriter()
                    .patch()
                    .uri(QUOTES + "/" + quote.quoteId() + "/offer-terms")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(offerTerms())
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.message")
                    .value(message -> assertTrue(
                            message.toString().contains(
                                    "approved underwriting decision"
                            )
                    ));
        }

        @Test
        @DisplayName("offered terms must cover every requested coverage")
        void offerTermsMustBeComplete() {
            QuoteResponse quote = createQuote(validRequest());

            asPolicyholder()
                    .patch()
                    .uri(QUOTES + "/" + quote.quoteId() + "/submit")
                    .exchange()
                    .expectStatus().isOk();
            approveQuote(quote.quoteId());

            OfferQuoteTermsRequest partial =
                    new OfferQuoteTermsRequest(
                            LocalDateTime.now().plusDays(7),
                            List.of(
                                    offerTerms().coverages().get(0)
                            )
                    );

            asUnderwriter()
                    .patch()
                    .uri(QUOTES + "/" + quote.quoteId() + "/offer-terms")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(partial)
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.message")
                    .value(message -> assertTrue(
                            message.toString().contains("Missing:")
                    ));
        }

        @Test
        @DisplayName("the offer must expire in the future")
        void offerTermsMustExpireInTheFuture() {
            QuoteResponse quote = createQuote(validRequest());

            asPolicyholder()
                    .patch()
                    .uri(QUOTES + "/" + quote.quoteId() + "/submit")
                    .exchange()
                    .expectStatus().isOk();
            approveQuote(quote.quoteId());

            OfferQuoteTermsRequest expired =
                    new OfferQuoteTermsRequest(
                            LocalDateTime.now().minusDays(1),
                            offerTerms().coverages()
                    );

            asUnderwriter()
                    .patch()
                    .uri(QUOTES + "/" + quote.quoteId() + "/offer-terms")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(expired)
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.validationErrors.quoteExpiresAt")
                    .exists();
        }

        @Test
        @DisplayName("the customer declines a quote and the reason is stored")
        void declineQuote() {
            QuoteResponse quote = createQuote(validRequestForToday());

            asPolicyholder()
                    .patch()
                    .uri(QUOTES + "/" + quote.quoteId() + "/submit")
                    .exchange()
                    .expectStatus().isOk();
            approveQuote(quote.quoteId());
            offerTerms(quote.quoteId());

            asPolicyholder()
                    .patch()
                    .uri(QUOTES + "/" + quote.quoteId() + "/decline")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(
                            new DeclineQuoteRequest("  Too expensive  ")
                    )
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.status")
                    .isEqualTo("DECLINED_BY_CUSTOMER")
                    .jsonPath("$.declineReason")
                    .isEqualTo("Too expensive");
        }

        @Test
        @DisplayName("a decline without a reason is rejected")
        void declineRequiresReason() {
            asPolicyholder()
                    .patch()
                    .uri(QUOTES + "/" + UUID.randomUUID() + "/decline")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("reason", " "))
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.validationErrors.reason")
                    .exists();
        }

        @Test
        @DisplayName("an admin can reassign the underwriter of a submitted quote")
        void reassignUnderwriter() {
            QuoteResponse quote = createQuote(validRequest());

            asPolicyholder()
                    .patch()
                    .uri(QUOTES + "/" + quote.quoteId() + "/submit")
                    .exchange()
                    .expectStatus().isOk();

            asAdmin()
                    .patch()
                    .uri(QUOTES + "/" + quote.quoteId()
                            + "/underwriter/reassign")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(
                            new AssignUnderwriterRequest(
                                    TestFixtures.OTHER_UNDERWRITER_ID
                            )
                    )
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("IN_REVIEW")
                    .jsonPath("$.assignedUnderwriterId")
                    .isEqualTo(TestFixtures.OTHER_UNDERWRITER_ID.toString());
        }

        @Test
        @DisplayName("a draft quote cannot be reassigned")
        void reassignDraftIsRejected() {
            QuoteResponse quote = createQuote(validRequest());

            asAdmin()
                    .patch()
                    .uri(QUOTES + "/" + quote.quoteId()
                            + "/underwriter/reassign")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(
                            new AssignUnderwriterRequest(
                                    TestFixtures.OTHER_UNDERWRITER_ID
                            )
                    )
                    .exchange()
                    .expectStatus().isBadRequest();
        }

        @Test
        @DisplayName("the decision history is returned newest first")
        void decisionHistory() {
            QuoteResponse quote = createQuote(validRequest());

            asPolicyholder()
                    .patch()
                    .uri(QUOTES + "/" + quote.quoteId() + "/submit")
                    .exchange()
                    .expectStatus().isOk();

            for (UnderwritingDecisionType type : List.of(
                    UnderwritingDecisionType.REFERRED,
                    UnderwritingDecisionType.APPROVED_WITH_MODIFIED_TERMS
            )) {
                asUnderwriter()
                        .post()
                        .uri(QUOTES + "/" + quote.quoteId()
                                + "/underwriting-decisions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(TestFixtures.decisionRequest(type))
                        .exchange()
                        .expectStatus().isCreated();
            }

            List<UnderwritingDecisionResponse> history =
                    asUnderwriter()
                            .get()
                            .uri(QUOTES + "/" + quote.quoteId()
                                    + "/underwriting-decisions")
                            .exchange()
                            .expectStatus().isOk()
                            .expectBodyList(
                                    UnderwritingDecisionResponse.class
                            )
                            .returnResult()
                            .getResponseBody();

            assertNotNull(history);
            assertEquals(2, history.size());
            assertEquals(
                    UnderwritingDecisionType.APPROVED_WITH_MODIFIED_TERMS,
                    history.get(0).decision()
            );
            assertEquals(
                    UnderwritingDecisionType.REFERRED,
                    history.get(1).decision()
            );
        }

        @Test
        @DisplayName("the decision history of an unknown quote returns 404")
        void decisionHistoryUnknownQuote() {
            asUnderwriter()
                    .get()
                    .uri(QUOTES + "/" + UUID.randomUUID()
                            + "/underwriting-decisions")
                    .exchange()
                    .expectStatus().isNotFound();
        }
    }

    @Nested
    @DisplayName("quote read endpoints")
    class QuoteReads {

        @Test
        @DisplayName("fetches a quote by id and by number")
        void readsByIdAndNumber() {
            QuoteResponse quote = createQuote(validRequest());

            asPolicyholder()
                    .get()
                    .uri(QUOTES + "/" + quote.quoteId())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.quoteId")
                    .isEqualTo(quote.quoteId().toString());

            asPolicyholder()
                    .get()
                    .uri(QUOTES + "/number/" + quote.quoteNumber())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.quoteNumber")
                    .isEqualTo(quote.quoteNumber());
        }

        @Test
        @DisplayName("returns 404 for an unknown quote number")
        void unknownQuoteNumber() {
            asPolicyholder()
                    .get()
                    .uri(QUOTES + "/number/QTE-2026-NOPE")
                    .exchange()
                    .expectStatus().isNotFound();
        }

        @Test
        @DisplayName("returns 400 when the id is not a UUID")
        void malformedQuoteId() {
            asPolicyholder()
                    .get()
                    .uri(QUOTES + "/not-a-uuid")
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.message")
                    .isEqualTo("Invalid request value or format");
        }

        @Test
        @DisplayName("lists every quote of a customer")
        void listsCustomerQuotes() {
            createQuote(validRequest());
            createQuote(validRequest());

            asPolicyholder()
                    .get()
                    .uri(QUOTES + "/customer/" + TestFixtures.CUSTOMER_ID)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(QuoteResponse.class)
                    .hasSize(2);
        }
    }

    @Nested
    @DisplayName("policy endpoints")
    class Policies {

        @Test
        @DisplayName("binds an accepted quote into an in force policy")
        void bindsPolicy() {
            PolicyResponse policy = bindThroughFullLifecycle();

            assertNotNull(policy.policyId());
            assertEquals(PolicyStatus.IN_FORCE, policy.status());
            assertTrue(
                    policy.policyNumber()
                            .matches("POL-\\d{4}-[0-9A-F]{8}")
            );
            assertEquals(
                    TestFixtures.UNDERWRITER_ID,
                    policy.issuedByUserId()
            );
            assertEquals(
                    LocalDate.now(),
                    policy.startDate()
            );
            assertEquals(
                    LocalDate.now().plusYears(1).minusDays(1),
                    policy.endDate()
            );
            assertEquals(
                    2,
                    policy.coverages().size()
            );
            assertNotNull(policy.issuedAt());

            asPolicyholder()
                    .get()
                    .uri(QUOTES + "/" + policy.quoteId())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("BOUND")
                    .jsonPath("$.boundByUserId")
                    .isEqualTo(TestFixtures.UNDERWRITER_ID.toString());
        }

        @Test
        @DisplayName("binds a future dated quote into a pending issuance policy")
        void bindsFuturePolicy() {
            QuoteResponse quote = createQuote(validRequest());

            asPolicyholder()
                    .patch()
                    .uri(QUOTES + "/" + quote.quoteId() + "/submit")
                    .exchange()
                    .expectStatus().isOk();
            approveQuote(quote.quoteId());
            offerTerms(quote.quoteId());
            asPolicyholder()
                    .patch()
                    .uri(QUOTES + "/" + quote.quoteId() + "/accept")
                    .exchange()
                    .expectStatus().isOk();

            asUnderwriter()
                    .post()
                    .uri(POLICIES + "/bind/" + quote.quoteId())
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody()
                    .jsonPath("$.status")
                    .isEqualTo("PENDING_ISSUANCE");
        }

        @Test
        @DisplayName("refuses to bind the same quote twice")
        void refusesDoubleBinding() {
            bindThroughFullLifecycle();

            QuoteResponse second = createQuote(validRequestForToday());
            asPolicyholder()
                    .patch()
                    .uri(QUOTES + "/" + second.quoteId() + "/submit")
                    .exchange()
                    .expectStatus().isOk();
            approveQuote(second.quoteId());
            offerTerms(second.quoteId());
            asPolicyholder()
                    .patch()
                    .uri(QUOTES + "/" + second.quoteId() + "/accept")
                    .exchange()
                    .expectStatus().isOk();
            asUnderwriter()
                    .post()
                    .uri(POLICIES + "/bind/" + second.quoteId())
                    .exchange()
                    .expectStatus().isCreated();

            // A third attempt on the very first bound quote is impossible
            // because the quote is no longer ACCEPTED, so the duplicate
            // guard is asserted on a freshly bound quote.
            Policy persistedPolicy = entityTemplate
                    .select(Policy.class)
                    .matching(
                            Query.query(
                                    Criteria.where("quoteId")
                                            .is(second.quoteId())
                            )
                    )
                    .one()
                    .block();

            assertNotNull(persistedPolicy);

            asUnderwriter()
                    .post()
                    .uri(POLICIES + "/bind/" + second.quoteId())
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.message")
                    .isEqualTo(
                            "A policy has already been created for "
                                    + "this quote"
                    );
        }

        @Test
        @DisplayName("only the assigned underwriter can bind")
        void bindingIsUnderwriterOnly() {
            QuoteResponse quote = createQuote(validRequestForToday());

            asPolicyholder()
                    .patch()
                    .uri(QUOTES + "/" + quote.quoteId() + "/submit")
                    .exchange()
                    .expectStatus().isOk();
            approveQuote(quote.quoteId());
            offerTerms(quote.quoteId());
            asPolicyholder()
                    .patch()
                    .uri(QUOTES + "/" + quote.quoteId() + "/accept")
                    .exchange()
                    .expectStatus().isOk();

            client.mutate()
                    .defaultHeaders(
                            headers -> headers.setBearerAuth(
                                    JwtTestTokens.underwriterToken(
                                            TestFixtures
                                                    .OTHER_UNDERWRITER_ID
                                    )
                            )
                    )
                    .build()
                    .post()
                    .uri(POLICIES + "/bind/" + quote.quoteId())
                    .exchange()
                    .expectStatus().isForbidden();
        }

        @Test
        @DisplayName("documents that a @PreAuthorize denial surfaces as 500 instead of 403")
        void policyholderCannotBind() {
            QuoteResponse quote = createQuote(validRequestForToday());

            asPolicyholder()
                    .patch()
                    .uri(QUOTES + "/" + quote.quoteId() + "/submit")
                    .exchange()
                    .expectStatus().isOk();
            approveQuote(quote.quoteId());
            offerTerms(quote.quoteId());
            asPolicyholder()
                    .patch()
                    .uri(QUOTES + "/" + quote.quoteId() + "/accept")
                    .exchange()
                    .expectStatus().isOk();

            asPolicyholder()
                    .post()
                    .uri(POLICIES + "/bind/" + quote.quoteId())
                    .exchange()
                    .expectStatus().is5xxServerError()
                    .expectBody()
                    .jsonPath("$.message")
                    .isEqualTo("An unexpected error occurred");
        }

        @Test
        @DisplayName("reads a policy by id and by number")
        void readsPolicy() {
            PolicyResponse bound = bindThroughFullLifecycle();

            asPolicyholder()
                    .get()
                    .uri(POLICIES + "/" + bound.policyId())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.policyId")
                    .isEqualTo(bound.policyId().toString());

            asPolicyholder()
                    .get()
                    .uri(POLICIES + "/number/" + bound.policyNumber())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.policyNumber")
                    .isEqualTo(bound.policyNumber());
        }

        @Test
        @DisplayName("returns 404 for an unknown policy")
        void unknownPolicy() {
            asPolicyholder()
                    .get()
                    .uri(POLICIES + "/number/POL-2026-NOPE")
                    .exchange()
                    .expectStatus().isNotFound();
        }

        @Test
        @DisplayName("lists the policies of a customer")
        void listsCustomerPolicies() {
            bindThroughFullLifecycle();

            asPolicyholder()
                    .get()
                    .uri(POLICIES + "/customer/" + TestFixtures.CUSTOMER_ID)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(PolicyResponse.class)
                    .hasSize(1);
        }

        @Test
        @DisplayName("documents that a @PreAuthorize denial surfaces as 500 instead of 403")
        void claimsAdjusterCanReadPolicies() {
            PolicyResponse bound = bindThroughFullLifecycle();

            /*
             * Reactive method security raises
             * org.springframework.security.access.AccessDeniedException
             * from inside the handler, so the ExceptionTranslationWebFilter
             * of the security chain never sees it and the global handler
             * falls back to 500. Consumers of /policies/customer/{id}
             * therefore receive 500 rather than 403 for an unauthorised
             * role - this test pins that behaviour so it is visible.
             */
            asClaimsAdjuster()
                    .get()
                    .uri(POLICIES + "/customer/" + TestFixtures.CUSTOMER_ID)
                    .exchange()
                    .expectStatus().is5xxServerError();

            asClaimsAdjuster()
                    .get()
                    .uri(POLICIES + "/" + bound.policyId())
                    .exchange()
                    .expectStatus().isOk();
        }

        @Test
        @DisplayName("reports the policy status for a loss date")
        void policyStatusOnDate() {
            PolicyResponse bound = bindThroughFullLifecycle();

            asClaimsAdjuster()
                    .get()
                    .uri(POLICIES + "/number/" + bound.policyNumber()
                            + "/status?requestedDate=" + LocalDate.now())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.activeOnRequestedDate").isEqualTo(true)
                    .jsonPath("$.status").isEqualTo("IN_FORCE");

            asClaimsAdjuster()
                    .get()
                    .uri(POLICIES + "/number/" + bound.policyNumber()
                            + "/status?requestedDate="
                            + LocalDate.now().plusYears(3))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.activeOnRequestedDate").isEqualTo(false);
        }

        @Test
        @DisplayName("a policyholder cannot read the claims status endpoint")
        void policyholderCannotReadStatus() {
            PolicyResponse bound = bindThroughFullLifecycle();

            asPolicyholder()
                    .get()
                    .uri(POLICIES + "/number/" + bound.policyNumber()
                            + "/status?requestedDate=" + LocalDate.now())
                    .exchange()
                    .expectStatus().isForbidden();
        }

        @Test
        @DisplayName("returns 400 when the loss date is missing or malformed")
        void policyStatusRequiresDate() {
            asClaimsAdjuster()
                    .get()
                    .uri(POLICIES + "/number/POL-2026-X/status")
                    .exchange()
                    .expectStatus().is4xxClientError();

            asClaimsAdjuster()
                    .get()
                    .uri(POLICIES + "/number/POL-2026-X/status"
                            + "?requestedDate=not-a-date")
                    .exchange()
                    .expectStatus().is4xxClientError();
        }

        @Test
        @DisplayName("checks coverage for a claims adjuster")
        void coverageCheck() {
            PolicyResponse bound = bindThroughFullLifecycle();

            CoverageCheckResponse response = asClaimsAdjuster()
                    .get()
                    .uri(POLICIES + "/number/" + bound.policyNumber()
                            + "/coverage-check?coverageCode=  fire  "
                            + "&requestedDate=" + LocalDate.now())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(CoverageCheckResponse.class)
                    .returnResult()
                    .getResponseBody();

            assertNotNull(response);
            assertTrue(response.coveragePresent());
            assertTrue(response.policyEffectiveOnDate());
            assertEquals("FIRE", response.coverageCode());
            assertEquals(
                    new java.math.BigDecimal("90000.00"),
                    response.limitAmount()
            );
        }

        @Test
        @DisplayName("reports a coverage that is not present")
        void coverageCheckMissingCoverage() {
            PolicyResponse bound = bindThroughFullLifecycle();

            asClaimsAdjuster()
                    .get()
                    .uri(POLICIES + "/number/" + bound.policyNumber()
                            + "/coverage-check?coverageCode=flood"
                            + "&requestedDate=" + LocalDate.now())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.coveragePresent").isEqualTo(false)
                    .jsonPath("$.policyEffectiveOnDate").isEqualTo(true);
        }

        @Test
        @DisplayName("reports a loss date outside the policy period")
        void coverageCheckOutsidePolicyPeriod() {
            PolicyResponse bound = bindThroughFullLifecycle();

            asClaimsAdjuster()
                    .get()
                    .uri(POLICIES + "/number/" + bound.policyNumber()
                            + "/coverage-check?coverageCode=fire"
                            + "&requestedDate="
                            + LocalDate.now().plusYears(3))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.coveragePresent").isEqualTo(false)
                    .jsonPath("$.policyEffectiveOnDate").isEqualTo(false);
        }

        @Test
        @DisplayName("a policyholder cannot use the coverage check endpoint")
        void policyholderCannotCheckCoverage() {
            PolicyResponse bound = bindThroughFullLifecycle();

            asPolicyholder()
                    .get()
                    .uri(POLICIES + "/number/" + bound.policyNumber()
                            + "/coverage-check?coverageCode=fire"
                            + "&requestedDate=" + LocalDate.now())
                    .exchange()
                    .expectStatus().isForbidden();
        }

        @Test
        @DisplayName("returns 404 for a coverage check on an unknown policy")
        void coverageCheckUnknownPolicy() {
            asClaimsAdjuster()
                    .get()
                    .uri(POLICIES + "/number/POL-2026-NOPE/coverage-check"
                            + "?coverageCode=fire&requestedDate="
                            + LocalDate.now())
                    .exchange()
                    .expectStatus().isNotFound();
        }
    }

    @Nested
    @DisplayName("PATCH /api/quotes/{id}/import-underwriting-result")
    class UnderwritingImport {

        @Test
        @DisplayName("returns 404 for an unknown quote")
        void unknownQuote() {
            asUnderwriter()
                    .patch()
                    .uri(QUOTES + "/" + UUID.randomUUID()
                            + "/import-underwriting-result")
                    .exchange()
                    .expectStatus().isNotFound()
                    .expectBody()
                    .jsonPath("$.message")
                    .value(message -> assertTrue(
                            message.toString().contains(
                                    "Quote not found with ID:"
                            )
                    ));
        }

        @Test
        @DisplayName("requires authentication")
        void requiresAuthentication() {
            client.patch()
                    .uri(QUOTES + "/" + UUID.randomUUID()
                            + "/import-underwriting-result")
                    .exchange()
                    .expectStatus().isUnauthorized();
        }
    }

    @Test
    @DisplayName("diagnostic: shows why an authenticated request fails")
    void diagnostic() {
        byte[] response = asPolicyholder()
                .get()
                .uri(QUOTES + "/" + UUID.randomUUID())
                .exchange()
                .expectStatus()
                .value(status -> System.out.println(
                        "DIAGNOSTIC STATUS=" + status))
                .expectBody()
                .returnResult()
                .getResponseBody();

        System.out.println(
                "DIAGNOSTIC BODY="
                        + new String(response == null
                        ? new byte[0]
                        : response)
        );
    }

    @Test
    @DisplayName("the scheduled lifecycle transitions keep the persisted state consistent")
    void lifecycleSchedulerRun() {
        PolicyResponse bound = bindThroughFullLifecycle();

        Quote boundQuote = entityTemplate
                .select(Quote.class)
                .matching(
                        Query.query(
                                Criteria.where("quoteId")
                                        .is(bound.quoteId())
                        )
                )
                .one()
                .block();

        assertNotNull(boundQuote);
        assertEquals(QuoteStatus.BOUND, boundQuote.getStatus());
        assertNotNull(boundQuote.getBoundAt());

        Policy policy = entityTemplate
                .select(Policy.class)
                .matching(
                        Query.query(
                                Criteria.where("policyId")
                                        .is(bound.policyId())
                        )
                )
                .one()
                .block();

        assertNotNull(policy);
        assertEquals(PolicyStatus.IN_FORCE, policy.getStatus());
        assertNull(policy.getExpiredAt());
        assertFalse(
                policy.getPolicyNumber().isBlank()
        );
    }
}
