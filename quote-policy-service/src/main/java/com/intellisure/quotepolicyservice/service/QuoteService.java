package com.intellisure.quotepolicyservice.service;

import com.intellisure.quotepolicyservice.dto.request.AssignUnderwriterRequest;
import com.intellisure.quotepolicyservice.dto.request.CreateQuoteCoverageRequest;
import com.intellisure.quotepolicyservice.dto.request.CreateQuoteRequest;
import com.intellisure.quotepolicyservice.dto.response.QuoteResponse;
import com.intellisure.quotepolicyservice.entity.Quote;
import com.intellisure.quotepolicyservice.entity.QuoteCoverage;
import com.intellisure.quotepolicyservice.entity.QuoteVersion;
import com.intellisure.quotepolicyservice.enums.QuoteStatus;
import com.intellisure.quotepolicyservice.exception.BusinessException;
import com.intellisure.quotepolicyservice.exception.ResourceNotFoundException;
import com.intellisure.quotepolicyservice.mapper.QuoteMapper;
import com.intellisure.quotepolicyservice.repository.QuoteCoverageRepository;
import com.intellisure.quotepolicyservice.repository.QuoteRepository;
import com.intellisure.quotepolicyservice.repository.QuoteVersionRepository;
import com.intellisure.quotepolicyservice.repository.SubjectivityRepository;
import com.intellisure.quotepolicyservice.service.assignment.UnderwriterAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.intellisure.quotepolicyservice.dto.request.OfferQuoteTermsRequest;
import com.intellisure.quotepolicyservice.dto.request.OfferedCoverageRequest;
import com.intellisure.quotepolicyservice.dto.request.RecordUnderwritingDecisionRequest;
import com.intellisure.quotepolicyservice.dto.response.UnderwritingDecisionResponse;
import com.intellisure.quotepolicyservice.entity.UnderwritingDecision;
import com.intellisure.quotepolicyservice.enums.UnderwritingDecisionType;
import com.intellisure.quotepolicyservice.repository.UnderwritingDecisionRepository;
import com.intellisure.quotepolicyservice.dto.request.AcceptQuoteRequest;
import com.intellisure.quotepolicyservice.dto.request.DeclineQuoteRequest;
import com.intellisure.quotepolicyservice.exception.AccessDeniedBusinessException;
import com.intellisure.quotepolicyservice.security.SecurityActorService;
import org.springframework.security.access.prepost.PreAuthorize;

