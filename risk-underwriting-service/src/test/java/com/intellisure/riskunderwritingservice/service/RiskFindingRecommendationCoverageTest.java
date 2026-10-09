package com.intellisure.riskunderwritingservice.service;

import com.intellisure.riskunderwritingservice.dto.request.*;
import com.intellisure.riskunderwritingservice.entity.*;
import com.intellisure.riskunderwritingservice.enums.*;
import com.intellisure.riskunderwritingservice.exception.BusinessException;
import com.intellisure.riskunderwritingservice.mapper.RiskEvidenceJsonMapper;
import com.intellisure.riskunderwritingservice.repository.*;
import com.intellisure.riskunderwritingservice.security.SecurityActorService;
import com.intellisure.riskunderwritingservice.testsupport.EntityTemplateStubber;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RiskFindingRecommendationCoverageTest {
    @Mock RiskAssessmentRepository assessmentRepository;
    @Mock RiskFindingRepository findingRepository;
    @Mock RiskRecommendationRepository recommendationRepository;
    @Mock R2dbcEntityTemplate entityTemplate;
    @Mock RiskEvidenceJsonMapper evidenceMapper;
    @Mock SecurityActorService security;

    private final UUID assessmentId = UUID.randomUUID();
    private final UUID actor = UUID.randomUUID();

    private RiskAssessment assessment(RiskAssessmentStatus status) {
        return RiskAssessment.builder().assessmentId(assessmentId).quoteId(UUID.randomUUID()).status(status)
                .assignedRiskEngineerId(actor).build();
    }

    @Test
    void findingServiceCoversCreateReadAuthorizationAndEditability() {
        RiskFindingService service = new RiskFindingService(assessmentRepository, findingRepository, entityTemplate, evidenceMapper, security);
        RiskAssessment assessment = assessment(RiskAssessmentStatus.IN_PROGRESS);
        when(assessmentRepository.findById(assessmentId)).thenReturn(Mono.just(assessment));
        when(security.currentUserId()).thenReturn(Mono.just(actor));
        when(evidenceMapper.toJson(any())).thenReturn("[]");
        when(evidenceMapper.fromJson(any())).thenReturn(List.of());
        EntityTemplateStubber.stubInsert(entityTemplate, RiskFinding.class);
        CreateRiskFindingRequest request = new CreateRiskFindingRequest(" hazard ", " description ", FindingSeverity.HIGH, ControlStatus.ADEQUATE, List.of(UUID.randomUUID()), actor);
        StepVerifier.create(service.createFinding(assessmentId, request)).assertNext(r -> assertEquals("HAZARD", r.findingType())).verifyComplete();
        RiskFinding finding = RiskFinding.builder().findingId(UUID.randomUUID()).assessmentId(assessmentId).findingType("HAZARD").evidenceDocumentIds("[]").build();
        when(findingRepository.findAllByAssessmentId(assessmentId)).thenReturn(Flux.just(finding));
        StepVerifier.create(service.getFindings(assessmentId)).expectNextCount(1).verifyComplete();
        when(security.currentUserId()).thenReturn(Mono.just(UUID.randomUUID()));
        StepVerifier.create(service.createFinding(assessmentId, request)).expectError().verify();
        assessment.setStatus(RiskAssessmentStatus.COMPLETED);
        when(security.currentUserId()).thenReturn(Mono.just(actor));
        StepVerifier.create(service.createFinding(assessmentId, request)).expectError(BusinessException.class).verify();
        assessment.setStatus(RiskAssessmentStatus.CANCELLED);
        StepVerifier.create(service.createFinding(assessmentId, request)).expectError(BusinessException.class).verify();
        when(assessmentRepository.findById(assessmentId)).thenReturn(Mono.empty());
        StepVerifier.create(service.getFindings(assessmentId)).expectError().verify();
    }

    @Test
    void recommendationServiceCoversLifecycleTransitionsAndGuards() {
        RiskRecommendationService service = new RiskRecommendationService(assessmentRepository, recommendationRepository, entityTemplate);
        RiskAssessment assessment = assessment(RiskAssessmentStatus.IN_PROGRESS);
        when(assessmentRepository.findById(assessmentId)).thenReturn(Mono.just(assessment));
        EntityTemplateStubber.stubInsert(entityTemplate, RiskRecommendation.class);
        when(entityTemplate.update(any(RiskRecommendation.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        CreateRiskRecommendationRequest create = new CreateRiskRecommendationRequest(" inspection ", " description ", RecommendationPriority.HIGH, true, LocalDate.now(), actor);
        StepVerifier.create(service.createRecommendation(assessmentId, create)).assertNext(r -> assertEquals(RecommendationStatus.OPEN, r.status())).verifyComplete();
        RiskRecommendation recommendation = RiskRecommendation.builder().recommendationId(UUID.randomUUID()).assessmentId(assessmentId).status(RecommendationStatus.OPEN).build();
        when(recommendationRepository.findById(recommendation.getRecommendationId())).thenReturn(Mono.just(recommendation));
        UUID recommendationId = recommendation.getRecommendationId();
        StepVerifier.create(service.updateStatus(recommendationId, new UpdateRecommendationStatusRequest(RecommendationStatus.IN_PROGRESS, null))).expectNextCount(1).verifyComplete();
        StepVerifier.create(service.updateStatus(recommendationId, new UpdateRecommendationStatusRequest(RecommendationStatus.COMPLETED, null))).expectNextCount(1).verifyComplete();
        StepVerifier.create(service.updateStatus(recommendationId, new UpdateRecommendationStatusRequest(RecommendationStatus.VERIFIED, null))).expectError(BusinessException.class).verify();
        recommendation.setStatus(RecommendationStatus.COMPLETED);
        StepVerifier.create(service.updateStatus(recommendationId, new UpdateRecommendationStatusRequest(RecommendationStatus.VERIFIED, actor))).expectNextCount(1).verifyComplete();
        StepVerifier.create(service.updateStatus(recommendationId, new UpdateRecommendationStatusRequest(RecommendationStatus.WAIVED, actor))).expectError(BusinessException.class).verify();
        recommendation.setStatus(RecommendationStatus.OPEN);
        StepVerifier.create(service.updateStatus(recommendationId, new UpdateRecommendationStatusRequest(RecommendationStatus.WAIVED, actor))).expectNextCount(1).verifyComplete();
        recommendation.setStatus(RecommendationStatus.IN_PROGRESS);
        StepVerifier.create(service.updateStatus(recommendationId, new UpdateRecommendationStatusRequest(RecommendationStatus.WAIVED, actor))).expectNextCount(1).verifyComplete();
        recommendation.setStatus(RecommendationStatus.COMPLETED);
        StepVerifier.create(service.updateStatus(recommendationId, new UpdateRecommendationStatusRequest(RecommendationStatus.IN_PROGRESS, null))).expectNextCount(1).verifyComplete();
        recommendation.setStatus(RecommendationStatus.VERIFIED);
        StepVerifier.create(service.updateStatus(recommendationId, new UpdateRecommendationStatusRequest(RecommendationStatus.IN_PROGRESS, null))).expectError(BusinessException.class).verify();
        recommendation.setStatus(RecommendationStatus.WAIVED);
        StepVerifier.create(service.updateStatus(recommendationId, new UpdateRecommendationStatusRequest(RecommendationStatus.OPEN, null))).expectError(BusinessException.class).verify();
        recommendation.setStatus(RecommendationStatus.OPEN);
        StepVerifier.create(service.updateStatus(recommendationId, new UpdateRecommendationStatusRequest(RecommendationStatus.OPEN, null))).expectError(BusinessException.class).verify();
        when(recommendationRepository.findAllByAssessmentId(assessmentId)).thenReturn(Flux.just(recommendation));
        when(assessmentRepository.findById(assessmentId)).thenReturn(Mono.just(assessment));
        StepVerifier.create(service.getRecommendations(assessmentId)).expectNextCount(1).verifyComplete();
        assessment.setStatus(RiskAssessmentStatus.COMPLETED);
        StepVerifier.create(service.createRecommendation(assessmentId, create)).expectError(BusinessException.class).verify();
        assessment.setStatus(RiskAssessmentStatus.CANCELLED);
        StepVerifier.create(service.createRecommendation(assessmentId, create)).expectError(BusinessException.class).verify();
        when(recommendationRepository.findById(recommendationId)).thenReturn(Mono.empty());
        StepVerifier.create(service.updateStatus(recommendationId, new UpdateRecommendationStatusRequest(RecommendationStatus.OPEN, null))).expectError().verify();
    }
}
