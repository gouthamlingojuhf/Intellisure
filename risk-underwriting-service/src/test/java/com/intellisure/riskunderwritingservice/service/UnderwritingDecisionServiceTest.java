package com.intellisure.riskunderwritingservice.service;

import com.intellisure.riskunderwritingservice.dto.request.CreateUnderwritingDecisionRequest;
import com.intellisure.riskunderwritingservice.entity.RiskAssessment;
import com.intellisure.riskunderwritingservice.entity.UnderwritingDecision;
import com.intellisure.riskunderwritingservice.entity.UnderwritingReferral;
import com.intellisure.riskunderwritingservice.enums.RiskAssessmentStatus;
import com.intellisure.riskunderwritingservice.enums.RiskBand;
import com.intellisure.riskunderwritingservice.enums.SubjectivityStatus;
import com.intellisure.riskunderwritingservice.enums.UnderwritingOutcome;
import com.intellisure.riskunderwritingservice.exception.BusinessException;
import com.intellisure.riskunderwritingservice.repository.RiskAssessmentRepository;
import com.intellisure.riskunderwritingservice.repository.SubjectivityRepository;
import com.intellisure.riskunderwritingservice.repository.UnderwritingDecisionRepository;
import com.intellisure.riskunderwritingservice.repository.UnderwritingReferralRepository;
import com.intellisure.riskunderwritingservice.security.SecurityActorService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UnderwritingDecisionServiceTest")
class UnderwritingDecisionServiceTest {

    @Mock
    private RiskAssessmentRepository assessmentRepository;

    @Mock
    private UnderwritingDecisionRepository decisionRepository;

    @Mock
    private UnderwritingReferralRepository referralRepository;

    @Mock
    private SubjectivityRepository subjectivityRepository;

    @Mock
    private R2dbcEntityTemplate entityTemplate;

    @Mock
    private SecurityActorService securityActorService;

    @InjectMocks
    private UnderwritingDecisionService service;

    private RiskAssessment createAssessment(UUID assessmentId, RiskAssessmentStatus status, UUID underwriterId) {
        return RiskAssessment.builder()
                .assessmentId(assessmentId)
                .assessmentNumber("RA-2026-0001")
                .quoteId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .assessmentType("UNDERWRITING")
                .assessmentDate(LocalDate.now())
                .status(status)
                .assignedUnderwriterId(underwriterId)
                .riskScore(new BigDecimal("35.00"))
                .riskBand(RiskBand.LOW)
                .build();
    }

