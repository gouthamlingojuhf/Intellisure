package com.intellisure.riskunderwritingservice.service;

import com.intellisure.riskunderwritingservice.dto.request.*;
import com.intellisure.riskunderwritingservice.entity.*;
import com.intellisure.riskunderwritingservice.enums.*;
import com.intellisure.riskunderwritingservice.exception.BusinessException;
import com.intellisure.riskunderwritingservice.mapper.RiskEvidenceJsonMapper;
import com.intellisure.riskunderwritingservice.repository.*;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnderwritingWorkflowBranchCoverageTest {
    @Mock RiskAssessmentRepository assessmentRepository;
    @Mock UnderwritingReferralRepository referralRepository;
    @Mock SubjectivityRepository subjectivityRepository;
    @Mock R2dbcEntityTemplate entityTemplate;
    @Mock RiskEvidenceJsonMapper evidenceMapper;
    @InjectMocks UnderwritingWorkflowService service;

    private final UUID assessmentId = UUID.randomUUID();
    private final UUID referralId = UUID.randomUUID();
    private final UUID subjectivityId = UUID.randomUUID();

    private RiskAssessment assessment(RiskAssessmentStatus status) { return RiskAssessment.builder().assessmentId(assessmentId).quoteId(UUID.randomUUID()).status(status).build(); }
    private UnderwritingReferral referral(ReferralStatus status, UUID referredTo) { return UnderwritingReferral.builder().referralId(referralId).assessmentId(assessmentId).quoteId(UUID.randomUUID()).status(status).referredTo(referredTo).createdAt(LocalDateTime.now()).build(); }

    @Test
    void referralValidationAssignmentResolutionAndListingBranches() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Mono.just(assessment(RiskAssessmentStatus.DRAFT)));
        StepVerifier.create(service.createReferral(assessmentId, new CreateReferralRequest("reason", "MANAGER", UUID.randomUUID()))).expectError(BusinessException.class).verify();
        when(referralRepository.findById(referralId)).thenReturn(Mono.just(referral(ReferralStatus.RESOLVED, null)));
        StepVerifier.create(service.assignReferral(referralId, new AssignReferralRequest(UUID.randomUUID()))).expectError(BusinessException.class).verify();
        assertThrows(BusinessException.class, () -> service.resolveReferral(referralId, new ResolveReferralRequest(ReferralStatus.OPEN, "bad")));
        when(referralRepository.findById(referralId)).thenReturn(Mono.just(referral(ReferralStatus.OPEN, null)));
        StepVerifier.create(service.resolveReferral(referralId, new ResolveReferralRequest(ReferralStatus.RESOLVED, "done"))).expectError(BusinessException.class).verify();
        UnderwritingReferral returned = referral(ReferralStatus.OPEN, UUID.randomUUID());
        when(referralRepository.findById(referralId)).thenReturn(Mono.just(returned));
        when(referralRepository.save(any(UnderwritingReferral.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(assessmentRepository.findById(assessmentId)).thenReturn(Mono.just(assessment(RiskAssessmentStatus.REFERRED)));
        when(entityTemplate.update(any(RiskAssessment.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        StepVerifier.create(service.resolveReferral(referralId, new ResolveReferralRequest(ReferralStatus.RETURNED, null)))
                .assertNext(r -> assertEquals(ReferralStatus.RETURNED, r.status())).verifyComplete();
        when(referralRepository.findById(referralId)).thenReturn(Mono.just(referral(ReferralStatus.RESOLVED, UUID.randomUUID())));
        StepVerifier.create(service.resolveReferral(referralId, new ResolveReferralRequest(ReferralStatus.APPROVED, "again"))).expectError(BusinessException.class).verify();
        UnderwritingReferral reviewReferral = referral(ReferralStatus.UNDER_REVIEW, UUID.randomUUID());
        when(referralRepository.findById(referralId)).thenReturn(Mono.just(reviewReferral));
        when(assessmentRepository.findById(assessmentId)).thenReturn(Mono.just(assessment(RiskAssessmentStatus.REFERRED)));
        StepVerifier.create(service.resolveReferral(referralId, new ResolveReferralRequest(ReferralStatus.APPROVED, "approved")))
                .assertNext(r -> assertEquals(ReferralStatus.APPROVED, r.status())).verifyComplete();
        when(assessmentRepository.findById(assessmentId)).thenReturn(Mono.just(assessment(RiskAssessmentStatus.UNDER_REVIEW)));
        EntityTemplateStubber.stubInsert(entityTemplate, UnderwritingReferral.class);
        when(entityTemplate.update(any(RiskAssessment.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        StepVerifier.create(service.createReferral(assessmentId, new CreateReferralRequest("reason", "manager", UUID.randomUUID())))
                .assertNext(r -> assertEquals(ReferralStatus.OPEN, r.status())).verifyComplete();
        UnderwritingReferral older = referral(ReferralStatus.OPEN, null); older.setCreatedAt(LocalDateTime.now().minusDays(1));
        when(referralRepository.findAllByAssessmentId(assessmentId)).thenReturn(Flux.just(older, returned));
        StepVerifier.create(service.getReferrals(assessmentId)).expectNextCount(2).verifyComplete();
    }

    @Test
    void subjectivityStatusBranchesAndQueriesAreCovered() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Mono.just(assessment(RiskAssessmentStatus.COMPLETED)));
        StepVerifier.create(service.createSubjectivity(assessmentId, new CreateSubjectivityRequest("TYPE", "desc", true, LocalDate.now(), UUID.randomUUID())))
                .expectError(BusinessException.class).verify();
        when(assessmentRepository.findById(assessmentId)).thenReturn(Mono.just(assessment(RiskAssessmentStatus.NEEDS_INFORMATION)));
        when(subjectivityRepository.save(any(Subjectivity.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        StepVerifier.create(service.createSubjectivity(assessmentId, new CreateSubjectivityRequest(" type ", " desc ", true, LocalDate.now(), UUID.randomUUID())))
                .assertNext(r -> assertEquals(SubjectivityStatus.OUTSTANDING, r.status())).verifyComplete();
        when(assessmentRepository.findById(assessmentId)).thenReturn(Mono.just(assessment(RiskAssessmentStatus.REFERRED)));
        StepVerifier.create(service.createSubjectivity(assessmentId, new CreateSubjectivityRequest(" type ", " desc ", true, LocalDate.now(), UUID.randomUUID())))
                .assertNext(r -> assertEquals(SubjectivityStatus.OUTSTANDING, r.status())).verifyComplete();
        Subjectivity subjectivity = Subjectivity.builder().subjectivityId(subjectivityId).assessmentId(assessmentId).status(SubjectivityStatus.REJECTED).build();
        when(subjectivityRepository.findById(subjectivityId)).thenReturn(Mono.just(subjectivity));
        when(evidenceMapper.toJson(any())).thenReturn("[]"); when(evidenceMapper.fromJson(any())).thenReturn(List.of());
        StepVerifier.create(service.submitSubjectivityEvidence(subjectivityId, new SubmitSubjectivityRequest(List.of(UUID.randomUUID()))))
                .assertNext(r -> assertEquals(SubjectivityStatus.SUBMITTED, r.status())).verifyComplete();
        subjectivity.setStatus(SubjectivityStatus.SATISFIED);
        StepVerifier.create(service.submitSubjectivityEvidence(subjectivityId, new SubmitSubjectivityRequest(List.of())))
                .expectError(BusinessException.class).verify();
        subjectivity.setStatus(SubjectivityStatus.SUBMITTED);
        StepVerifier.create(service.verifySubjectivity(subjectivityId, new VerifySubjectivityRequest(SubjectivityStatus.REJECTED, UUID.randomUUID(), "rejected")))
                .assertNext(r -> assertEquals(SubjectivityStatus.REJECTED, r.status())).verifyComplete();
        assertThrows(BusinessException.class, () -> service.verifySubjectivity(subjectivityId, new VerifySubjectivityRequest(SubjectivityStatus.OUTSTANDING, UUID.randomUUID(), "bad")));
        subjectivity.setStatus(SubjectivityStatus.SUBMITTED);
        StepVerifier.create(service.verifySubjectivity(subjectivityId, new VerifySubjectivityRequest(SubjectivityStatus.SATISFIED, UUID.randomUUID(), "ok")))
                .expectNextCount(1).verifyComplete();
        subjectivity.setStatus(SubjectivityStatus.OUTSTANDING);
        StepVerifier.create(service.verifySubjectivity(subjectivityId, new VerifySubjectivityRequest(SubjectivityStatus.SATISFIED, UUID.randomUUID(), "not submitted")))
                .expectError(BusinessException.class).verify();
        subjectivity.setStatus(SubjectivityStatus.SATISFIED);
        StepVerifier.create(service.waiveSubjectivity(subjectivityId, new WaiveSubjectivityRequest(UUID.randomUUID(), "waive"))).expectError(BusinessException.class).verify();
        subjectivity.setStatus(SubjectivityStatus.OUTSTANDING);
        StepVerifier.create(service.waiveSubjectivity(subjectivityId, new WaiveSubjectivityRequest(UUID.randomUUID(), " waive ")))
                .assertNext(r -> assertEquals(SubjectivityStatus.WAIVED, r.status())).verifyComplete();
        subjectivity.setStatus(SubjectivityStatus.WAIVED);
        StepVerifier.create(service.waiveSubjectivity(subjectivityId, new WaiveSubjectivityRequest(UUID.randomUUID(), "waive again"))).expectError(BusinessException.class).verify();
        when(subjectivityRepository.findAllByAssessmentId(assessmentId)).thenReturn(Flux.just(subjectivity));
        when(subjectivityRepository.findAllByAssessmentIdAndRequiredBeforeBindTrue(assessmentId)).thenReturn(Flux.just(
                subjectivity, Subjectivity.builder().status(SubjectivityStatus.SATISFIED).build(), Subjectivity.builder().status(SubjectivityStatus.WAIVED).build()));
        when(assessmentRepository.findById(assessmentId)).thenReturn(Mono.just(assessment(RiskAssessmentStatus.UNDER_REVIEW)));
        StepVerifier.create(service.getSubjectivities(assessmentId)).expectNextCount(1).verifyComplete();
        subjectivity.setStatus(SubjectivityStatus.OUTSTANDING);
        StepVerifier.create(service.getOutstandingBindSubjectivities(assessmentId)).expectNextCount(1).verifyComplete();
        when(subjectivityRepository.findById(subjectivityId)).thenReturn(Mono.empty());
        StepVerifier.create(service.waiveSubjectivity(subjectivityId, new WaiveSubjectivityRequest(UUID.randomUUID(), "x"))).expectError().verify();
    }
}