import java.math.BigDecimal;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final QuoteCoverageRepository quoteCoverageRepository;
    private final R2dbcEntityTemplate entityTemplate;
    private final QuoteMapper quoteMapper;
    private final UnderwriterAssignmentService underwriterAssignmentService;
    private final UnderwritingDecisionRepository underwritingDecisionRepository;
    private final SecurityActorService securityActorService;
    private final QuoteVersionRepository quoteVersionRepository;
    private final SubjectivityRepository subjectivityRepository;
    private final RatingService ratingService;

    /*
     * ---------------------------------------------------------
     * CREATE DRAFT QUOTE
     * ---------------------------------------------------------
     */

    @PreAuthorize("hasRole('POLICYHOLDER')")
    @Transactional
    public Mono<QuoteResponse> createDraftQuote(
            CreateQuoteRequest request
    ) {
        return securityActorService
                .currentCustomerId()
                .flatMap(authenticatedCustomerId -> {
                    if (!authenticatedCustomerId.equals(
                            request.customerId()
                    )) {
                        return Mono.error(
                                new AccessDeniedBusinessException(
                                        "A policyholder can only create "
                                                + "quotes for the authenticated "
                                                + "customer account"
                                )
                        );
                    }

                    return createDraftQuoteInternal(request);
                });
    }

    /*
     * ---------------------------------------------------------
     * SUBMIT QUOTE
     * DRAFT -> SUBMITTED
     * ---------------------------------------------------------
     */

    @PreAuthorize("hasRole('POLICYHOLDER')")
    @Transactional
    public Mono<QuoteResponse> submitQuote(
            UUID quoteId
    ) {
        return getQuoteEntity(quoteId)
                .flatMap(quote ->
                        verifyQuoteCustomerOwnership(quote)
                                .thenReturn(quote)
                )
                .flatMap(quote -> {
                    validateRequiredStatus(
                            quote,
                            QuoteStatus.DRAFT,
                            "Only a DRAFT quote can be submitted"
                    );

                    LocalDateTime now =
                            LocalDateTime.now();

                    quote.setStatus(
                            QuoteStatus.SUBMITTED
                    );
                    quote.setSubmittedAt(now);
                    quote.setUpdatedAt(now);

                    return entityTemplate.update(quote);
                })
                .flatMap(submittedQuote ->
                        underwriterAssignmentService
                                .selectLeastLoadedUnderwriter()
                                .flatMap(underwriterId -> {
                                    submittedQuote
                                            .setAssignedUnderwriterId(
                                                    underwriterId
                                            );

                                    submittedQuote.setStatus(
                                            QuoteStatus.IN_REVIEW
                                    );

                                    submittedQuote.setUpdatedAt(
                                            LocalDateTime.now()
                                    );

                                    return entityTemplate.update(
                                            submittedQuote
                                    );
                                })
                                .switchIfEmpty(
                                        Mono.just(submittedQuote)
                                )
                )
                .flatMap(this::buildQuoteResponse);
    }
    /*
     * ---------------------------------------------------------
     * ASSIGN UNDERWRITER
     * Allowed while SUBMITTED or IN_REVIEW
     * ---------------------------------------------------------
     */

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Mono<QuoteResponse> reassignUnderwriter(
            UUID quoteId,
            AssignUnderwriterRequest request
    ) {
        return getQuoteEntity(quoteId)
                .flatMap(quote -> {
                    boolean allowedStatus =
                            quote.getStatus()
                                    == QuoteStatus.SUBMITTED
                                    || quote.getStatus()
                                    == QuoteStatus.IN_REVIEW
                                    || quote.getStatus()
                                    == QuoteStatus.NEEDS_INFORMATION
                                    || quote.getStatus()
                                    == QuoteStatus.RISK_ASSESSMENT;

                    if (!allowedStatus) {
                        return Mono.error(
                                new BusinessException(
                                        "Underwriter reassignment is "
                                                + "not allowed while the quote "
                                                + "is in status "
                                                + quote.getStatus()
                                )
                        );
                    }

                    quote.setAssignedUnderwriterId(
                            request.underwriterId()
                    );

                    if (quote.getStatus()
                            == QuoteStatus.SUBMITTED) {
                        quote.setStatus(
                                QuoteStatus.IN_REVIEW
                        );
                    }

                    quote.setUpdatedAt(
                            LocalDateTime.now()
                    );

                    return entityTemplate.update(quote);
                })
                .flatMap(this::buildQuoteResponse);
    }

    /*
     * ---------------------------------------------------------
     * START UNDERWRITING REVIEW
     * SUBMITTED -> IN_REVIEW
     * ---------------------------------------------------------
     */


    /*
     * ---------------------------------------------------------
     * GET OPERATIONS
     * ---------------------------------------------------------
     */

    public Mono<QuoteResponse> getQuoteById(UUID quoteId) {
        return getQuoteEntity(quoteId)
                .flatMap(quote -> securityActorService
                        .assertCustomerAccess(quote.getCustomerId())
                        .then(Mono.defer(() -> buildQuoteResponse(quote))));
    }

    public Mono<QuoteResponse> getQuoteByNumber(
            String quoteNumber
    ) {
        return quoteRepository
                .findByQuoteNumber(quoteNumber)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException(
                                "Quote not found with number: "
                                        + quoteNumber
                        )
                ))
                .flatMap(quote -> securityActorService
                        .assertCustomerAccess(quote.getCustomerId())
                        .then(Mono.defer(() -> buildQuoteResponse(quote))));
    }

    public Flux<QuoteResponse> getQuotesByCustomerId(
            UUID customerId
    ) {
        return securityActorService
                .assertCustomerAccess(customerId)
                .thenMany(quoteRepository
                        .findAllByCustomerId(customerId)
                        .flatMap(this::buildQuoteResponse));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_ADMINISTRATOR')")
    public Flux<QuoteResponse> getAllQuotesForAdministration() {
        return quoteRepository.findAll().flatMap(this::buildQuoteResponse);
    }


    @PreAuthorize("hasRole('UNDERWRITER')")
    @Transactional
    public Mono<QuoteResponse> offerQuoteTerms(
            UUID quoteId,
            OfferQuoteTermsRequest request
    ) {
        validateDuplicateOfferedCoverageCodes(
                request.coverages()
        );

        return getQuoteEntity(quoteId)

                // Only the underwriter assigned to this quote
                // can offer commercial terms.
                .flatMap(quote ->
                        verifyAssignedUnderwriter(quote)
                                .thenReturn(quote)
                )

                .flatMap(quote -> {
                    validateRequiredStatus(
                            quote,
                            QuoteStatus.IN_REVIEW,
                            "Terms can only be offered while "
                                    + "the quote is IN_REVIEW"
                    );

                    return underwritingDecisionRepository
                            .findFirstByQuoteIdOrderByDecidedAtDesc(
                                    quoteId
                            )
                            .switchIfEmpty(
                                    Mono.error(
                                            new BusinessException(
                                                    "An approved underwriting "
                                                            + "decision is required "
                                                            + "before terms can "
                                                            + "be offered"
                                            )
                                    )
                            )
                            .flatMap(decision -> {
                                validateApprovedDecision(
                                        decision
                                );

                                return applyOfferedTerms(
                                        quote,
                                        request
                                );
                            });
                });
    }


    @PreAuthorize("hasRole('POLICYHOLDER')")
    @Transactional
    public Mono<QuoteResponse> acceptQuote(
            UUID quoteId
    ) {
        return getQuoteEntity(quoteId)

                // Confirm that the authenticated customer owns the quote.
                .flatMap(quote ->
                        verifyQuoteCustomerOwnership(quote)
                                .thenReturn(quote)
                )

                // Get the authenticated user account ID from JWT subject.
                .flatMap(quote ->
                        securityActorService
                                .currentUserId()
                                .flatMap(authenticatedUserId -> {
                                    validateRequiredStatus(
                                            quote,
                                            QuoteStatus.QUOTED,
                                            "Only a QUOTED quote "
                                                    + "can be accepted"
                                    );

                                    validateQuoteNotExpired(
                                            quote
                                    );

                                    if (quote.getTotalPremium() == null) {
                                        return Mono.error(
                                                new BusinessException(
                                                        "The quote does not "
                                                                + "have a final "
                                                                + "premium"
                                                )
                                        );
                                    }

                                    LocalDateTime now =
                                            LocalDateTime.now();

                                    quote.setStatus(
                                            QuoteStatus.ACCEPTED
                                    );

                                    quote.setAcceptedByUserId(
                                            authenticatedUserId
                                    );

                                    quote.setAcceptedAt(now);
                                    quote.setUpdatedAt(now);

                                    return entityTemplate.update(
                                            quote
                                    );
                                })
                )

                .flatMap(this::buildQuoteResponse);
    }



    @PreAuthorize("hasRole('POLICYHOLDER')")
    @Transactional
    public Mono<QuoteResponse> declineQuoteByCustomer(
            UUID quoteId,
            DeclineQuoteRequest request
    ) {
        return getQuoteEntity(quoteId)

                // Confirm that the authenticated customer owns the quote.
                .flatMap(quote ->
                        verifyQuoteCustomerOwnership(quote)
                                .thenReturn(quote)
                )

                .flatMap(quote -> {
                    validateRequiredStatus(
                            quote,
                            QuoteStatus.QUOTED,
                            "Only a QUOTED quote can be "
                                    + "declined by the customer"
                    );

                    validateQuoteNotExpired(
                            quote
                    );

                    LocalDateTime now = LocalDateTime.now();

                    quote.setStatus(
                            QuoteStatus.DECLINED_BY_CUSTOMER
                    );

                    quote.setDeclineReason(
                            request.reason().trim()
                    );

                    quote.setUpdatedAt(now);

                    return entityTemplate.update(
                            quote
                    );
                })

                .flatMap(this::buildQuoteResponse);
    }



    public void validateStatusTransition(
            QuoteStatus currentStatus,
            QuoteStatus targetStatus
    ) {
        if (currentStatus == null || targetStatus == null) {
            throw new BusinessException("Current and target status must not be null");
        }
        if (currentStatus == targetStatus) {
            return;
        }

        // Terminal states cannot transition
        if (currentStatus == QuoteStatus.ISSUED
                || currentStatus == QuoteStatus.DECLINED_BY_INSURER
                || currentStatus == QuoteStatus.DECLINED_BY_CUSTOMER
                || currentStatus == QuoteStatus.WITHDRAWN
                || currentStatus == QuoteStatus.EXPIRED) {
            throw new BusinessException("Cannot transition a " + currentStatus + " quote to " + targetStatus);
        }

        // Direct jumps from DRAFT
        if (currentStatus == QuoteStatus.DRAFT &&
                (targetStatus == QuoteStatus.ACCEPTED
                        || targetStatus == QuoteStatus.BOUND
                        || targetStatus == QuoteStatus.ISSUED
                        || targetStatus == QuoteStatus.QUOTED)) {
            throw new BusinessException("Cannot transition directly from DRAFT to " + targetStatus
                    + " without submission, review, and quote offering");
        }

        // Direct jumps from SUBMITTED
        if (currentStatus == QuoteStatus.SUBMITTED &&
                (targetStatus == QuoteStatus.ACCEPTED
                        || targetStatus == QuoteStatus.BOUND
                        || targetStatus == QuoteStatus.ISSUED)) {
            throw new BusinessException("Cannot transition directly from SUBMITTED to " + targetStatus
                    + " without underwriting review and quote offering");
        }

        // Direct jumps from REVIEW states
        if ((currentStatus == QuoteStatus.IN_REVIEW || currentStatus == QuoteStatus.UNDER_REVIEW) &&
                (targetStatus == QuoteStatus.ACCEPTED
                        || targetStatus == QuoteStatus.BOUND
                        || targetStatus == QuoteStatus.ISSUED)) {
            throw new BusinessException("Cannot transition directly from review to " + targetStatus
                    + " without terms being offered first (QUOTED)");
        }

        // Direct jump from QUOTED to BOUND or ISSUED without acceptance
        if (currentStatus == QuoteStatus.QUOTED &&
                (targetStatus == QuoteStatus.BOUND || targetStatus == QuoteStatus.ISSUED)) {
            throw new BusinessException("A QUOTED quote must be ACCEPTED before it can be bound or issued");
        }

        // Cannot jump from ACCEPTED to ISSUED without being BOUND
        if (currentStatus == QuoteStatus.ACCEPTED && targetStatus == QuoteStatus.ISSUED) {
            throw new BusinessException("An ACCEPTED quote must be BOUND before policy issuance");
        }
    }

    public void validateStatusTransition(String currentStatus, String targetStatus) {
        if (currentStatus == null || targetStatus == null) {
            throw new BusinessException("Current and target status must not be null");
        }
        try {
            QuoteStatus current = QuoteStatus.valueOf(currentStatus.trim().toUpperCase());
            QuoteStatus target = QuoteStatus.valueOf(targetStatus.trim().toUpperCase());
            validateStatusTransition(current, target);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid status provided: " + e.getMessage());
        }
    }

    private void validateQuoteNotExpired(
            Quote quote
    ) {
        if (quote.getQuoteExpiresAt() == null) {
            throw new BusinessException(
                    "The quote does not have an expiration date"
            );
        }

        if (!quote.getQuoteExpiresAt()
                .isAfter(LocalDateTime.now())) {
            throw new BusinessException(
                    "The quote has expired and can no longer "
                            + "be accepted or declined"
            );
        }
    }


    @Transactional
    public Mono<Long> expireQuotedOffers() {
        LocalDateTime now = LocalDateTime.now();

        return quoteRepository
                .findAllByStatusAndQuoteExpiresAtLessThanEqual(
                        QuoteStatus.QUOTED,
                        now
                )
                .concatMap(quote -> {
                    quote.setStatus(QuoteStatus.EXPIRED);
                    quote.setUpdatedAt(now);

                    return entityTemplate.update(quote);
                })
                .count();
    }

    @PreAuthorize("hasRole('UNDERWRITER')")
    @Transactional
    public Mono<UnderwritingDecisionResponse>
    recordUnderwritingDecision(
            UUID quoteId,
            RecordUnderwritingDecisionRequest request
    ) {
        return getQuoteEntity(quoteId)

                // Verify the logged-in underwriter owns this assignment.
                .flatMap(quote ->
                        verifyAssignedUnderwriter(quote)
                                .thenReturn(quote)
                )

                .flatMap(quote -> {
                    validateRequiredStatus(
                            quote,
                            QuoteStatus.IN_REVIEW,
                            "An underwriting decision can only be "
                                    + "recorded while the quote is IN_REVIEW"
                    );

                    if (quote.getAssignedUnderwriterId() == null) {
                        return Mono.error(
                                new BusinessException(
                                        "The quote does not have an "
                                                + "assigned underwriter"
                                )
                        );
                    }

                    LocalDateTime now = LocalDateTime.now();

                    UnderwritingDecision decision =
                            UnderwritingDecision.builder()
                                    .underwritingDecisionId(
                                            UUID.randomUUID()
                                    )
                                    .quoteId(
                                            quote.getQuoteId()
                                    )
                                    .underwriterId(
                                            quote.getAssignedUnderwriterId()
                                    )
                                    .decision(
                                            request.decision()
                                    )
                                    .decisionReason(
                                            request.decisionReason()
                                                    .trim()
                                    )
                                    .authorityLevel(
                                            normalizeNullableText(
                                                    request.authorityLevel()
                                            )
                                    )
                                    .conditions(
                                            normalizeNullableText(
                                                    request.conditions()
                                            )
                                    )
                                    .decidedAt(now)
                                    .createdAt(now)
                                    .build();

                    return entityTemplate
                            .insert(UnderwritingDecision.class)
                            .using(decision)
                            .flatMap(insertedDecision ->
                                    applyDecisionStatus(
                                            quote,
                                            request
                                    )
                                            .thenReturn(
                                                    toDecisionResponse(
                                                            insertedDecision
                                                    )
                                            )
                            );
                });
    }


    public Flux<UnderwritingDecisionResponse>
    getUnderwritingDecisionHistory(
            UUID quoteId
    ) {
        return getQuoteEntity(quoteId)
                .flatMap(quote -> securityActorService
                        .assertCustomerAccess(quote.getCustomerId()))
                .thenMany(
                        underwritingDecisionRepository
                                .findAllByQuoteId(quoteId)
                                .sort(
                                        (first, second) ->
                                                second.getDecidedAt()
                                                        .compareTo(
                                                                first.getDecidedAt()
                                                        )
                                )
                                .map(this::toDecisionResponse)
                );
    }


    /*
     * ---------------------------------------------------------
     * PRIVATE HELPERS
     * ---------------------------------------------------------
     */



    private Mono<Void> verifyQuoteCustomerOwnership(
            Quote quote
    ) {
        return securityActorService
                .currentCustomerId()
                .flatMap(authenticatedCustomerId -> {

                    if (!authenticatedCustomerId.equals(
                            quote.getCustomerId()
                    )) {
                        return Mono.error(
                                new AccessDeniedBusinessException(
                                        "The authenticated customer "
                                                + "does not own this quote"
                                )
                        );
                    }

                    return Mono.empty();
                });
    }

    private Mono<QuoteResponse> createDraftQuoteInternal(
            CreateQuoteRequest request
    ) {
        validateDuplicateCoverageCodes(
                request.coverages()
        );

        LocalDateTime now = LocalDateTime.now();
        UUID quoteId = UUID.randomUUID();

        Quote quote = Quote.builder()
                .quoteId(quoteId)
                .quoteNumber(generateQuoteNumber())
                .customerId(request.customerId())
                .productCode(request.productCode().trim())
                .insuranceNeed(request.insuranceNeed().trim())
                .businessOperations(
                        request.businessOperations().trim()
                )
                .status(QuoteStatus.DRAFT)
                .requestedEffectiveDate(
                        request.requestedEffectiveDate()
                )
                .createdAt(now)
                .updatedAt(now)
                .build();

        List<QuoteCoverage> coverages =
                request.coverages()
                        .stream()
                        .map(coverageRequest ->
                                createCoverage(
                                        quoteId,
                                        coverageRequest,
                                        now
                                )
                        )
                        .toList();

        return entityTemplate
                .insert(Quote.class)
                .using(quote)
                .flatMap(insertedQuote ->
                        Flux.fromIterable(coverages)
                                .concatMap(coverage ->
                                        entityTemplate
                                                .insert(
                                                        QuoteCoverage.class
                                                )
                                                .using(coverage)
                                )
                                .collectList()
                                .map(savedCoverages ->
                                        quoteMapper.toResponse(
                                                insertedQuote,
                                                savedCoverages
                                        )
                                )
                );
    }

    private Mono<QuoteResponse> applyOfferedTerms(
            Quote quote,
            OfferQuoteTermsRequest request
    ) {
        Map<String, OfferedCoverageRequest> offeredTerms =
                request.coverages()
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        offeredCoverage ->
                                                offeredCoverage
                                                        .coverageCode()
                                                        .trim()
                                                        .toUpperCase(),
                                        Function.identity()
                                )
                        );

        return quoteCoverageRepository
                .findAllByQuoteId(quote.getQuoteId())
                .collectList()
                .flatMap(existingCoverages -> {

                    if (existingCoverages.isEmpty()) {
                        return Mono.error(
                                new BusinessException(
                                        "The quote does not contain "
                                                + "any requested coverages"
                                )
                        );
                    }

                    validateOfferedCoverageCompleteness(
                            existingCoverages,
                            offeredTerms
                    );

                    LocalDateTime now = LocalDateTime.now();

                    List<QuoteCoverage> updatedCoverages =
                            existingCoverages
                                    .stream()
                                    .map(existingCoverage -> {
                                        String coverageCode =
                                                existingCoverage
                                                        .getCoverageCode()
                                                        .trim()
                                                        .toUpperCase();

                                        OfferedCoverageRequest offered =
                                                offeredTerms.get(
                                                        coverageCode
                                                );

                                        existingCoverage.setOfferedLimit(
                                                offered.offeredLimit()
                                        );

                                        existingCoverage
                                                .setOfferedDeductible(
                                                        offered
                                                                .offeredDeductible()
                                                );

                                        existingCoverage
                                                .setCoveragePremium(
                                                        offered.coveragePremium()
                                                );

                                        existingCoverage.setConditions(
                                                normalizeNullableText(
                                                        offered.conditions()
                                                )
                                        );

                                        existingCoverage.setExclusions(
                                                normalizeNullableText(
                                                        offered.exclusions()
                                                )
                                        );

                                        existingCoverage
                                                .setWaitingPeriodDays(
                                                        offered
                                                                .waitingPeriodDays()
                                                );

                                        existingCoverage.setUpdatedAt(
                                                now
                                        );

                                        return existingCoverage;
                                    })
                                    .toList();

                    BigDecimal totalPremium =
                            updatedCoverages
                                    .stream()
                                    .map(
                                            QuoteCoverage::getCoveragePremium
                                    )
                                    .reduce(
                                            BigDecimal.ZERO,
                                            BigDecimal::add
                                    );

                    return Flux.fromIterable(updatedCoverages)
                            .concatMap(
                                    coverage ->
                                            entityTemplate.update(
                                                    coverage
                                            )
                            )
                            .collectList()
                            .flatMap(savedCoverages -> {
                                // Save quote version before applying new terms
                                Long newVersion = (quote.getVersion() == null ? 0L : quote.getVersion()) + 1;
                                return saveQuoteVersion(quote, newVersion, savedCoverages, quote.getAssignedUnderwriterId())
                                        .then(
                                                Mono.defer(() -> {
                                                    quote.setVersion(newVersion);
                                                    quote.setTotalPremium(totalPremium);

                                                    quote.setQuoteExpiresAt(request.quoteExpiresAt());

                                                    quote.setQuotedAt(now);

                                                    quote.setStatus(QuoteStatus.QUOTED);

                                                    quote.setUpdatedAt(now);

                                                    return entityTemplate
                                                            .update(quote)
                                                            .map(updatedQuote ->
                                                                    quoteMapper.toResponse(updatedQuote, savedCoverages)
                                                            );
                                                })
                                        );
                            });
                });
    }

    private Mono<Void> saveQuoteVersion(Quote quote, Long version, List<QuoteCoverage> coverages, UUID offeredByUserId) {
        QuoteVersion quoteVersion = new QuoteVersion(
                UUID.randomUUID(),
                quote.getQuoteId(),
                version,
                quote.getTotalPremium(),
                coverages.toString(), // Simplified - use ObjectMapper in production
                offeredByUserId,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        return quoteVersionRepository.save(quoteVersion).then();
    }


    private void validateApprovedDecision(
            UnderwritingDecision decision
    ) {
        boolean approved =
                decision.getDecision()
                        == UnderwritingDecisionType.APPROVED
                        || decision.getDecision()
                        == UnderwritingDecisionType
                        .APPROVED_WITH_MODIFIED_TERMS;

        if (!approved) {
            throw new BusinessException(
                    "The latest underwriting decision must be "
                            + "APPROVED or "
                            + "APPROVED_WITH_MODIFIED_TERMS. "
                            + "Current decision: "
                            + decision.getDecision()
            );
        }
    }



    private Mono<Quote> applyDecisionStatus(
            Quote quote,
            RecordUnderwritingDecisionRequest request
    ) {
        LocalDateTime now = LocalDateTime.now();

        switch (request.decision()) {

            case MORE_INFORMATION_REQUIRED -> {
                quote.setStatus(
                        QuoteStatus.NEEDS_INFORMATION
                );

                quote.setDeclineReason(null);
            }

            case DECLINED -> {
                quote.setStatus(
                        QuoteStatus.DECLINED_BY_INSURER
                );

                quote.setDeclineReason(
                        request.decisionReason().trim()
                );
            }

            case APPROVED,
                 APPROVED_WITH_MODIFIED_TERMS,
                 REFERRED -> {
                /*
                 * Keep the quote in IN_REVIEW.
                 *
                 * APPROVED and APPROVED_WITH_MODIFIED_TERMS
                 * require offered commercial terms before the
                 * quote can become QUOTED.
                 *
                 * REFERRED requires further underwriting review
                 * or approval by a higher authority.
                 */
            }
        }

        quote.setUpdatedAt(now);

        return entityTemplate.update(quote);
    }

    private Mono<Void> verifyAssignedUnderwriter(
            Quote quote
    ) {
        return securityActorService
                .currentUserId()
                .flatMap(authenticatedUserId -> {
                    if (!authenticatedUserId.equals(
                            quote.getAssignedUnderwriterId()
                    )) {
                        return Mono.error(
                                new AccessDeniedBusinessException(
                                        "The authenticated underwriter "
                                                + "is not assigned to this quote"
                                )
                        );
                    }

                    return Mono.empty();
                });
    }


    private Mono<Quote> getQuoteEntity(UUID quoteId) {
        return quoteRepository
                .findById(quoteId)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException(
                                "Quote not found with ID: " + quoteId
                        )
                ));
    }

    private Mono<QuoteResponse> buildQuoteResponse(
            Quote quote
    ) {
        return quoteCoverageRepository
                .findAllByQuoteId(quote.getQuoteId())
                .collectList()
                .map(coverages ->
                        quoteMapper.toResponse(
                                quote,
                                coverages
                        )
                );
    }




    private UnderwritingDecisionResponse toDecisionResponse(
            UnderwritingDecision decision
    ) {
        return new UnderwritingDecisionResponse(
                decision.getUnderwritingDecisionId(),
                decision.getQuoteId(),
                decision.getUnderwriterId(),
                decision.getDecision(),
                decision.getDecisionReason(),
                decision.getAuthorityLevel(),
                decision.getConditions(),
                decision.getDecidedAt(),
                decision.getCreatedAt()
        );
    }

    private void validateRequiredStatus(
            Quote quote,
            QuoteStatus requiredStatus,
            String message
    ) {
        if (quote.getStatus() != requiredStatus) {
            throw new BusinessException(
                    message
                            + ". Current status: "
                            + quote.getStatus()
            );
        }
    }





    private void validateOfferedCoverageCompleteness(
            List<QuoteCoverage> existingCoverages,
            Map<String, OfferedCoverageRequest> offeredTerms
    ) {
        Set<String> existingCodes =
                existingCoverages
                        .stream()
                        .map(coverage ->
                                coverage.getCoverageCode()
                                        .trim()
                                        .toUpperCase()
                        )
                        .collect(Collectors.toSet());

        Set<String> offeredCodes =
                offeredTerms.keySet();

        if (!existingCodes.equals(offeredCodes)) {
            Set<String> missingCodes =
                    new HashSet<>(existingCodes);

            missingCodes.removeAll(offeredCodes);

            Set<String> unknownCodes =
                    new HashSet<>(offeredCodes);

            unknownCodes.removeAll(existingCodes);

            throw new BusinessException(
                    "Offered coverages do not match the "
                            + "requested coverages. Missing: "
                            + missingCodes
                            + ", Unknown: "
                            + unknownCodes
            );
        }
    }

    private String normalizeNullableText(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }


    private void validateDuplicateOfferedCoverageCodes(
            List<OfferedCoverageRequest> coverages
    ) {
        Set<String> uniqueCodes = new HashSet<>();

        for (OfferedCoverageRequest coverage : coverages) {
            String normalizedCode =
                    coverage.coverageCode()
                            .trim()
                            .toUpperCase();

            if (!uniqueCodes.add(normalizedCode)) {
                throw new BusinessException(
                        "Duplicate offered coverage code: "
                                + normalizedCode
                );
            }
        }
    }



    private QuoteCoverage createCoverage(
            UUID quoteId,
            CreateQuoteCoverageRequest request,
            LocalDateTime now
    ) {
        return QuoteCoverage.builder()
                .quoteCoverageId(UUID.randomUUID())
                .quoteId(quoteId)
                .coverageCode(
                        request.coverageCode()
                                .trim()
                                .toUpperCase()
                )
                .coverageName(
                        request.coverageName().trim()
                )
                .requestedLimit(
                        request.requestedLimit()
                )
                .requestedDeductible(
                        request.requestedDeductible()
                )
                .waitingPeriodDays(
                        request.waitingPeriodDays()
                )
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private void validateDuplicateCoverageCodes(
            List<CreateQuoteCoverageRequest> coverages
    ) {
        Set<String> uniqueCodes = new HashSet<>();

        for (CreateQuoteCoverageRequest coverage : coverages) {
            String normalizedCode =
                    coverage.coverageCode()
                            .trim()
                            .toUpperCase();

            if (!uniqueCodes.add(normalizedCode)) {
                throw new BusinessException(
                        "Duplicate coverage code: "
                                + normalizedCode
                );
            }
        }
    }

    private String generateQuoteNumber() {
        return "QTE-"
                + Year.now().getValue()
                + "-"
                + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }
}
