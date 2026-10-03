package com.intellisure.riskunderwritingservice.service;

import com.intellisure.riskunderwritingservice.dto.request.CreateUnderwritingDecisionRequest;
import com.intellisure.riskunderwritingservice.dto.response.UnderwritingDecisionResponse;
import com.intellisure.riskunderwritingservice.dto.response.UnderwritingResultResponse;
import com.intellisure.riskunderwritingservice.entity.RiskAssessment;
import com.intellisure.riskunderwritingservice.entity.Subjectivity;
import com.intellisure.riskunderwritingservice.entity.UnderwritingDecision;
import com.intellisure.riskunderwritingservice.enums.ReferralStatus;
import com.intellisure.riskunderwritingservice.enums.RiskAssessmentStatus;
import com.intellisure.riskunderwritingservice.enums.SubjectivityStatus;
import com.intellisure.riskunderwritingservice.enums.UnderwritingOutcome;
import com.intellisure.riskunderwritingservice.exception.AccessDeniedBusinessException;
import com.intellisure.riskunderwritingservice.exception.BusinessException;
import com.intellisure.riskunderwritingservice.exception.ResourceNotFoundException;
import com.intellisure.riskunderwritingservice.repository.RiskAssessmentRepository;
import com.intellisure.riskunderwritingservice.repository.SubjectivityRepository;
import com.intellisure.riskunderwritingservice.repository.UnderwritingDecisionRepository;
import com.intellisure.riskunderwritingservice.repository.UnderwritingReferralRepository;
import com.intellisure.riskunderwritingservice.security.SecurityActorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UnderwritingDecisionService {

    private final RiskAssessmentRepository assessmentRepository;

    private final UnderwritingDecisionRepository decisionRepository;

    private final UnderwritingReferralRepository referralRepository;

    private final SubjectivityRepository subjectivityRepository;

    private final R2dbcEntityTemplate entityTemplate;

    private final SecurityActorService securityActorService;

    /*
     * =========================================================
     * RECORD DECISION
     * =========================================================
     */


    @PreAuthorize("hasRole('UNDERWRITER')")
    @Transactional
    public Mono<UnderwritingDecisionResponse> recordDecision(
            UUID assessmentId,
            CreateUnderwritingDecisionRequest request
    ) {
        return getAssessment(assessmentId)
                .flatMap(assessment ->
                        verifyAssignedUnderwriter(
                                assessment
                        )
                                .thenReturn(assessment)
                )
                .flatMap(assessment ->
                        validateAssessmentForDecision(
                                assessment,
                                request
                        )
                )
                .flatMap(validation ->
                        createAndApplyDecision(
                                validation.assessment(),
                                request,
                                validation
                                        .subjectivitiesOutstanding()
                        )
                );
    }

    private Mono<DecisionValidation> validateAssessmentForDecision(
            RiskAssessment assessment,
            CreateUnderwritingDecisionRequest request
    ) {
        if (assessment.getStatus()
                != RiskAssessmentStatus.UNDER_REVIEW) {
            return Mono.error(
                    new BusinessException(
                            "An underwriting decision can only "
                                    + "be recorded while the assessment "
                                    + "is UNDER_REVIEW. Current status: "
                                    + assessment.getStatus()
                    )
            );
        }

        if (assessment.getAssignedUnderwriterId() == null) {
            return Mono.error(
                    new BusinessException(
                            "An underwriter must be assigned "
                                    + "before a decision is recorded"
                    )
            );
        }

        if (assessment.getRiskScore() == null
                || assessment.getRiskBand() == null) {
            return Mono.error(
                    new BusinessException(
                            "Risk score and risk band must be "
                                    + "recorded before a decision"
                    )
            );
        }

        return hasUnresolvedReferrals(assessment.getAssessmentId())
                .flatMap(hasUnresolvedReferrals -> {
                    if (hasUnresolvedReferrals
                            && request.outcome()
                            != UnderwritingOutcome.REFERRED) {
                        return Mono.error(
                                new BusinessException(
                                        "The assessment has an unresolved "
                                                + "underwriting referral"
                                )
                        );
                    }

                    return getOutstandingBindSubjectivities(
                            assessment.getAssessmentId()
                    )
                            .collectList()
                            .flatMap(outstanding -> {
                                validateOutcomeRequirements(
                                        request,
                                        outstanding
                                );

                                return Mono.just(
                                        new DecisionValidation(
                                                assessment,
                                                !outstanding.isEmpty()
                                        )
                                );
                            });
                });
    }

    private Mono<UnderwritingDecisionResponse>
    createAndApplyDecision(
            RiskAssessment assessment,
            CreateUnderwritingDecisionRequest request,
            boolean subjectivitiesOutstanding
    ) {
        LocalDateTime now = LocalDateTime.now();

        UnderwritingDecision decision =
                UnderwritingDecision.builder()
                        .decisionId(UUID.randomUUID())
                        .assessmentId(
                                assessment.getAssessmentId()
                        )
                        .quoteId(assessment.getQuoteId())
                        .underwriterId(
                                assessment
                                        .getAssignedUnderwriterId()
                        )
                        .outcome(request.outcome())
                        .decisionRationale(
                                request.decisionRationale()
                                        .trim()
                        )
                        .authorityLevel(
                                request.authorityLevel()
                                        .trim()
                                        .toUpperCase()
                        )
                        .withinAuthority(
                                request.withinAuthority()
                        )
                        .approvedLimit(
                                request.approvedLimit()
                        )
                        .approvedDeductible(
                                request.approvedDeductible()
                        )
                        .indicatedPremium(
                                request.indicatedPremium()
                        )
                        .conditions(
                                normalizeNullableText(
                                        request.conditions()
                                )
                        )
                        .subjectivitiesOutstanding(
                                subjectivitiesOutstanding
                        )
                        .ruleVersionReference(
                                normalizeNullableText(
                                        request
                                                .ruleVersionReference()
                                )
                        )
                        .decidedAt(now)
                        .createdAt(now)
                        .build();

        return entityTemplate
                .insert(UnderwritingDecision.class)
                .using(decision)
                .flatMap(insertedDecision ->
                        applyDecisionToAssessment(
                                assessment,
                                request.outcome(),
                                now
                        )
                                .thenReturn(
                                        toResponse(
                                                insertedDecision
                                        )
                                )
                );
    }

    /*
     * =========================================================
     * DECISION QUERIES
     * =========================================================
     */


    @PreAuthorize("""
hasAnyRole(
'UNDERWRITER',
'RISK_ENGINEER',
'ADMIN',
'CLAIMS_ADJUSTER',
'CLAIMS_MANAGER'
)
""")
    public Flux<UnderwritingDecisionResponse>
    getDecisionHistory(
            UUID assessmentId
    ) {
        return getAssessment(assessmentId)
                .thenMany(
                        decisionRepository
                                .findAllByAssessmentIdOrderByDecidedAtDesc(
                                        assessmentId
                                )
                                .map(this::toResponse)
                );
    }

    @PreAuthorize("""
hasAnyRole(
'UNDERWRITER',
'RISK_ENGINEER',
'ADMIN',
'CLAIMS_ADJUSTER',
'CLAIMS_MANAGER'
)
""")
    public Mono<UnderwritingDecisionResponse> getLatestDecision(
            UUID assessmentId
    ) {
        return getAssessment(assessmentId)
                .then(
                        decisionRepository
                                .findFirstByAssessmentIdOrderByDecidedAtDesc(
                                        assessmentId
                                )
                )
                .switchIfEmpty(
                        Mono.error(
                                new ResourceNotFoundException(
                                        "No underwriting decision exists "
                                                + "for assessment: "
                                                + assessmentId
                                )
                        )
                )
                .map(this::toResponse);
    }


    @PreAuthorize(
            "hasAnyRole('UNDERWRITER', 'ADMIN', 'SYSTEM')"
    )
    public Mono<UnderwritingResultResponse> getResultByQuoteId(
            UUID quoteId
    ) {
        return assessmentRepository
                .findByQuoteId(quoteId)
                .switchIfEmpty(
                        Mono.error(
                                new ResourceNotFoundException(
                                        "Risk assessment not found "
                                                + "for quote: "
                                                + quoteId
                                )
                        )
                )
                .flatMap(assessment ->
                        decisionRepository
                                .findFirstByAssessmentIdOrderByDecidedAtDesc(
                                        assessment.getAssessmentId()
                                )
                                .switchIfEmpty(
                                        Mono.error(
                                                new ResourceNotFoundException(
                                                        "No underwriting "
                                                                + "decision exists "
                                                                + "for quote: "
                                                                + quoteId
                                                )
                                        )
                                )
                                .map(decision ->
                                        toResultResponse(
                                                assessment,
                                                decision
                                        )
                                )
                );
    }

    /*
     * =========================================================
     * VALIDATION
     * =========================================================
     */

    private void validateOutcomeRequirements(
            CreateUnderwritingDecisionRequest request,
            List<Subjectivity> outstandingSubjectivities
    ) {
        if (!request.withinAuthority()
                && request.outcome()
                != UnderwritingOutcome.REFERRED) {
            throw new BusinessException(
                    "A decision outside the underwriter's "
                            + "authority must be REFERRED"
            );
        }

        switch (request.outcome()) {

            case APPROVED -> {
                requireCommercialIndications(request);

                if (!outstandingSubjectivities.isEmpty()) {
                    throw new BusinessException(
                            "APPROVED is not allowed while "
                                    + "bind-blocking subjectivities "
                                    + "remain outstanding"
                    );
                }
            }

            case APPROVED_WITH_CONDITIONS -> {
                requireCommercialIndications(request);

                boolean hasConditions =
                        request.conditions() != null
                                && !request
                                .conditions()
                                .isBlank();

                if (!hasConditions
                        && outstandingSubjectivities.isEmpty()) {
                    throw new BusinessException(
                            "APPROVED_WITH_CONDITIONS requires "
                                    + "conditions or outstanding "
                                    + "subjectivities"
                    );
                }
            }

            case MORE_INFORMATION_REQUIRED -> {
                // Rationale is already mandatory through validation.
            }

            case REFERRED -> {
                // An open referral can be created separately.
            }

            case DECLINED -> {
                // Rationale and authority level are already mandatory.
            }
        }
    }

    private void requireCommercialIndications(
            CreateUnderwritingDecisionRequest request
    ) {
        if (request.approvedLimit() == null
                || request.approvedDeductible() == null
                || request.indicatedPremium() == null) {
            throw new BusinessException(
                    "Approved limit, approved deductible "
                            + "and indicated premium are required "
                            + "for an approval decision"
            );
        }
    }

    private Mono<Boolean> hasUnresolvedReferrals(
            UUID assessmentId
    ) {
        return referralRepository
                .findAllByAssessmentIdAndStatusIn(
                        assessmentId,
                        List.of(
                                ReferralStatus.OPEN,
                                ReferralStatus.UNDER_REVIEW,
                                ReferralStatus.RETURNED
                        )
                )
                .hasElements();
    }

    private Flux<Subjectivity>
    getOutstandingBindSubjectivities(
            UUID assessmentId
    ) {
        return subjectivityRepository
                .findAllByAssessmentIdAndRequiredBeforeBindTrue(
                        assessmentId
                )
                .filter(subjectivity ->
                        subjectivity.getStatus()
                                != SubjectivityStatus.SATISFIED
                                && subjectivity.getStatus()
                                != SubjectivityStatus.WAIVED
                );
    }

    /*
     * =========================================================
     * ASSESSMENT STATUS
     * =========================================================
     */




    private Mono<Void> verifyAssignedUnderwriter(
            RiskAssessment assessment
    ) {
        return securityActorService
                .currentUserId()
                .flatMap(currentUserId -> {
                    if (!currentUserId.equals(
                            assessment
                                    .getAssignedUnderwriterId()
                    )) {
                        return Mono.error(
                                new AccessDeniedBusinessException(
                                        "Only the assigned underwriter "
                                                + "can record a decision "
                                                + "for this assessment"
                                )
                        );
                    }

                    return Mono.empty();
                });
    }

    private Mono<RiskAssessment> applyDecisionToAssessment(
            RiskAssessment assessment,
            UnderwritingOutcome outcome,
            LocalDateTime now
    ) {
        switch (outcome) {

            case APPROVED,
                 APPROVED_WITH_CONDITIONS,
                 DECLINED -> {
                assessment.setStatus(
                        RiskAssessmentStatus.COMPLETED
                );

                assessment.setCompletedAt(now);
            }

            case MORE_INFORMATION_REQUIRED -> {
                assessment.setStatus(
                        RiskAssessmentStatus.NEEDS_INFORMATION
                );

                assessment.setCompletedAt(null);
            }

            case REFERRED -> {
                assessment.setStatus(
                        RiskAssessmentStatus.REFERRED
                );

                assessment.setCompletedAt(null);
            }
        }

        assessment.setUpdatedAt(now);

        return entityTemplate.update(assessment);
    }

    /*
     * =========================================================
     * LOOKUPS AND MAPPERS
     * =========================================================
     */

    private Mono<RiskAssessment> getAssessment(
            UUID assessmentId
    ) {
        return assessmentRepository
                .findById(assessmentId)
                .switchIfEmpty(
                        Mono.error(
                                new ResourceNotFoundException(
                                        "Risk assessment not found "
                                                + "with ID: "
                                                + assessmentId
                                )
                        )
                );
    }

    private UnderwritingDecisionResponse toResponse(
            UnderwritingDecision decision
    ) {
        return new UnderwritingDecisionResponse(
                decision.getDecisionId(),
                decision.getAssessmentId(),
                decision.getQuoteId(),
                decision.getUnderwriterId(),
                decision.getOutcome(),
                decision.getDecisionRationale(),
                decision.getAuthorityLevel(),
                decision.getWithinAuthority(),
                decision.getApprovedLimit(),
                decision.getApprovedDeductible(),
                decision.getIndicatedPremium(),
                decision.getConditions(),
                decision.getSubjectivitiesOutstanding(),
                decision.getRuleVersionReference(),
                decision.getDecidedAt(),
                decision.getCreatedAt()
        );
    }

    private UnderwritingResultResponse toResultResponse(
            RiskAssessment assessment,
            UnderwritingDecision decision
    ) {
        return new UnderwritingResultResponse(
                assessment.getAssessmentId(),
                assessment.getAssessmentNumber(),
                assessment.getQuoteId(),
                assessment.getStatus(),
                assessment.getRiskScore(),
                assessment.getRiskBand(),
                decision.getDecisionId(),
                decision.getOutcome(),
                decision.getDecisionRationale(),
                decision.getAuthorityLevel(),
                decision.getWithinAuthority(),
                decision.getApprovedLimit(),
                decision.getApprovedDeductible(),
                decision.getIndicatedPremium(),
                decision.getConditions(),
                decision.getSubjectivitiesOutstanding(),
                decision.getRuleVersionReference(),
                decision.getDecidedAt()
        );
    }

    private String normalizeNullableText(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private record DecisionValidation(
            RiskAssessment assessment,
            boolean subjectivitiesOutstanding
    ) {
    }
}