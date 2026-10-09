package com.intellisure.riskunderwritingservice.controller;

import com.intellisure.riskunderwritingservice.dto.response.*;
import com.intellisure.riskunderwritingservice.enums.RiskAssessmentStatus;
import com.intellisure.riskunderwritingservice.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiskControllerCoverageTest {
    @Mock RiskAssessmentService assessmentService;
    @Mock RiskFindingService findingService;
    @Mock RiskRecommendationService recommendationService;
    @Mock UnderwritingDecisionService decisionService;
    @Mock UnderwritingWorkflowService workflowService;

    private final UUID id = UUID.randomUUID();
    private final RiskAssessmentResponse assessment = null;

    @Test
    void assessmentAndEvidenceControllersDelegate() {
        RiskAssessmentController ac = new RiskAssessmentController(assessmentService);
        when(assessmentService.createAssessment(any())).thenReturn(Mono.empty());
        when(assessmentService.startAssessment(id)).thenReturn(Mono.empty());
        when(assessmentService.assignAssessment(eq(id), any())).thenReturn(Mono.empty());
        when(assessmentService.submitForReview(id)).thenReturn(Mono.empty());
        when(assessmentService.completeRiskScore(eq(id), any())).thenReturn(Mono.empty());
        when(assessmentService.getById(id)).thenReturn(Mono.empty());
        when(assessmentService.getByNumber("RA-1")).thenReturn(Mono.empty());
        when(assessmentService.getByQuoteId(id)).thenReturn(Mono.empty());
        when(assessmentService.getByStatus(RiskAssessmentStatus.DRAFT)).thenReturn(Flux.empty());
        when(assessmentService.getAssignedUnderwriterQueue(id)).thenReturn(Flux.empty());
        when(assessmentService.getAssignedRiskEngineerQueue(id)).thenReturn(Flux.empty());
        StepVerifier.create(ac.create(null)).verifyComplete(); StepVerifier.create(ac.start(id)).verifyComplete();
        StepVerifier.create(ac.assign(id, null)).verifyComplete(); StepVerifier.create(ac.submitReview(id)).verifyComplete();
        StepVerifier.create(ac.completeRiskScore(id, null)).verifyComplete(); StepVerifier.create(ac.getById(id)).verifyComplete();
        StepVerifier.create(ac.getByNumber("RA-1")).verifyComplete(); StepVerifier.create(ac.getByQuoteId(id)).verifyComplete();
        StepVerifier.create(ac.getByStatus(RiskAssessmentStatus.DRAFT)).verifyComplete(); StepVerifier.create(ac.getUnderwriterQueue(id)).verifyComplete();
        StepVerifier.create(ac.getRiskEngineerQueue(id)).verifyComplete();

        RiskEvidenceController ec = new RiskEvidenceController(findingService, recommendationService);
        when(findingService.createFinding(eq(id), any())).thenReturn(Mono.empty()); when(findingService.getFindings(id)).thenReturn(Flux.empty());
        when(recommendationService.createRecommendation(eq(id), any())).thenReturn(Mono.empty()); when(recommendationService.getRecommendations(id)).thenReturn(Flux.empty());
        when(recommendationService.updateStatus(eq(id), any())).thenReturn(Mono.empty());
        StepVerifier.create(ec.createFinding(id, null)).verifyComplete(); StepVerifier.create(ec.getFindings(id)).verifyComplete();
        StepVerifier.create(ec.createRecommendation(id, null)).verifyComplete(); StepVerifier.create(ec.getRecommendations(id)).verifyComplete();
        StepVerifier.create(ec.updateRecommendationStatus(id, null)).verifyComplete();
    }

    @Test
    void decisionAndWorkflowControllersDelegate() {
        UnderwritingDecisionController dc = new UnderwritingDecisionController(decisionService);
        when(decisionService.recordDecision(eq(id), any())).thenReturn(Mono.empty()); when(decisionService.getDecisionHistory(id)).thenReturn(Flux.empty());
        when(decisionService.getLatestDecision(id)).thenReturn(Mono.empty()); when(decisionService.getResultByQuoteId(id)).thenReturn(Mono.empty());
        StepVerifier.create(dc.recordDecision(id, null)).verifyComplete(); StepVerifier.create(dc.getDecisionHistory(id)).verifyComplete();
        StepVerifier.create(dc.getLatestDecision(id)).verifyComplete(); StepVerifier.create(dc.getResultByQuoteId(id)).verifyComplete();

        UnderwritingWorkflowController wc = new UnderwritingWorkflowController(workflowService);
        when(workflowService.createReferral(eq(id), any())).thenReturn(Mono.empty()); when(workflowService.assignReferral(eq(id), any())).thenReturn(Mono.empty());
        when(workflowService.resolveReferral(eq(id), any())).thenReturn(Mono.empty()); when(workflowService.getReferrals(id)).thenReturn(Flux.empty());
        when(workflowService.createSubjectivity(eq(id), any())).thenReturn(Mono.empty()); when(workflowService.submitSubjectivityEvidence(eq(id), any())).thenReturn(Mono.empty());
        when(workflowService.verifySubjectivity(eq(id), any())).thenReturn(Mono.empty()); when(workflowService.waiveSubjectivity(eq(id), any())).thenReturn(Mono.empty());
        when(workflowService.getSubjectivities(id)).thenReturn(Flux.empty()); when(workflowService.getOutstandingBindSubjectivities(id)).thenReturn(Flux.empty());
        StepVerifier.create(wc.createReferral(id, null)).verifyComplete(); StepVerifier.create(wc.assignReferral(id, null)).verifyComplete();
        StepVerifier.create(wc.resolveReferral(id, null)).verifyComplete(); StepVerifier.create(wc.getReferrals(id)).verifyComplete();
        StepVerifier.create(wc.createSubjectivity(id, null)).verifyComplete(); StepVerifier.create(wc.submitSubjectivityEvidence(id, null)).verifyComplete();
        StepVerifier.create(wc.verifySubjectivity(id, null)).verifyComplete(); StepVerifier.create(wc.waiveSubjectivity(id, null)).verifyComplete();
        StepVerifier.create(wc.getSubjectivities(id)).verifyComplete(); StepVerifier.create(wc.getOutstandingBindSubjectivities(id)).verifyComplete();
    }
}
