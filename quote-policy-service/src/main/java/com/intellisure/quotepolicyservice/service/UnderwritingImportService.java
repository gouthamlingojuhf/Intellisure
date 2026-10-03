package com.intellisure.quotepolicyservice.service;

import com.intellisure.quotepolicyservice.client.RiskUnderwritingClient;
import com.intellisure.quotepolicyservice.client.dto.RiskAssessmentStatusClient;
import com.intellisure.quotepolicyservice.client.dto.UnderwritingOutcomeClient;
import com.intellisure.quotepolicyservice.client.dto.UnderwritingResultClientResponse;
import com.intellisure.quotepolicyservice.dto.response.ImportedUnderwritingResultResponse;
import com.intellisure.quotepolicyservice.entity.Quote;
import com.intellisure.quotepolicyservice.entity.UnderwritingDecision;
import com.intellisure.quotepolicyservice.enums.QuoteStatus;
import com.intellisure.quotepolicyservice.enums.UnderwritingDecisionType;
import com.intellisure.quotepolicyservice.exception.BusinessException;
import com.intellisure.quotepolicyservice.exception.ResourceNotFoundException;
import com.intellisure.quotepolicyservice.repository.QuoteRepository;
import com.intellisure.quotepolicyservice.repository.UnderwritingDecisionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UnderwritingImportService {

    private final QuoteRepository quoteRepository;

    private final UnderwritingDecisionRepository
            decisionRepository;

    private final RiskUnderwritingClient
            riskUnderwritingClient;

    private final R2dbcEntityTemplate entityTemplate;

    @Transactional
    public Mono<ImportedUnderwritingResultResponse>
    importResult(
            UUID quoteId,
            String authorizationHeader
    ) {
        return getQuote(quoteId)
                .flatMap(quote ->
                        riskUnderwritingClient
                                .getUnderwritingResult(
                                        quoteId,
                                        authorizationHeader
                                )
                                .flatMap(result ->
                                        importValidatedResult(
                                                quote,
                                                result
                                        )
                                )
                );
    }

    private Mono<ImportedUnderwritingResultResponse>
    importValidatedResult(
            Quote quote,
            UnderwritingResultClientResponse result
    ) {
        validateResultMatchesQuote(
                quote,
                result
        );

        validateFinalResult(result);

        return decisionRepository
                .findBySourceDecisionId(
                        result.decisionId()
                )
                .map(existingDecision ->
                        toResponse(
                                quote,
                                result,
                                mapOutcome(result.outcome()),
                                true
                        )
                )
                .switchIfEmpty(
                        Mono.defer(() ->
                                insertSnapshotAndUpdateQuote(
                                        quote,
                                        result
                                )
                        )
                );
    }

    private Mono<ImportedUnderwritingResultResponse>
    insertSnapshotAndUpdateQuote(
            Quote quote,
            UnderwritingResultClientResponse result
    ) {
        LocalDateTime now = LocalDateTime.now();

        UnderwritingDecisionType localDecision =
                mapOutcome(result.outcome());

        UnderwritingDecision snapshot =
                UnderwritingDecision.builder()
                        .underwritingDecisionId(
                                UUID.randomUUID()
                        )
                        .sourceDecisionId(
                                result.decisionId()
                        )
                        .quoteId(
                                quote.getQuoteId()
                        )
                        .underwriterId(
                                quote.getAssignedUnderwriterId()
                        )
                        .decision(localDecision)
                        .decisionReason(
                                result.decisionRationale()
                        )
                        .authorityLevel(
                                result.authorityLevel()
                        )
                        .conditions(
                                result.conditions()
                        )
                        .decidedAt(
                                result.decidedAt()
                        )
                        .createdAt(now)
                        .build();

        return entityTemplate
                .insert(UnderwritingDecision.class)
                .using(snapshot)
                .flatMap(insertedSnapshot -> {
                    quote.setRiskAssessmentId(
                            result.assessmentId()
                    );

                    applyResultToQuote(
                            quote,
                            result.outcome()
                    );

                    if (result.outcome()
                            == UnderwritingOutcomeClient.DECLINED) {
                        quote.setDeclineReason(
                                result.decisionRationale()
                        );
                    }

                    quote.setUpdatedAt(now);

                    return entityTemplate
                            .update(quote)
                            .map(updatedQuote ->
                                    toResponse(
                                            updatedQuote,
                                            result,
                                            localDecision,
                                            false
                                    )
                            );
                });
    }

    private void validateResultMatchesQuote(
            Quote quote,
            UnderwritingResultClientResponse result
    ) {
        if (!quote.getQuoteId().equals(
                result.quoteId()
        )) {
            throw new BusinessException(
                    "The underwriting result does not "
                            + "belong to the requested quote"
            );
        }

        if (result.decisionId() == null
                || result.assessmentId() == null
                || result.outcome() == null) {
            throw new BusinessException(
                    "The underwriting result is incomplete"
            );
        }
    }

    private void validateFinalResult(
            UnderwritingResultClientResponse result
    ) {
        if (result.assessmentStatus()
                != RiskAssessmentStatusClient.COMPLETED) {
            throw new BusinessException(
                    "Only a COMPLETED risk assessment "
                            + "can be imported. Current status: "
                            + result.assessmentStatus()
            );
        }

        boolean finalOutcome =
                result.outcome()
                        == UnderwritingOutcomeClient.APPROVED
                        || result.outcome()
                        == UnderwritingOutcomeClient
                        .APPROVED_WITH_CONDITIONS
                        || result.outcome()
                        == UnderwritingOutcomeClient.DECLINED;

        if (!finalOutcome) {
            throw new BusinessException(
                    "Only APPROVED, "
                            + "APPROVED_WITH_CONDITIONS or "
                            + "DECLINED results can be imported"
            );
        }

        if (Boolean.TRUE.equals(
                result.subjectivitiesOutstanding()
        )) {
            throw new BusinessException(
                    "The underwriting result cannot be imported "
                            + "while bind-blocking subjectivities "
                            + "remain outstanding"
            );
        }
    }

    private void applyResultToQuote(
            Quote quote,
            UnderwritingOutcomeClient outcome
    ) {
        switch (outcome) {

            case APPROVED,
                 APPROVED_WITH_CONDITIONS -> {
                /*
                 * Keep the quote in IN_REVIEW.
                 *
                 * The underwriter must still apply final
                 * per-coverage terms before the quote becomes QUOTED.
                 */
                quote.setStatus(
                        QuoteStatus.IN_REVIEW
                );

                quote.setDeclineReason(null);
            }

            case DECLINED -> {
                quote.setStatus(
                        QuoteStatus.DECLINED_BY_INSURER
                );

                /*
                 * The caller sets the authoritative rationale
                 * separately after this switch.
                 */
            }

            case MORE_INFORMATION_REQUIRED,
                 REFERRED -> throw new BusinessException(
                    "The underwriting result is not final"
            );
        }
    }

    private UnderwritingDecisionType mapOutcome(
            UnderwritingOutcomeClient outcome
    ) {
        return switch (outcome) {

            case APPROVED ->
                    UnderwritingDecisionType.APPROVED;

            case APPROVED_WITH_CONDITIONS ->
                    UnderwritingDecisionType
                            .APPROVED_WITH_MODIFIED_TERMS;

            case MORE_INFORMATION_REQUIRED ->
                    UnderwritingDecisionType
                            .MORE_INFORMATION_REQUIRED;

            case REFERRED ->
                    UnderwritingDecisionType.REFERRED;

            case DECLINED ->
                    UnderwritingDecisionType.DECLINED;
        };
    }

    private Mono<Quote> getQuote(
            UUID quoteId
    ) {
        return quoteRepository
                .findById(quoteId)
                .switchIfEmpty(
                        Mono.error(
                                new ResourceNotFoundException(
                                        "Quote not found with ID: "
                                                + quoteId
                                )
                        )
                );
    }

    private ImportedUnderwritingResultResponse toResponse(
            Quote quote,
            UnderwritingResultClientResponse result,
            UnderwritingDecisionType localDecision,
            boolean alreadyImported
    ) {
        return new ImportedUnderwritingResultResponse(
                quote.getQuoteId(),
                quote.getQuoteNumber(),
                quote.getStatus(),
                result.assessmentId(),
                result.decisionId(),
                localDecision,
                result.riskScore(),
                result.riskBand() == null
                        ? null
                        : result.riskBand().name(),
                result.approvedLimit(),
                result.approvedDeductible(),
                result.indicatedPremium(),
                result.subjectivitiesOutstanding(),
                result.ruleVersionReference(),
                alreadyImported
        );
    }
}