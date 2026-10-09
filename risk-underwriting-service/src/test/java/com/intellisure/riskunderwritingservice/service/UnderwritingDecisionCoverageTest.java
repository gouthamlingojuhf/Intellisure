package com.intellisure.riskunderwritingservice.service;

import com.intellisure.riskunderwritingservice.dto.request.CreateUnderwritingDecisionRequest;
import com.intellisure.riskunderwritingservice.entity.*;
import com.intellisure.riskunderwritingservice.enums.*;
import com.intellisure.riskunderwritingservice.exception.BusinessException;
import com.intellisure.riskunderwritingservice.repository.*;
import com.intellisure.riskunderwritingservice.security.SecurityActorService;
import com.intellisure.riskunderwritingservice.testsupport.EntityTemplateStubber;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnderwritingDecisionCoverageTest {
    @Mock RiskAssessmentRepository assessmentRepository;
    @Mock UnderwritingDecisionRepository decisionRepository;
    @Mock UnderwritingReferralRepository referralRepository;
    @Mock SubjectivityRepository subjectivityRepository;
    @Mock R2dbcEntityTemplate entityTemplate;
    @Mock SecurityActorService security;
    @InjectMocks UnderwritingDecisionService service;

    private final UUID assessmentId = UUID.randomUUID();
    private final UUID actor = UUID.randomUUID();

    private RiskAssessment assessment() {
        return RiskAssessment.builder().assessmentId(assessmentId).assessmentNumber("RA-1").quoteId(UUID.randomUUID())
                .status(RiskAssessmentStatus.UNDER_REVIEW).assignedUnderwriterId(actor).riskScore(new BigDecimal("30"))
                .riskBand(RiskBand.MODERATE).assessmentDate(LocalDate.now()).build();
    }

    private CreateUnderwritingDecisionRequest request(UnderwritingOutcome outcome, boolean withinAuthority, BigDecimal limit, String conditions) {
        return new CreateUnderwritingDecisionRequest(outcome, " rationale ", " authority ", withinAuthority,
                limit, limit == null ? null : BigDecimal.ONE, limit == null ? null : BigDecimal.TEN, conditions, " rules ");
    }

    private void common(RiskAssessment assessment) {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Mono.just(assessment));
        when(security.currentUserId()).thenReturn(Mono.just(actor));
        when(referralRepository.findAllByAssessmentIdAndStatusIn(eq(assessmentId), any())).thenReturn(Flux.empty());
        when(subjectivityRepository.findAllByAssessmentIdAndRequiredBeforeBindTrue(assessmentId)).thenReturn(Flux.empty());
        EntityTemplateStubber.stubInsert(entityTemplate, UnderwritingDecision.class);
        when(entityTemplate.update(any(RiskAssessment.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
    }

    @Test
    void recordsEveryDecisionOutcomeAndAppliesAssessmentStatus() {
        for (UnderwritingOutcome outcome : UnderwritingOutcome.values()) {
            RiskAssessment assessment = assessment(); common(assessment);
            String conditions = outcome == UnderwritingOutcome.APPROVED_WITH_CONDITIONS ? "conditions" : null;
            StepVerifier.create(service.recordDecision(assessmentId, request(outcome, true, outcome == UnderwritingOutcome.APPROVED || outcome == UnderwritingOutcome.APPROVED_WITH_CONDITIONS ? BigDecimal.TEN : null, conditions)))
                    .assertNext(response -> assertEquals(outcome, response.outcome())).verifyComplete();
            assertEquals(outcome == UnderwritingOutcome.MORE_INFORMATION_REQUIRED ? RiskAssessmentStatus.NEEDS_INFORMATION : outcome == UnderwritingOutcome.REFERRED ? RiskAssessmentStatus.REFERRED : RiskAssessmentStatus.COMPLETED, assessment.getStatus());
        }
    }

    @Test
    void validatesAuthorityTermsReferralsSubjectivitiesAndAssignment() {
        RiskAssessment assessment = assessment(); common(assessment);
        StepVerifier.create(service.recordDecision(assessmentId, request(UnderwritingOutcome.APPROVED, false, BigDecimal.TEN, null))).expectError(BusinessException.class).verify();
        StepVerifier.create(service.recordDecision(assessmentId, request(UnderwritingOutcome.APPROVED, true, null, null))).expectError(BusinessException.class).verify();
        StepVerifier.create(service.recordDecision(assessmentId, request(UnderwritingOutcome.APPROVED_WITH_CONDITIONS, true, BigDecimal.TEN, null))).expectError(BusinessException.class).verify();
        assessment.setStatus(RiskAssessmentStatus.DRAFT);
        StepVerifier.create(service.recordDecision(assessmentId, request(UnderwritingOutcome.DECLINED, true, null, null))).expectError().verify();
        assessment.setStatus(RiskAssessmentStatus.UNDER_REVIEW); assessment.setRiskScore(null);
        StepVerifier.create(service.recordDecision(assessmentId, request(UnderwritingOutcome.DECLINED, true, null, null))).expectError().verify();
        assessment.setRiskScore(BigDecimal.TEN); assessment.setRiskBand(RiskBand.LOW); assessment.setAssignedUnderwriterId(null);
        StepVerifier.create(service.recordDecision(assessmentId, request(UnderwritingOutcome.DECLINED, true, null, null))).expectError().verify();
        assessment.setAssignedUnderwriterId(actor); when(security.currentUserId()).thenReturn(Mono.just(UUID.randomUUID()));
        StepVerifier.create(service.recordDecision(assessmentId, request(UnderwritingOutcome.DECLINED, true, null, null))).expectError().verify();
        when(security.currentUserId()).thenReturn(Mono.just(actor));
        when(referralRepository.findAllByAssessmentIdAndStatusIn(eq(assessmentId), any())).thenReturn(Flux.just(UnderwritingReferral.builder().status(ReferralStatus.OPEN).build()));
        StepVerifier.create(service.recordDecision(assessmentId, request(UnderwritingOutcome.APPROVED, true, BigDecimal.TEN, null))).expectError(BusinessException.class).verify();
        StepVerifier.create(service.recordDecision(assessmentId, request(UnderwritingOutcome.REFERRED, true, null, null))).expectNextCount(1).verifyComplete();
        assessment.setStatus(RiskAssessmentStatus.UNDER_REVIEW);
        when(subjectivityRepository.findAllByAssessmentIdAndRequiredBeforeBindTrue(assessmentId)).thenReturn(Flux.just(
                Subjectivity.builder().status(SubjectivityStatus.OUTSTANDING).build(),
                Subjectivity.builder().status(SubjectivityStatus.SATISFIED).build(),
                Subjectivity.builder().status(SubjectivityStatus.WAIVED).build()));
        when(referralRepository.findAllByAssessmentIdAndStatusIn(eq(assessmentId), any())).thenReturn(Flux.empty());
        StepVerifier.create(service.recordDecision(assessmentId, request(UnderwritingOutcome.APPROVED, true, BigDecimal.TEN, null))).expectError(BusinessException.class).verify();
        assessment.setStatus(RiskAssessmentStatus.UNDER_REVIEW);
        when(subjectivityRepository.findAllByAssessmentIdAndRequiredBeforeBindTrue(assessmentId)).thenReturn(Flux.empty());
        StepVerifier.create(service.recordDecision(assessmentId, new CreateUnderwritingDecisionRequest(UnderwritingOutcome.APPROVED, "r", "a", true, BigDecimal.ONE, null, BigDecimal.TEN, null, null))).expectError(BusinessException.class).verify();
        StepVerifier.create(service.recordDecision(assessmentId, new CreateUnderwritingDecisionRequest(UnderwritingOutcome.APPROVED, "r", "a", true, BigDecimal.ONE, BigDecimal.ONE, null, null, null))).expectError(BusinessException.class).verify();
        StepVerifier.create(service.recordDecision(assessmentId, new CreateUnderwritingDecisionRequest(UnderwritingOutcome.REFERRED, "r", "a", true, null, null, null, "", ""))).expectNextCount(1).verifyComplete();
    }

    @Test
    void decisionQueriesReturnHistoryLatestAndQuoteResultOrErrors() {
        RiskAssessment assessment = assessment();
        UnderwritingDecision decision = UnderwritingDecision.builder().decisionId(UUID.randomUUID()).assessmentId(assessmentId).quoteId(assessment.getQuoteId()).outcome(UnderwritingOutcome.APPROVED).build();
        when(assessmentRepository.findById(assessmentId)).thenReturn(Mono.just(assessment));
        when(decisionRepository.findAllByAssessmentIdOrderByDecidedAtDesc(assessmentId)).thenReturn(Flux.just(decision));
        when(decisionRepository.findFirstByAssessmentIdOrderByDecidedAtDesc(assessmentId)).thenReturn(Mono.just(decision));
        StepVerifier.create(service.getDecisionHistory(assessmentId)).expectNextCount(1).verifyComplete();
        StepVerifier.create(service.getLatestDecision(assessmentId)).expectNextCount(1).verifyComplete();
        when(decisionRepository.findFirstByAssessmentIdOrderByDecidedAtDesc(assessmentId)).thenReturn(Mono.empty());
        StepVerifier.create(service.getLatestDecision(assessmentId)).expectError().verify();
        when(assessmentRepository.findByQuoteId(assessment.getQuoteId())).thenReturn(Mono.just(assessment));
        when(decisionRepository.findFirstByAssessmentIdOrderByDecidedAtDesc(assessmentId)).thenReturn(Mono.just(decision));
        StepVerifier.create(service.getResultByQuoteId(assessment.getQuoteId())).expectNextCount(1).verifyComplete();
        when(assessmentRepository.findByQuoteId(any())).thenReturn(Mono.empty());
        StepVerifier.create(service.getResultByQuoteId(UUID.randomUUID())).expectError().verify();
        when(assessmentRepository.findById(assessmentId)).thenReturn(Mono.empty());
        StepVerifier.create(service.getDecisionHistory(assessmentId)).expectError().verify();
    }
}