    @Test
    @DisplayName("Recording APPROVED decision updates assessment to COMPLETED and returns decision")
    void recordDecisionApproved() {
        UUID assessmentId = UUID.randomUUID();
        UUID underwriterId = UUID.randomUUID();
        RiskAssessment assessment = createAssessment(assessmentId, RiskAssessmentStatus.UNDER_REVIEW, underwriterId);

        when(assessmentRepository.findById(assessmentId)).thenReturn(Mono.just(assessment));
        when(securityActorService.currentUserId()).thenReturn(Mono.just(underwriterId));
        when(referralRepository.findAllByAssessmentIdAndStatusIn(eq(assessmentId), any()))
                .thenReturn(Flux.empty());
        when(subjectivityRepository.findAllByAssessmentIdAndRequiredBeforeBindTrue(assessmentId))
                .thenReturn(Flux.empty());
        EntityTemplateStubber.stubInsert(entityTemplate, UnderwritingDecision.class);
        when(entityTemplate.update(any(RiskAssessment.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        CreateUnderwritingDecisionRequest request = new CreateUnderwritingDecisionRequest(
                UnderwritingOutcome.APPROVED,
                "Acceptable risk profile",
                "AUTOMATED",
                Boolean.TRUE,
                new BigDecimal("500000.00"),
                new BigDecimal("2500.00"),
                new BigDecimal("7500.00"),
                null,
                "RULES-V1"
        );

        StepVerifier.create(service.recordDecision(assessmentId, request))
                .assertNext(response -> {
                    assertEquals(assessmentId, response.assessmentId());
                    assertEquals(UnderwritingOutcome.APPROVED, response.outcome());
                    assertNotNull(response.decisionId());
                })
                .verifyComplete();

        verify(entityTemplate).update(argThat((RiskAssessment a) -> a.getStatus() == RiskAssessmentStatus.COMPLETED));
    }

    @Test
    @DisplayName("Recording DECLINED decision updates assessment to CANCELLED")
    void recordDecisionDeclined() {
        UUID assessmentId = UUID.randomUUID();
        UUID underwriterId = UUID.randomUUID();
        RiskAssessment assessment = createAssessment(assessmentId, RiskAssessmentStatus.UNDER_REVIEW, underwriterId);

        when(assessmentRepository.findById(assessmentId)).thenReturn(Mono.just(assessment));
        when(securityActorService.currentUserId()).thenReturn(Mono.just(underwriterId));
        when(referralRepository.findAllByAssessmentIdAndStatusIn(eq(assessmentId), any()))
                .thenReturn(Flux.empty());
        when(subjectivityRepository.findAllByAssessmentIdAndRequiredBeforeBindTrue(assessmentId))
                .thenReturn(Flux.empty());
        EntityTemplateStubber.stubInsert(entityTemplate, UnderwritingDecision.class);
        when(entityTemplate.update(any(RiskAssessment.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        CreateUnderwritingDecisionRequest request = new CreateUnderwritingDecisionRequest(
                UnderwritingOutcome.DECLINED,
                "Outside risk appetite",
                "MANUAL",
                Boolean.TRUE,
                null,
                null,
                null,
                null,
                "RULES-V1"
        );

        StepVerifier.create(service.recordDecision(assessmentId, request))
                .assertNext(response -> {
                    assertEquals(UnderwritingOutcome.DECLINED, response.outcome());
                })
                .verifyComplete();

        verify(entityTemplate).update(argThat((RiskAssessment a) -> a.getStatus() == RiskAssessmentStatus.COMPLETED));
    }

    @Test
    @DisplayName("Invalid transition: recording decision on non-UNDER_REVIEW assessment is rejected")
    void recordDecisionRejectsNonUnderReview() {
        UUID assessmentId = UUID.randomUUID();
        UUID underwriterId = UUID.randomUUID();
        RiskAssessment draftAssessment = createAssessment(assessmentId, RiskAssessmentStatus.DRAFT, underwriterId);

        when(assessmentRepository.findById(assessmentId)).thenReturn(Mono.just(draftAssessment));
        when(securityActorService.currentUserId()).thenReturn(Mono.just(underwriterId));

        CreateUnderwritingDecisionRequest request = new CreateUnderwritingDecisionRequest(
                UnderwritingOutcome.APPROVED,
                "Rationale",
                "AUTOMATED",
                Boolean.TRUE,
                null, null, null, null, null
        );

        StepVerifier.create(service.recordDecision(assessmentId, request))
                .expectError(BusinessException.class)
                .verify();

        verify(entityTemplate, never()).insert(any(Class.class));
    }

    @Test
    @DisplayName("Recording APPROVED decision with unresolved open referrals is rejected")
    void recordDecisionRejectsApprovalWithOpenReferrals() {
        UUID assessmentId = UUID.randomUUID();
        UUID underwriterId = UUID.randomUUID();
        RiskAssessment assessment = createAssessment(assessmentId, RiskAssessmentStatus.UNDER_REVIEW, underwriterId);

        when(assessmentRepository.findById(assessmentId)).thenReturn(Mono.just(assessment));
        when(securityActorService.currentUserId()).thenReturn(Mono.just(underwriterId));
        when(referralRepository.findAllByAssessmentIdAndStatusIn(eq(assessmentId), any()))
                .thenReturn(Flux.just(mock(UnderwritingReferral.class)));

        CreateUnderwritingDecisionRequest request = new CreateUnderwritingDecisionRequest(
                UnderwritingOutcome.APPROVED,
                "Rationale",
                "AUTOMATED",
                Boolean.TRUE,
                null, null, null, null, null
        );

        StepVerifier.create(service.recordDecision(assessmentId, request))
                .expectError(BusinessException.class)
                .verify();
    }

    @Test
    @DisplayName("getResultByQuoteId returns authoritative underwriting result")
    void getResultByQuoteIdReturnsAuthoritativeResult() {
        UUID quoteId = UUID.randomUUID();
        UUID assessmentId = UUID.randomUUID();
        RiskAssessment assessment = RiskAssessment.builder()
                .assessmentId(assessmentId)
                .assessmentNumber("RA-2026-0001")
                .quoteId(quoteId)
                .customerId(UUID.randomUUID())
                .status(RiskAssessmentStatus.COMPLETED)
                .riskScore(new BigDecimal("35.00"))
                .riskBand(RiskBand.LOW)
                .build();

        UnderwritingDecision decision = UnderwritingDecision.builder()
                .decisionId(UUID.randomUUID())
                .assessmentId(assessmentId)
                .outcome(UnderwritingOutcome.APPROVED)
                .decisionRationale("Standard approved terms")
                .authorityLevel("AUTOMATED")
                .approvedLimit(new BigDecimal("1000000.00"))
                .approvedDeductible(new BigDecimal("1000.00"))
                .indicatedPremium(new BigDecimal("5000.00"))
                .decidedAt(LocalDateTime.now())
                .build();

        when(assessmentRepository.findByQuoteId(quoteId)).thenReturn(Mono.just(assessment));
        when(decisionRepository.findFirstByAssessmentIdOrderByDecidedAtDesc(assessmentId)).thenReturn(Mono.just(decision));

        StepVerifier.create(service.getResultByQuoteId(quoteId))
                .assertNext(result -> {
                    assertEquals(quoteId, result.quoteId());
                    assertEquals(assessmentId, result.assessmentId());
                    assertEquals(UnderwritingOutcome.APPROVED, result.outcome());
                    assertEquals("Standard approved terms", result.decisionRationale());
                })
                .verifyComplete();
    }
}
