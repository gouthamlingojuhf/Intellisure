package com.intellisure.riskunderwritingservice.service;

import com.intellisure.riskunderwritingservice.dto.request.AssignReferralRequest;
import com.intellisure.riskunderwritingservice.dto.request.CreateReferralRequest;
import com.intellisure.riskunderwritingservice.dto.request.CreateSubjectivityRequest;
import com.intellisure.riskunderwritingservice.dto.request.ResolveReferralRequest;
import com.intellisure.riskunderwritingservice.dto.request.SubmitSubjectivityRequest;
import com.intellisure.riskunderwritingservice.dto.request.VerifySubjectivityRequest;
import com.intellisure.riskunderwritingservice.dto.request.WaiveSubjectivityRequest;
import com.intellisure.riskunderwritingservice.entity.RiskAssessment;
import com.intellisure.riskunderwritingservice.entity.Subjectivity;
import com.intellisure.riskunderwritingservice.entity.UnderwritingReferral;
import com.intellisure.riskunderwritingservice.enums.ReferralStatus;
import com.intellisure.riskunderwritingservice.enums.RiskAssessmentStatus;
import com.intellisure.riskunderwritingservice.enums.SubjectivityStatus;
import com.intellisure.riskunderwritingservice.mapper.RiskEvidenceJsonMapper;
import com.intellisure.riskunderwritingservice.repository.RiskAssessmentRepository;
import com.intellisure.riskunderwritingservice.repository.SubjectivityRepository;
import com.intellisure.riskunderwritingservice.repository.UnderwritingReferralRepository;
import com.intellisure.riskunderwritingservice.testsupport.EntityTemplateStubber;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UnderwritingWorkflowServiceTest")
class UnderwritingWorkflowServiceTest {

    @Mock
    private RiskAssessmentRepository assessmentRepository;

    @Mock
    private UnderwritingReferralRepository referralRepository;

    @Mock
    private SubjectivityRepository subjectivityRepository;

    @Mock
    private R2dbcEntityTemplate entityTemplate;

    @Mock
    private RiskEvidenceJsonMapper evidenceJsonMapper;

    @InjectMocks
    private UnderwritingWorkflowService workflowService;

    private RiskAssessment createAssessment(UUID assessmentId, RiskAssessmentStatus status) {
        return RiskAssessment.builder()
                .assessmentId(assessmentId)
                .assessmentNumber("RA-2026-0002")
                .quoteId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .assessmentType("UNDERWRITING")
                .assessmentDate(LocalDate.now())
                .status(status)
                .assignedUnderwriterId(UUID.randomUUID())
                .build();
    }

