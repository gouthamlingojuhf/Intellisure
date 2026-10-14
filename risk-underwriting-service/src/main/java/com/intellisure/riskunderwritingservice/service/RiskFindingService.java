package com.intellisure.riskunderwritingservice.service;

import com.intellisure.riskunderwritingservice.dto.request.CreateRiskFindingRequest;
import com.intellisure.riskunderwritingservice.dto.response.RiskFindingResponse;
import com.intellisure.riskunderwritingservice.entity.RiskAssessment;
import com.intellisure.riskunderwritingservice.entity.RiskFinding;
import com.intellisure.riskunderwritingservice.enums.RiskAssessmentStatus;
import com.intellisure.riskunderwritingservice.exception.BusinessException;
import com.intellisure.riskunderwritingservice.exception.ResourceNotFoundException;
import com.intellisure.riskunderwritingservice.mapper.RiskEvidenceJsonMapper;
import com.intellisure.riskunderwritingservice.repository.RiskAssessmentRepository;
import com.intellisure.riskunderwritingservice.repository.RiskFindingRepository;
import com.intellisure.riskunderwritingservice.security.SecurityActorService;
import com.intellisure.riskunderwritingservice.exception.AccessDeniedBusinessException;
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
public class RiskFindingService {

    private final RiskAssessmentRepository
            assessmentRepository;

    private final RiskFindingRepository
            findingRepository;

    private final R2dbcEntityTemplate entityTemplate;

    private final RiskEvidenceJsonMapper
            evidenceJsonMapper;

    private final SecurityActorService
            securityActorService;

    @PreAuthorize(
            "hasAnyRole('RISK_ENGINEER', 'UNDERWRITER', 'ADMIN', 'SYSTEM_ADMINISTRATOR')"
    )
    @Transactional
    public Mono<RiskFindingResponse> createFinding(
            UUID assessmentId,
            CreateRiskFindingRequest request
    ) {
        return getAssessment(assessmentId)
                .flatMap(assessment ->
                        verifyAssignedRiskEngineer(assessment)
                                .thenReturn(assessment)
                )
                .flatMap(assessment -> {
                    validateAssessmentEditable(assessment);

                    LocalDateTime now =
                            LocalDateTime.now();

                    RiskFinding finding =
                            RiskFinding.builder()
                                    .findingId(UUID.randomUUID())
                                    .assessmentId(assessmentId)
                                    .findingType(
                                            request.findingType()
                                                    .trim()
                                                    .toUpperCase()
                                    )
                                    .description(
                                            request.description()
                                                    .trim()
                                    )
                                    .severity(request.severity())
                                    .controlStatus(
                                            request.controlStatus()
                                    )
                                    .evidenceDocumentIds(
                                            evidenceJsonMapper.toJson(
                                                    request
                                                            .evidenceDocumentIds()
                                            )
                                    )
                                    .createdBy(
                                            request.createdBy()
                                    )
                                    .createdAt(now)
                                    .updatedAt(now)
                                    .build();

                    return entityTemplate
                            .insert(RiskFinding.class)
                            .using(finding);
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
    public Flux<RiskFindingResponse> getFindings(
            UUID assessmentId
    ) {
        return getAssessment(assessmentId)
                .thenMany(
                        findingRepository
                                .findAllByAssessmentId(
                                        assessmentId
                                )
                                .map(this::toResponse)
                );
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

    private Mono<Void> verifyAssignedRiskEngineer(
            RiskAssessment assessment
    ) {
        return securityActorService
                .currentUserId()
                .flatMap(currentUserId -> {
                    // If no engineer is assigned yet, auto-assign this assessment to the current engineer
                    if (assessment.getAssignedRiskEngineerId() == null) {
                        assessment.setAssignedRiskEngineerId(currentUserId);
                        if (assessment.getStatus() == RiskAssessmentStatus.DRAFT) {
                            assessment.setStatus(RiskAssessmentStatus.RISK_ENGINEERING_REQUIRED);
                        }
                        return entityTemplate.update(assessment).then();
                    }

                    // If assigned to current user, permit
                    if (currentUserId.equals(assessment.getAssignedRiskEngineerId())) {
                        return Mono.<Void>empty();
                    }

                    // If assigned to someone else, check if caller is an underwriter or administrator
                    return securityActorService.hasRole("ADMIN")
                            .flatMap(isAdmin -> {
                                if (Boolean.TRUE.equals(isAdmin)) {
                                    return Mono.<Void>empty();
                                }
                                return securityActorService.hasRole("UNDERWRITER")
                                        .flatMap(isUnderwriter -> {
                                            if (Boolean.TRUE.equals(isUnderwriter)) {
                                                return Mono.<Void>empty();
                                            }
                                            return Mono.<Void>error(
                                                    new AccessDeniedBusinessException(
                                                            "The authenticated risk engineer "
                                                                    + "is not assigned to this assessment"
                                                    )
                                            );
                                        });
                            })
                            .onErrorResume(e -> e instanceof AccessDeniedBusinessException ? Mono.error(e) : Mono.error(
                                    new AccessDeniedBusinessException(
                                            "The authenticated risk engineer "
                                                    + "is not assigned to this assessment"
                                    )
                            ));
                });
    }

    private void validateAssessmentEditable(
            RiskAssessment assessment
    ) {
        if (assessment.getStatus()
                == RiskAssessmentStatus.COMPLETED
                || assessment.getStatus()
                == RiskAssessmentStatus.CANCELLED) {
            throw new BusinessException(
                    "Findings cannot be added to an assessment "
                            + "in status "
                            + assessment.getStatus()
            );
        }
    }

    private RiskFindingResponse toResponse(
            RiskFinding finding
    ) {
        return new RiskFindingResponse(
                finding.getFindingId(),
                finding.getAssessmentId(),
                finding.getFindingType(),
                finding.getDescription(),
                finding.getSeverity(),
                finding.getControlStatus(),
                evidenceJsonMapper.fromJson(
                        finding.getEvidenceDocumentIds()
                ),
                finding.getCreatedBy(),
                finding.getCreatedAt(),
                finding.getUpdatedAt()
        );
    }
}