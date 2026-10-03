package com.intellisure.riskunderwritingservice.service;

import com.intellisure.riskunderwritingservice.dto.request.AssignAssessmentRequest;
import com.intellisure.riskunderwritingservice.dto.request.CompleteRiskScoreRequest;
import com.intellisure.riskunderwritingservice.dto.request.CreateRiskAssessmentRequest;
import com.intellisure.riskunderwritingservice.dto.response.RiskAssessmentResponse;
import com.intellisure.riskunderwritingservice.entity.RiskAssessment;
import com.intellisure.riskunderwritingservice.enums.RiskAssessmentStatus;
import com.intellisure.riskunderwritingservice.enums.RiskBand;
import com.intellisure.riskunderwritingservice.exception.AccessDeniedBusinessException;
import com.intellisure.riskunderwritingservice.exception.BusinessException;
import com.intellisure.riskunderwritingservice.exception.ResourceNotFoundException;
import com.intellisure.riskunderwritingservice.mapper.RiskAssessmentMapper;
import com.intellisure.riskunderwritingservice.repository.RiskAssessmentRepository;
import com.intellisure.riskunderwritingservice.security.SecurityActorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RiskAssessmentService {

    private final RiskAssessmentRepository
            assessmentRepository;

    private final R2dbcEntityTemplate entityTemplate;

    private final RiskAssessmentMapper mapper;

    private final SecurityActorService  securityActorService;



    @PreAuthorize(
            "hasAnyRole('UNDERWRITER', 'ADMIN', 'SYSTEM')"
    )
    @Transactional
    public Mono<RiskAssessmentResponse> createAssessment(
            CreateRiskAssessmentRequest request
    ) {
        return assessmentRepository
                .existsByQuoteId(request.quoteId())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(
                                new BusinessException(
                                        "A risk assessment already "
                                                + "exists for quote: "
                                                + request.quoteId()
                                )
                        );
                    }

                    return insertAssessment(request);
                });
    }

    private Mono<Void> verifyAssignedActor(
            RiskAssessment assessment
    ) {
        return securityActorService
                .currentUserId()
                .flatMap(currentUserId -> {
                    boolean assignedToAssessment =
                            currentUserId.equals(
                                    assessment.getAssignedUnderwriterId()
                            )
                                    || currentUserId.equals(
                                    assessment.getAssignedRiskEngineerId()
                            );

                    if (!assignedToAssessment) {
                        return Mono.error(
                                new AccessDeniedBusinessException(
                                        "The authenticated user is not assigned to this assessment"
                                )
                        );
                    }

                    return Mono.empty();
                });
    }

    private Mono<Void> verifyAssignedRiskEngineer(RiskAssessment assessment) {
        return securityActorService
                .currentUserId()
                .flatMap(currentUserId -> {
                    if (!currentUserId.equals(
                            assessment
                                    .getAssignedRiskEngineerId()
                    )) {
                        return Mono.error(
                                new AccessDeniedBusinessException(
                                        "The authenticated risk engineer "
                                                + "is not assigned to "
                                                + "this assessment"
                                )
                        );
                    }

                    return Mono.empty();
                });
    }

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
                                        "The authenticated underwriter "
                                                + "is not assigned to "
                                                + "this assessment"
                                )
                        );
                    }

                    return Mono.empty();
                });
    }




    private Mono<RiskAssessmentResponse> insertAssessment(
            CreateRiskAssessmentRequest request
    ) {
        LocalDateTime now = LocalDateTime.now();

        RiskAssessment assessment =
                RiskAssessment.builder()
                        .assessmentId(UUID.randomUUID())
                        .assessmentNumber(
                                generateAssessmentNumber()
                        )
                        .quoteId(request.quoteId())
                        .policyId(request.policyId())
                        .customerId(request.customerId())
                        .assessmentType(
                                request.assessmentType()
                                        .trim()
                                        .toUpperCase()
                        )
                        .status(
                                RiskAssessmentStatus.DRAFT
                        )
                        .assessmentDate(
                                request.assessmentDate()
                        )
                        .location(
                                request.location().trim()
                        )
                        .businessOperations(
                                request.businessOperations()
                                        .trim()
                        )
                        .annualRevenue(
                                request.annualRevenue()
                        )
                        .annualPayroll(
                                request.annualPayroll()
                        )
                        .employeeCount(
                                request.employeeCount()
                        )
                        .assetValue(
                                request.assetValue()
                        )
                        .priorClaimCount(
                                request.priorClaimCount() == null
                                        ? 0
                                        : request.priorClaimCount()
                        )
                        .priorLossAmount(
                                request.priorLossAmount() == null
                                        ? BigDecimal.ZERO
                                        : request.priorLossAmount()
                        )
                        .createdBy(request.createdBy())
                        .createdAt(now)
                        .updatedAt(now)
                        .build();

        return entityTemplate
                .insert(RiskAssessment.class)
                .using(assessment)
                .map(mapper::toResponse);
    }



    @PreAuthorize(
            "hasAnyRole('UNDERWRITER', 'RISK_ENGINEER')"
    )
    @Transactional
    public Mono<RiskAssessmentResponse> startAssessment(
            UUID assessmentId
    ) {
        return getAssessmentEntity(assessmentId)
                .flatMap(assessment ->
                        verifyAssignedActor(assessment)
                                .thenReturn(assessment)
                )
                .flatMap(assessment -> {
                    validateStatus(
                            assessment,
                            RiskAssessmentStatus.DRAFT,
                            "Only a DRAFT assessment can be started"
                    );

                    assessment.setStatus(
                            RiskAssessmentStatus.IN_PROGRESS
                    );

                    assessment.setUpdatedAt(
                            LocalDateTime.now()
                    );

                    return entityTemplate.update(assessment);
                })
                .map(mapper::toResponse);
    }

    @PreAuthorize(
            "hasAnyRole('ADMIN', 'SYSTEM')"
    )
    @Transactional
    public Mono<RiskAssessmentResponse> assignAssessment(
            UUID assessmentId,
            AssignAssessmentRequest request
    ) {
        return getAssessmentEntity(assessmentId)
                .flatMap(assessment -> {
                    if (assessment.getStatus()
                            == RiskAssessmentStatus.COMPLETED
                            || assessment.getStatus()
                            == RiskAssessmentStatus.CANCELLED) {
                        return Mono.error(
                                new BusinessException(
                                        "A completed or cancelled "
                                                + "assessment cannot be assigned"
                                )
                        );
                    }

                    assessment.setAssignedUnderwriterId(
                            request.underwriterId()
                    );

                    assessment.setAssignedRiskEngineerId(
                            request.riskEngineerId()
                    );

                    assessment.setUpdatedAt(
                            LocalDateTime.now()
                    );

                    return entityTemplate.update(assessment);
                })
                .map(mapper::toResponse);
    }



    @PreAuthorize(
            "hasAnyRole('UNDERWRITER', 'RISK_ENGINEER')"
    )
    @Transactional
    public Mono<RiskAssessmentResponse> submitForReview(
            UUID assessmentId
    ) {
        return getAssessmentEntity(assessmentId)
                .flatMap(assessment ->
                        verifyAssignedActor(assessment)
                                .thenReturn(assessment)
                )
                .flatMap(assessment -> {
                    validateStatus(
                            assessment,
                            RiskAssessmentStatus.IN_PROGRESS,
                            "Only an IN_PROGRESS assessment "
                                    + "can be submitted for review"
                    );

                    if (assessment.getAssignedUnderwriterId()
                            == null) {
                        return Mono.error(
                                new BusinessException(
                                        "An underwriter must be assigned "
                                                + "before review"
                                )
                        );
                    }

                    LocalDateTime now = LocalDateTime.now();

                    assessment.setStatus(
                            RiskAssessmentStatus.UNDER_REVIEW
                    );

                    assessment.setSubmittedAt(now);
                    assessment.setUpdatedAt(now);

                    return entityTemplate.update(assessment);
                })
                .map(mapper::toResponse);
    }

    @PreAuthorize(
            "hasAnyRole('UNDERWRITER', 'RISK_ENGINEER')"
    )
    @Transactional
    public Mono<RiskAssessmentResponse> completeRiskScore(
            UUID assessmentId,
            CompleteRiskScoreRequest request
    ) {
        return getAssessmentEntity(assessmentId)
                .flatMap(assessment ->
                        verifyAssignedUnderwriter(assessment)
                                .thenReturn(assessment)
                )
                .flatMap(assessment -> {
                    validateStatus(
                            assessment,
                            RiskAssessmentStatus.UNDER_REVIEW,
                            "Risk scoring can only be completed "
                                    + "while the assessment is UNDER_REVIEW"
                    );

                    assessment.setRiskScore(
                            request.riskScore()
                    );

                    assessment.setRiskBand(
                            determineRiskBand(
                                    request.riskScore()
                            )
                    );

                    assessment.setSummary(
                            request.summary().trim()
                    );

                    assessment.setUpdatedAt(
                            LocalDateTime.now()
                    );

                    return entityTemplate.update(assessment);
                })
                .map(mapper::toResponse);
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
    public Mono<RiskAssessmentResponse> getById(
            UUID assessmentId
    ) {
        return getAssessmentEntity(assessmentId)
                .map(mapper::toResponse);
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
    public Mono<RiskAssessmentResponse> getByNumber(
            String assessmentNumber
    ) {
        return assessmentRepository
                .findByAssessmentNumber(
                        assessmentNumber
                )
                .switchIfEmpty(
                        Mono.error(
                                new ResourceNotFoundException(
                                        "Risk assessment not found: "
                                                + assessmentNumber
                                )
                        )
                )
                .map(mapper::toResponse);
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
    public Mono<RiskAssessmentResponse> getByQuoteId(
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
                .map(mapper::toResponse);
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
    public Flux<RiskAssessmentResponse> getByStatus(
            RiskAssessmentStatus status
    ) {
        return assessmentRepository
                .findAllByStatus(status)
                .map(mapper::toResponse);
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
    public Flux<RiskAssessmentResponse>
    getAssignedUnderwriterQueue(
            UUID underwriterId
    ) {
        return assessmentRepository
                .findAllByAssignedUnderwriterId(
                        underwriterId
                )
                .map(mapper::toResponse);
    }

    private Mono<RiskAssessment> getAssessmentEntity(
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

    private void validateStatus(
            RiskAssessment assessment,
            RiskAssessmentStatus requiredStatus,
            String message
    ) {
        if (assessment.getStatus()
                != requiredStatus) {
            throw new BusinessException(
                    message
                            + ". Current status: "
                            + assessment.getStatus()
            );
        }
    }

    private RiskBand determineRiskBand(
            BigDecimal score
    ) {
        if (score.compareTo(
                new BigDecimal("20")
        ) <= 0) {
            return RiskBand.LOW;
        }

        if (score.compareTo(
                new BigDecimal("40")
        ) <= 0) {
            return RiskBand.MODERATE;
        }

        if (score.compareTo(
                new BigDecimal("60")
        ) <= 0) {
            return RiskBand.HIGH;
        }

        if (score.compareTo(
                new BigDecimal("80")
        ) <= 0) {
            return RiskBand.VERY_HIGH;
        }

        return RiskBand.EXTREME;
    }

    private String generateAssessmentNumber() {
        return "RA-"
                + Year.now().getValue()
                + "-"
                + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }
}