    @Test
    @DisplayName("Creating referral transitions assessment to REFERRED")
    void createReferralTransitionsAssessment() {
        UUID assessmentId = UUID.randomUUID();
        RiskAssessment assessment = createAssessment(assessmentId, RiskAssessmentStatus.UNDER_REVIEW);

        when(assessmentRepository.findById(assessmentId)).thenReturn(Mono.just(assessment));
        EntityTemplateStubber.stubInsert(entityTemplate, UnderwritingReferral.class);
        when(entityTemplate.update(any(RiskAssessment.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        CreateReferralRequest request = new CreateReferralRequest(
                "High coastal storm exposure exceeds authority",
                "SENIOR_UNDERWRITER",
                UUID.randomUUID()
        );

        StepVerifier.create(workflowService.createReferral(assessmentId, request))
                .assertNext(res -> {
                    assertEquals(assessmentId, res.assessmentId());
                    assertEquals(ReferralStatus.OPEN, res.status());
                    assertEquals("SENIOR_UNDERWRITER", res.requiredAuthorityLevel());
                })
                .verifyComplete();

        verify(entityTemplate).update(argThat((RiskAssessment a) -> a.getStatus() == RiskAssessmentStatus.REFERRED));
    }

    @Test
    @DisplayName("Assign and resolve referral updates status to RESOLVED and returns assessment to UNDER_REVIEW")
    void assignAndResolveReferral() {
        UUID referralId = UUID.randomUUID();
        UUID assessmentId = UUID.randomUUID();
        UUID assignedManagerId = UUID.randomUUID();
        RiskAssessment assessment = createAssessment(assessmentId, RiskAssessmentStatus.REFERRED);

        UnderwritingReferral openReferral = UnderwritingReferral.builder()
                .referralId(referralId)
                .assessmentId(assessmentId)
                .quoteId(assessment.getQuoteId())
                .status(ReferralStatus.OPEN)
                .referralReason("Reason")
                .requiredAuthorityLevel("MANAGER")
                .referredBy(UUID.randomUUID())
                .build();

        // 1. Assign referral
        when(referralRepository.findById(referralId)).thenReturn(Mono.just(openReferral));
        when(referralRepository.save(any(UnderwritingReferral.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(workflowService.assignReferral(referralId, new AssignReferralRequest(assignedManagerId)))
                .assertNext(res -> assertEquals(assignedManagerId, res.referredTo()))
                .verifyComplete();

        // 2. Resolve referral
        UnderwritingReferral assignedReferral = UnderwritingReferral.builder()
                .referralId(referralId)
                .assessmentId(assessmentId)
                .quoteId(assessment.getQuoteId())
                .status(ReferralStatus.OPEN)
                .referredTo(assignedManagerId)
                .build();

        when(referralRepository.findById(referralId)).thenReturn(Mono.just(assignedReferral));
        when(assessmentRepository.findById(assessmentId)).thenReturn(Mono.just(assessment));
        when(entityTemplate.update(any(RiskAssessment.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        ResolveReferralRequest resolveRequest = new ResolveReferralRequest(
                ReferralStatus.RESOLVED,
                "Approved exception with 10% premium surcharge"
        );

        StepVerifier.create(workflowService.resolveReferral(referralId, resolveRequest))
                .assertNext(res -> {
                    assertEquals(ReferralStatus.RESOLVED, res.status());
                    assertNotNull(res.resolvedAt());
                })
                .verifyComplete();

        verify(entityTemplate).update(argThat((RiskAssessment a) -> a.getStatus() == RiskAssessmentStatus.UNDER_REVIEW));
    }

    @Test
    @DisplayName("Create, submit evidence, and verify subjectivity")
    void createAndVerifySubjectivity() {
        UUID assessmentId = UUID.randomUUID();
        UUID subjectivityId = UUID.randomUUID();
        UUID underwriterId = UUID.randomUUID();
        RiskAssessment assessment = createAssessment(assessmentId, RiskAssessmentStatus.UNDER_REVIEW);

        when(assessmentRepository.findById(assessmentId)).thenReturn(Mono.just(assessment));
        when(subjectivityRepository.save(any(Subjectivity.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        CreateSubjectivityRequest createReq = new CreateSubjectivityRequest(
                "ROOF_INSPECTION",
                "Provide updated roof inspection report",
                true,
                LocalDate.now().plusDays(14),
                underwriterId
        );

        StepVerifier.create(workflowService.createSubjectivity(assessmentId, createReq))
                .assertNext(res -> {
                    assertEquals(SubjectivityStatus.OUTSTANDING, res.status());
                    assertEquals("ROOF_INSPECTION", res.subjectivityType());
                })
                .verifyComplete();

        // Submit evidence
        Subjectivity openSubjectivity = Subjectivity.builder()
                .subjectivityId(subjectivityId)
                .assessmentId(assessmentId)
                .status(SubjectivityStatus.OUTSTANDING)
                .build();

        when(subjectivityRepository.findById(subjectivityId)).thenReturn(Mono.just(openSubjectivity));
        when(evidenceJsonMapper.toJson(any())).thenReturn("[]");

        SubmitSubjectivityRequest submitReq = new SubmitSubjectivityRequest(
                List.of(UUID.randomUUID())
        );

        StepVerifier.create(workflowService.submitSubjectivityEvidence(subjectivityId, submitReq))
                .assertNext(res -> assertEquals(SubjectivityStatus.SUBMITTED, res.status()))
                .verifyComplete();

        // Verify subjectivity
        Subjectivity submittedSubjectivity = Subjectivity.builder()
                .subjectivityId(subjectivityId)
                .assessmentId(assessmentId)
                .status(SubjectivityStatus.SUBMITTED)
                .build();

        when(subjectivityRepository.findById(subjectivityId)).thenReturn(Mono.just(submittedSubjectivity));

        VerifySubjectivityRequest verifyReq = new VerifySubjectivityRequest(
                SubjectivityStatus.SATISFIED,
                underwriterId,
                "Report acceptable"
        );

        StepVerifier.create(workflowService.verifySubjectivity(subjectivityId, verifyReq))
                .assertNext(res -> {
                    assertEquals(SubjectivityStatus.SATISFIED, res.status());
                    assertNotNull(res.satisfiedAt());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Waiving open subjectivity sets status to WAIVED")
    void waiveSubjectivity() {
        UUID subjectivityId = UUID.randomUUID();
        Subjectivity openSubjectivity = Subjectivity.builder()
                .subjectivityId(subjectivityId)
                .assessmentId(UUID.randomUUID())
                .status(SubjectivityStatus.OUTSTANDING)
                .build();

        when(subjectivityRepository.findById(subjectivityId)).thenReturn(Mono.just(openSubjectivity));
        when(subjectivityRepository.save(any(Subjectivity.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        WaiveSubjectivityRequest waiveReq = new WaiveSubjectivityRequest(
                UUID.randomUUID(),
                "Waived per underwriter review"
        );

        StepVerifier.create(workflowService.waiveSubjectivity(subjectivityId, waiveReq))
                .assertNext(res -> {
                    assertEquals(SubjectivityStatus.WAIVED, res.status());
                    assertEquals("Waived per underwriter review", res.verificationNote());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Normal low-risk quotes proceed without referral")
    void normalLowRiskProceedsWithoutReferral() {
        UUID assessmentId = UUID.randomUUID();
        when(referralRepository.findAllByAssessmentIdAndStatusIn(eq(assessmentId), any()))
                .thenReturn(Flux.empty());

        StepVerifier.create(referralRepository.findAllByAssessmentIdAndStatusIn(assessmentId, List.of(ReferralStatus.OPEN)))
                .expectNextCount(0)
                .verifyComplete();
    }
}
