package com.intellisure.riskunderwritingservice.service;

import com.intellisure.riskunderwritingservice.dto.request.AssignReferralRequest;
import com.intellisure.riskunderwritingservice.dto.request.CreateReferralRequest;
import com.intellisure.riskunderwritingservice.dto.request.CreateSubjectivityRequest;
import com.intellisure.riskunderwritingservice.dto.request.ResolveReferralRequest;
import com.intellisure.riskunderwritingservice.dto.request.SubmitSubjectivityRequest;
import com.intellisure.riskunderwritingservice.dto.request.VerifySubjectivityRequest;
import com.intellisure.riskunderwritingservice.dto.request.WaiveSubjectivityRequest;
import com.intellisure.riskunderwritingservice.dto.response.SubjectivityResponse;
import com.intellisure.riskunderwritingservice.dto.response.UnderwritingReferralResponse;
import com.intellisure.riskunderwritingservice.entity.RiskAssessment;
import com.intellisure.riskunderwritingservice.entity.Subjectivity;
import com.intellisure.riskunderwritingservice.entity.UnderwritingReferral;
import com.intellisure.riskunderwritingservice.enums.ReferralStatus;
import com.intellisure.riskunderwritingservice.enums.RiskAssessmentStatus;
import com.intellisure.riskunderwritingservice.enums.SubjectivityStatus;
import com.intellisure.riskunderwritingservice.exception.BusinessException;
import com.intellisure.riskunderwritingservice.exception.ResourceNotFoundException;
import com.intellisure.riskunderwritingservice.mapper.RiskEvidenceJsonMapper;
import com.intellisure.riskunderwritingservice.repository.RiskAssessmentRepository;
import com.intellisure.riskunderwritingservice.repository.SubjectivityRepository;
import com.intellisure.riskunderwritingservice.repository.UnderwritingReferralRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UnderwritingWorkflowService {

    private final RiskAssessmentRepository assessmentRepository;

    private final UnderwritingReferralRepository referralRepository;

    private final SubjectivityRepository subjectivityRepository;

    private final R2dbcEntityTemplate entityTemplate;

    private final RiskEvidenceJsonMapper evidenceJsonMapper;

    /*
     * =========================================================
     * REFERRAL OPERATIONS
     * =========================================================
     */
    @PreAuthorize("hasRole('UNDERWRITER')")
    @Transactional
    public Mono<UnderwritingReferralResponse> createReferral(
            UUID assessmentId,
            CreateReferralRequest request
    ) {
        return getAssessment(assessmentId)
                .flatMap(assessment -> {
                    validateAssessmentForReferral(assessment);

                    LocalDateTime now = LocalDateTime.now();

                    UnderwritingReferral referral =
                            UnderwritingReferral.builder()
                                    .referralId(UUID.randomUUID())
                                    .assessmentId(assessmentId)
                                    .quoteId(assessment.getQuoteId())
                                    .referralReason(
                                            request.referralReason().trim()
                                    )
                                    .requiredAuthorityLevel(
                                            request.requiredAuthorityLevel()
                                                    .trim()
                                                    .toUpperCase()
                                    )
                                    .referredBy(request.referredBy())
                                    .referredTo(null)
                                    .status(ReferralStatus.OPEN)
                                    .resolutionNote(null)
                                    .createdAt(now)
                                    .resolvedAt(null)
                                    .updatedAt(now)
                                    .build();

                    return entityTemplate
                            .insert(UnderwritingReferral.class)
                            .using(referral)
                            .flatMap(insertedReferral -> {
                                assessment.setStatus(
                                        RiskAssessmentStatus.REFERRED
                                );

                                assessment.setUpdatedAt(now);

                                return entityTemplate
                                        .update(assessment)
                                        .thenReturn(
                                                toReferralResponse(
                                                        insertedReferral
                                                )
                                        );
                            });
                });
    }

    @PreAuthorize(
            "hasAnyRole('UNDERWRITER', 'ADMIN')"
    )
    @Transactional
    public Mono<UnderwritingReferralResponse> assignReferral(
            UUID referralId,
            AssignReferralRequest request
    ) {
        return getReferral(referralId)
                .flatMap(referral -> {
                    if (referral.getStatus() != ReferralStatus.OPEN) {
                        return Mono.error(new BusinessException(
                                "Only an OPEN referral can be assigned. Current status: " + referral.getStatus()
                        ));
                    }

                    referral.setReferredTo(request.referredTo());
                    referral.setStatus(ReferralStatus.UNDER_REVIEW);
                    referral.setUpdatedAt(LocalDateTime.now());

                    return referralRepository.save(referral)
                            .map(this::toReferralResponse);
                });
    }


    @PreAuthorize(
            "hasAnyRole('UNDERWRITER', 'ADMIN')"
    )
    @Transactional
    public Mono<UnderwritingReferralResponse> resolveReferral(
            UUID referralId,
            ResolveReferralRequest request
    ) {
        validateReferralResolutionStatus(request.status());

        return getReferral(referralId)
                .flatMap(referral -> {
                    boolean resolvable = referral.getStatus() == ReferralStatus.OPEN
                            || referral.getStatus() == ReferralStatus.UNDER_REVIEW;

                    if (!resolvable) {
                        return Mono.error(new BusinessException(
                                "Referral cannot be resolved from status " + referral.getStatus()
                        ));
                    }

                    if (referral.getReferredTo() == null) {
                        return Mono.error(new BusinessException(
                                "Referral must be assigned before it is resolved"
                        ));
                    }

                    LocalDateTime now = LocalDateTime.now();

                    referral.setStatus(request.status());
                    referral.setResolutionNote(request.resolutionNote() != null ? request.resolutionNote().trim() : null);
                    referral.setResolvedAt(now);
                    referral.setUpdatedAt(now);

                    return referralRepository.save(referral)
                            .flatMap(updatedReferral -> updateAssessmentAfterReferral(updatedReferral)
                                    .thenReturn(toReferralResponse(updatedReferral)));
                });
    }

    public Flux<UnderwritingReferralResponse> getReferrals(
            UUID assessmentId
    ) {
        return getAssessment(assessmentId)
                .thenMany(
                        referralRepository.findAllByAssessmentId(assessmentId)
                                .sort((first, second) -> second.getCreatedAt().compareTo(first.getCreatedAt()))
                                .map(this::toReferralResponse)
                );
    }

        /*
         * =========================================================
         * SUBJECTIVITY OPERATIONS
         * =========================================================
         */

    @PreAuthorize("hasRole('UNDERWRITER')")
    @Transactional
    public Mono<SubjectivityResponse> createSubjectivity(
            UUID assessmentId,
            CreateSubjectivityRequest request
    ) {
        return getAssessment(assessmentId)
                .flatMap(assessment -> {
                    validateAssessmentForSubjectivity(assessment);

                    LocalDateTime now = LocalDateTime.now();

                    Subjectivity subjectivity = Subjectivity.builder()
                            .subjectivityId(UUID.randomUUID())
                            .assessmentId(assessmentId)
                            .quoteId(assessment.getQuoteId())
                            .subjectivityType(request.subjectivityType().trim().toUpperCase())
                            .description(request.description().trim())
                            .requiredBeforeBind(request.requiredBeforeBind())
                            .status(SubjectivityStatus.OUTSTANDING)
                            .dueDate(request.dueDate())
                            .evidenceDocumentIds(null)
                            .submittedAt(null)
                            .satisfiedAt(null)
                            .verifiedBy(null)
                            .verificationNote(null)
                            .createdBy(request.createdBy())
                            .createdAt(now)
                            .updatedAt(now)
                            .build();

                    return subjectivityRepository.save(subjectivity)
                            .map(this::toSubjectivityResponse);
                });
    }


    @PreAuthorize(
            "hasAnyRole('UNDERWRITER', 'RISK_ENGINEER')"
    )
    @Transactional
    public Mono<SubjectivityResponse> submitSubjectivityEvidence(
                UUID subjectivityId,
                SubmitSubjectivityRequest request
    ) {
            return getSubjectivity(subjectivityId)
                    .flatMap(subjectivity -> {
                        boolean submittable =
                                subjectivity.getStatus()
                                        == SubjectivityStatus.OUTSTANDING
                                        || subjectivity.getStatus()
                                        == SubjectivityStatus.REJECTED;

                        if (!submittable) {
                            return Mono.error(
                                    new BusinessException(
                                            "Evidence cannot be submitted "
                                                    + "while subjectivity is "
                                                    + subjectivity.getStatus()
                                    )
                            );
                        }

                        LocalDateTime now =
                                LocalDateTime.now();

                        subjectivity.setEvidenceDocumentIds(
                                evidenceJsonMapper.toJson(
                                        request.evidenceDocumentIds()
                                )
                        );

                        subjectivity.setStatus(
                                SubjectivityStatus.SUBMITTED
                        );

                        subjectivity.setSubmittedAt(now);

                        /*
                         * Clear old verification fields when corrected
                         * evidence is resubmitted after rejection.
                         */
                        subjectivity.setSatisfiedAt(null);
                        subjectivity.setVerifiedBy(null);
                        subjectivity.setVerificationNote(null);
                        subjectivity.setUpdatedAt(now);

                        return subjectivityRepository.save(subjectivity)
                                .map(this::toSubjectivityResponse);
                });
        }

    @PreAuthorize(
            "hasAnyRole('UNDERWRITER', 'RISK_ENGINEER')"
    )
        @Transactional
        public Mono<SubjectivityResponse> verifySubjectivity(
                UUID subjectivityId,
                VerifySubjectivityRequest request
    ) {
            validateSubjectivityVerificationStatus(
                    request.status()
            );

            return getSubjectivity(subjectivityId)
                    .flatMap(subjectivity -> {
                        if (subjectivity.getStatus()
                                != SubjectivityStatus.SUBMITTED) {
                            return Mono.error(
                                    new BusinessException(
                                            "Only a SUBMITTED subjectivity "
                                                    + "can be verified. "
                                                    + "Current status: "
                                                    + subjectivity.getStatus()
                                    )
                            );
                        }

                        LocalDateTime now =
                                LocalDateTime.now();

                        subjectivity.setStatus(
                                request.status()
                        );

                        subjectivity.setVerifiedBy(
                                request.verifiedBy()
                        );

                        subjectivity.setVerificationNote(
                                request.verificationNote()
                                        .trim()
                        );

                        if (request.status()
                                == SubjectivityStatus.SATISFIED) {
                            subjectivity.setSatisfiedAt(now);
                        } else {
                            subjectivity.setSatisfiedAt(null);
                        }

                        subjectivity.setUpdatedAt(now);

                        return subjectivityRepository.save(subjectivity)
                                .map(this::toSubjectivityResponse);
                })
                ;
        }

    @PreAuthorize(
            "hasAnyRole('UNDERWRITER', 'ADMIN')"
    )
        @Transactional
        public Mono<SubjectivityResponse> waiveSubjectivity(
                UUID subjectivityId,
                WaiveSubjectivityRequest request
    ) {
            return getSubjectivity(subjectivityId)
                    .flatMap(subjectivity -> {
                        if (subjectivity.getStatus()
                                == SubjectivityStatus.SATISFIED
                                || subjectivity.getStatus()
                                == SubjectivityStatus.WAIVED) {
                            return Mono.error(
                                    new BusinessException(
                                            "Subjectivity cannot be waived "
                                                    + "from status "
                                                    + subjectivity.getStatus()
                                    )
                            );
                        }

                        LocalDateTime now =
                                LocalDateTime.now();

                        subjectivity.setStatus(
                                SubjectivityStatus.WAIVED
                        );

                        subjectivity.setVerifiedBy(
                                request.waivedBy()
                        );

                        subjectivity.setVerificationNote(
                                request.waiverReason()
                                        .trim()
                        );

                        subjectivity.setUpdatedAt(now);

                        return subjectivityRepository.save(subjectivity)
                                .map(this::toSubjectivityResponse);
                })
                ;
        }

        public Flux<SubjectivityResponse> getSubjectivities(
                UUID assessmentId
    ) {
            return getAssessment(assessmentId)
                    .thenMany(
                            subjectivityRepository
                                    .findAllByAssessmentId(
                                            assessmentId
                                    )
                                    .sort(
                                            (first, second) ->
                                                    second.getCreatedAt()
                                                            .compareTo(
                                                                    first.getCreatedAt()
                                                            )
                                    )
                                    .map(
                                            this::toSubjectivityResponse
                                    )
                    );
        }

        public Flux<SubjectivityResponse>
        getOutstandingBindSubjectivities(
                UUID assessmentId
        ) {
            return getAssessment(assessmentId)
                    .thenMany(
                            subjectivityRepository
                                    .findAllByAssessmentIdAndRequiredBeforeBindTrue(
                                            assessmentId
                                    )
                                    .filter(subjectivity ->
                                            subjectivity.getStatus()
                                                    != SubjectivityStatus.SATISFIED
                                                    && subjectivity.getStatus()
                                                    != SubjectivityStatus.WAIVED
                                    )
                                    .map(
                                            this::toSubjectivityResponse
                                    )
                    );
        }

        /*
         * =========================================================
         * PRIVATE LOOKUP HELPERS
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

        private Mono<UnderwritingReferral> getReferral(
                UUID referralId
    ) {
            return referralRepository
                    .findById(referralId)
                    .switchIfEmpty(
                            Mono.error(
                                    new ResourceNotFoundException(
                                            "Underwriting referral not found "
                                                    + "with ID: "
                                                    + referralId
                                    )
                            )
                    );
        }

        private Mono<Subjectivity> getSubjectivity(
                UUID subjectivityId
    ) {
            return subjectivityRepository
                    .findById(subjectivityId)
                    .switchIfEmpty(
                            Mono.error(
                                    new ResourceNotFoundException(
                                            "Subjectivity not found "
                                                    + "with ID: "
                                                    + subjectivityId
                                    )
                            )
                    );
        }

        /*
         * =========================================================
         * PRIVATE VALIDATION HELPERS
         * =========================================================
         */

        private void validateAssessmentForReferral(
                RiskAssessment assessment
    ) {
            if (assessment.getStatus()
                    != RiskAssessmentStatus.UNDER_REVIEW) {
                throw new BusinessException(
                        "A referral can only be created while "
                                + "the assessment is UNDER_REVIEW. "
                                + "Current status: "
                                + assessment.getStatus()
                );
            }
        }

        private void validateAssessmentForSubjectivity(
                RiskAssessment assessment
    ) {
            boolean allowed =
                    assessment.getStatus()
                            == RiskAssessmentStatus.UNDER_REVIEW
                            || assessment.getStatus()
                            == RiskAssessmentStatus.REFERRED
                            || assessment.getStatus()
                            == RiskAssessmentStatus
                            .NEEDS_INFORMATION;

            if (!allowed) {
                throw new BusinessException(
                        "Subjectivities cannot be created while "
                                + "the assessment is in status "
                                + assessment.getStatus()
                );
            }
        }

        private void validateReferralResolutionStatus(
                ReferralStatus status
    ) {
            boolean valid =
                    status == ReferralStatus.APPROVED
                            || status
                            == ReferralStatus.DECLINED
                            || status
                            == ReferralStatus.RETURNED
                            || status
                            == ReferralStatus.RESOLVED;

            if (!valid) {
                throw new BusinessException(
                        "Referral resolution status must be "
                                + "APPROVED, DECLINED, RETURNED "
                                + "or RESOLVED"
                );
            }
        }

        private void validateSubjectivityVerificationStatus(
                SubjectivityStatus status
    ) {
            if (status != SubjectivityStatus.SATISFIED
                    && status != SubjectivityStatus.REJECTED) {
                throw new BusinessException(
                        "Verification status must be "
                                + "SATISFIED or REJECTED"
                );
            }
        }

        /*
         * =========================================================
         * ASSESSMENT STATUS UPDATES
         * =========================================================
         */

        private Mono<RiskAssessment> updateAssessmentAfterReferral(
                UnderwritingReferral referral
    ) {
            return getAssessment(
                    referral.getAssessmentId()
            )
                    .flatMap(assessment -> {
                        /*
                         * RETURNED means more information is needed.
                         *
                         * APPROVED, DECLINED and RESOLVED return the
                         * assessment to underwriting review. The final
                         * underwriting outcome is recorded separately.
                         */
                        if (referral.getStatus()
                                == ReferralStatus.RETURNED) {
                            assessment.setStatus(
                                    RiskAssessmentStatus
                                            .NEEDS_INFORMATION
                            );
                        } else {
                            assessment.setStatus(
                                    RiskAssessmentStatus
                                            .UNDER_REVIEW
                            );
                        }

                        assessment.setUpdatedAt(
                                LocalDateTime.now()
                        );

                        return entityTemplate.update(
                                assessment
                        );
                    });
        }

        /*
         * =========================================================
         * RESPONSE MAPPERS
         * =========================================================
         */

        private UnderwritingReferralResponse
        toReferralResponse(
                UnderwritingReferral referral
        ) {
            return new UnderwritingReferralResponse(
                    referral.getReferralId(),
                    referral.getAssessmentId(),
                    referral.getQuoteId(),
                    referral.getReferralReason(),
                    referral.getRequiredAuthorityLevel(),
                    referral.getReferredBy(),
                    referral.getReferredTo(),
                    referral.getStatus(),
                    referral.getResolutionNote(),
                    referral.getCreatedAt(),
                    referral.getResolvedAt(),
                    referral.getUpdatedAt()
            );
        }

        private SubjectivityResponse toSubjectivityResponse(
                Subjectivity subjectivity
    ) {
            return new SubjectivityResponse(
                    subjectivity.getSubjectivityId(),
                    subjectivity.getAssessmentId(),
                    subjectivity.getQuoteId(),
                    subjectivity.getSubjectivityType(),
                    subjectivity.getDescription(),
                    subjectivity.getRequiredBeforeBind(),
                    subjectivity.getStatus(),
                    subjectivity.getDueDate(),
                    evidenceJsonMapper.fromJson(
                            subjectivity
                                    .getEvidenceDocumentIds()
                    ),
                    subjectivity.getSubmittedAt(),
                    subjectivity.getSatisfiedAt(),
                    subjectivity.getVerifiedBy(),
                    subjectivity.getVerificationNote(),
                    subjectivity.getCreatedBy(),
                    subjectivity.getCreatedAt(),
                    subjectivity.getUpdatedAt()
            );
        }
    }