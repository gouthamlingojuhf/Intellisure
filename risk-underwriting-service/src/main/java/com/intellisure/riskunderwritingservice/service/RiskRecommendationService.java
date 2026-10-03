package com.intellisure.riskunderwritingservice.service;

import com.intellisure.riskunderwritingservice.dto.request.CreateRiskRecommendationRequest;
import com.intellisure.riskunderwritingservice.dto.request.UpdateRecommendationStatusRequest;
import com.intellisure.riskunderwritingservice.dto.response.RiskRecommendationResponse;
import com.intellisure.riskunderwritingservice.entity.RiskAssessment;
import com.intellisure.riskunderwritingservice.entity.RiskRecommendation;
import com.intellisure.riskunderwritingservice.enums.RecommendationStatus;
import com.intellisure.riskunderwritingservice.enums.RiskAssessmentStatus;
import com.intellisure.riskunderwritingservice.exception.BusinessException;
import com.intellisure.riskunderwritingservice.exception.ResourceNotFoundException;
import com.intellisure.riskunderwritingservice.repository.RiskAssessmentRepository;
import com.intellisure.riskunderwritingservice.repository.RiskRecommendationRepository;
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
public class RiskRecommendationService {

    private final RiskAssessmentRepository
            assessmentRepository;

    private final RiskRecommendationRepository
            recommendationRepository;

    private final R2dbcEntityTemplate entityTemplate;


    @PreAuthorize(
            "hasAnyRole('RISK_ENGINEER', 'UNDERWRITER')"
    )
    @Transactional
    public Mono<RiskRecommendationResponse>
    createRecommendation(
            UUID assessmentId,
            CreateRiskRecommendationRequest request
    ) {
        return getAssessment(assessmentId)
                .flatMap(assessment -> {
                    validateAssessmentEditable(assessment);

                    LocalDateTime now =
                            LocalDateTime.now();

                    RiskRecommendation recommendation =
                            RiskRecommendation.builder()
                                    .recommendationId(
                                            UUID.randomUUID()
                                    )
                                    .assessmentId(assessmentId)
                                    .recommendationType(
                                            request
                                                    .recommendationType()
                                                    .trim()
                                                    .toUpperCase()
                                    )
                                    .description(
                                            request.description()
                                                    .trim()
                                    )
                                    .priority(
                                            request.priority()
                                    )
                                    .status(
                                            RecommendationStatus.OPEN
                                    )
                                    .requiredBeforeBind(
                                            request
                                                    .requiredBeforeBind()
                                    )
                                    .targetDate(
                                            request.targetDate()
                                    )
                                    .createdBy(
                                            request.createdBy()
                                    )
                                    .createdAt(now)
                                    .updatedAt(now)
                                    .build();

                    return entityTemplate
                            .insert(
                                    RiskRecommendation.class
                            )
                            .using(recommendation);
                })
                .map(this::toResponse);
    }



    @PreAuthorize(
            "hasAnyRole('RISK_ENGINEER', 'UNDERWRITER')"
    )
    @Transactional
    public Mono<RiskRecommendationResponse>
    updateStatus(
            UUID recommendationId,
            UpdateRecommendationStatusRequest request
    ) {
        return recommendationRepository
                .findById(recommendationId)
                .switchIfEmpty(
                        Mono.error(
                                new ResourceNotFoundException(
                                        "Risk recommendation not found "
                                                + "with ID: "
                                                + recommendationId
                                )
                        )
                )
                .flatMap(recommendation -> {
                    validateStatusTransition(
                            recommendation.getStatus(),
                            request.status()
                    );

                    LocalDateTime now =
                            LocalDateTime.now();

                    recommendation.setStatus(
                            request.status()
                    );

                    recommendation.setUpdatedAt(now);

                    if (request.status()
                            == RecommendationStatus.COMPLETED) {
                        recommendation.setCompletedAt(now);
                    }

                    if (request.status()
                            == RecommendationStatus.VERIFIED) {
                        if (request.verifiedBy() == null) {
                            return Mono.error(
                                    new BusinessException(
                                            "Verified-by user ID is "
                                                    + "required for VERIFIED "
                                                    + "recommendations"
                                    )
                            );
                        }

                        recommendation.setVerifiedBy(
                                request.verifiedBy()
                        );

                        recommendation.setVerifiedAt(now);

                        if (recommendation.getCompletedAt()
                                == null) {
                            recommendation.setCompletedAt(now);
                        }
                    }

                    if (request.status()
                            == RecommendationStatus.WAIVED) {
                        recommendation.setVerifiedBy(
                                request.verifiedBy()
                        );

                        recommendation.setVerifiedAt(now);
                    }

                    return entityTemplate.update(
                            recommendation
                    );
                })
                .map(this::toResponse);
    }


    @PreAuthorize("""
hasAnyRole(
'RISK_ENGINEER',
'UNDERWRITER',
'ADMIN',
'CLAIMS_ADJUSTER',
'CLAIMS_MANAGER'
)
""")
    public Flux<RiskRecommendationResponse>
    getRecommendations(
            UUID assessmentId
    ) {
        return getAssessment(assessmentId)
                .thenMany(
                        recommendationRepository
                                .findAllByAssessmentId(
                                        assessmentId
                                )
                                .map(this::toResponse)
                );
    }

    private void validateStatusTransition(
            RecommendationStatus current,
            RecommendationStatus target
    ) {
        if (current == target) {
            throw new BusinessException(
                    "Recommendation is already in status "
                            + target
            );
        }

        boolean allowed = switch (current) {
            case OPEN ->
                    target == RecommendationStatus.IN_PROGRESS
                            || target
                            == RecommendationStatus.WAIVED;

            case IN_PROGRESS ->
                    target == RecommendationStatus.COMPLETED
                            || target
                            == RecommendationStatus.WAIVED;

            case COMPLETED ->
                    target == RecommendationStatus.VERIFIED
                            || target
                            == RecommendationStatus.IN_PROGRESS;

            case VERIFIED, WAIVED -> false;
        };

        if (!allowed) {
            throw new BusinessException(
                    "Invalid recommendation status transition: "
                            + current
                            + " -> "
                            + target
            );
        }
    }

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

    private void validateAssessmentEditable(
            RiskAssessment assessment
    ) {
        if (assessment.getStatus()
                == RiskAssessmentStatus.COMPLETED
                || assessment.getStatus()
                == RiskAssessmentStatus.CANCELLED) {
            throw new BusinessException(
                    "Recommendations cannot be added to an "
                            + "assessment in status "
                            + assessment.getStatus()
            );
        }
    }

    private RiskRecommendationResponse toResponse(
            RiskRecommendation recommendation
    ) {
        return new RiskRecommendationResponse(
                recommendation.getRecommendationId(),
                recommendation.getAssessmentId(),
                recommendation.getRecommendationType(),
                recommendation.getDescription(),
                recommendation.getPriority(),
                recommendation.getStatus(),
                recommendation.getRequiredBeforeBind(),
                recommendation.getTargetDate(),
                recommendation.getCompletedAt(),
                recommendation.getVerifiedAt(),
                recommendation.getVerifiedBy(),
                recommendation.getCreatedBy(),
                recommendation.getCreatedAt(),
                recommendation.getUpdatedAt()
        );
    }
}