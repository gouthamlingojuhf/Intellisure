package com.intellisure.quotepolicyservice.controller;

import com.intellisure.quotepolicyservice.dto.*;
import com.intellisure.quotepolicyservice.dto.request.*;
import com.intellisure.quotepolicyservice.dto.response.*;
import com.intellisure.quotepolicyservice.service.*;
import com.intellisure.quotepolicyservice.service.assignment.UnderwriterAssignmentService;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QuotePolicyControllerCoverageTest {
    private final UUID id = UUID.randomUUID();

    @Test
    void delegatesQuoteAndPolicyEndpoints() {
        QuoteService quotes = mock(QuoteService.class);
        PolicyService policies = mock(PolicyService.class);
        QuoteResponse quote = mock(QuoteResponse.class);
        PolicyResponse policy = mock(PolicyResponse.class);
        UnderwritingDecisionResponse decision = mock(UnderwritingDecisionResponse.class);
        when(quotes.createDraftQuote(any())).thenReturn(Mono.just(quote));
        when(quotes.recordUnderwritingDecision(any(), any())).thenReturn(Mono.just(decision));
        when(quotes.getUnderwritingDecisionHistory(any())).thenReturn(Flux.just(decision));
        when(quotes.offerQuoteTerms(any(), any())).thenReturn(Mono.just(quote));
        when(quotes.submitQuote(any())).thenReturn(Mono.just(quote));
        when(quotes.acceptQuote(any())).thenReturn(Mono.just(quote));
        when(quotes.declineQuoteByCustomer(any(), any())).thenReturn(Mono.just(quote));
        when(quotes.reassignUnderwriter(any(), any())).thenReturn(Mono.just(quote));
        when(quotes.getQuoteById(any())).thenReturn(Mono.just(quote));
        when(quotes.getQuoteByNumber(any())).thenReturn(Mono.just(quote));
        when(quotes.getQuotesByCustomerId(any())).thenReturn(Flux.just(quote));
        when(quotes.getAllQuotesForAdministration()).thenReturn(Flux.just(quote));
        when(policies.bindQuote(any())).thenReturn(Mono.just(policy));

        QuoteController controller = new QuoteController(quotes, policies);
        StepVerifier.create(controller.createDraftQuote(mock(CreateQuoteRequest.class))).expectNext(quote).verifyComplete();
        StepVerifier.create(controller.recordUnderwritingDecision(id, mock(RecordUnderwritingDecisionRequest.class))).expectNext(decision).verifyComplete();
        StepVerifier.create(controller.getUnderwritingDecisionHistory(id)).expectNext(decision).verifyComplete();
        StepVerifier.create(controller.offerQuoteTerms(id, mock(OfferQuoteTermsRequest.class))).expectNext(quote).verifyComplete();
        StepVerifier.create(controller.submitQuote(id)).expectNext(quote).verifyComplete();
        StepVerifier.create(controller.acceptQuote(id)).expectNext(quote).verifyComplete();
        StepVerifier.create(controller.bindQuote(id)).expectNext(policy).verifyComplete();
        StepVerifier.create(controller.declineQuote(id, mock(DeclineQuoteRequest.class))).expectNext(quote).verifyComplete();
        StepVerifier.create(controller.reassignUnderwriter(id, mock(AssignUnderwriterRequest.class))).expectNext(quote).verifyComplete();
        StepVerifier.create(controller.getQuoteById(id)).expectNext(quote).verifyComplete();
        StepVerifier.create(controller.getQuoteByNumber("QTE-1")).expectNext(quote).verifyComplete();
        StepVerifier.create(controller.getQuotesByCustomerId(id)).expectNext(quote).verifyComplete();
        StepVerifier.create(controller.getAllQuotesForAdministration()).expectNext(quote).verifyComplete();
    }

    @Test
    void delegatesPolicyEndpoints() {
        PolicyService policies = mock(PolicyService.class);
        PolicyResponse policy = mock(PolicyResponse.class);
        PolicyStatusResponse status = mock(PolicyStatusResponse.class);
        CoverageCheckResponse coverage = mock(CoverageCheckResponse.class);
        when(policies.bindQuote(any())).thenReturn(Mono.just(policy));
        when(policies.issuePolicy(any())).thenReturn(Mono.just(policy));
        when(policies.cancelPolicy(any(), any())).thenReturn(Mono.just(policy));
        when(policies.reinstatePolicy(any(), any())).thenReturn(Mono.just(policy));
        when(policies.checkPolicyStatus(any(), any())).thenReturn(Mono.just(status));
        when(policies.getPolicyById(any())).thenReturn(Mono.just(policy));
        when(policies.getPolicyByNumber(any())).thenReturn(Mono.just(policy));
        when(policies.getPoliciesByCustomerId(any())).thenReturn(Flux.just(policy));
        when(policies.getAllPoliciesForAdministration()).thenReturn(Flux.just(policy));
        when(policies.checkCoverage(any(), any(), any())).thenReturn(Mono.just(coverage));

        PolicyController controller = new PolicyController(policies);
        StepVerifier.create(controller.bindQuote(id)).expectNext(policy).verifyComplete();
        StepVerifier.create(controller.issuePolicy(id)).expectNext(policy).verifyComplete();
        StepVerifier.create(controller.cancelPolicy(id, "reason")).expectNext(policy).verifyComplete();
        StepVerifier.create(controller.reinstatePolicy(id, "reason")).expectNext(policy).verifyComplete();
        StepVerifier.create(controller.checkPolicyStatus("POL-1", LocalDate.now())).expectNext(status).verifyComplete();
        StepVerifier.create(controller.getPolicyById(id)).expectNext(policy).verifyComplete();
        StepVerifier.create(controller.getPolicyByNumber("POL-1")).expectNext(policy).verifyComplete();
        StepVerifier.create(controller.getPoliciesByCustomerId(id)).expectNext(policy).verifyComplete();
        StepVerifier.create(controller.getAllPoliciesForAdministration()).expectNext(policy).verifyComplete();
        StepVerifier.create(controller.checkCoverage("POL-1", "FIRE", LocalDate.now())).expectNext(coverage).verifyComplete();
    }

    @Test
    void delegatesExtendedLifecycleControllers() {
        EndorsementService endorsements = mock(EndorsementService.class);
        PremiumAuditService audits = mock(PremiumAuditService.class);
        RenewalService renewals = mock(RenewalService.class);
        SubjectivityService subjectivities = mock(SubjectivityService.class);
        UnderwritingImportService imports = mock(UnderwritingImportService.class);
        EndorsementResponse endorsement = mock(EndorsementResponse.class);
        PremiumAuditResponse audit = mock(PremiumAuditResponse.class);
        RenewalTransactionResponse renewal = mock(RenewalTransactionResponse.class);
        SubjectivityResponse subjectivity = mock(SubjectivityResponse.class);
        ImportedUnderwritingResultResponse imported = mock(ImportedUnderwritingResultResponse.class);
        when(endorsements.requestEndorsement(any(), any())).thenReturn(Mono.just(endorsement));
        when(endorsements.approveEndorsement(any(), any())).thenReturn(Mono.just(endorsement));
        when(endorsements.rejectEndorsement(any(), any(), any())).thenReturn(Mono.just(endorsement));
        when(endorsements.issueEndorsement(any())).thenReturn(Mono.just(endorsement));
        when(endorsements.applyEndorsementToPolicy(any())).thenReturn(Mono.just(mock(com.intellisure.quotepolicyservice.entity.Endorsement.class)));
        when(endorsements.getEndorsements(any())).thenReturn(Flux.just(endorsement));
        when(audits.initiateAudit(any(), any())).thenReturn(Mono.just(audit));
        when(audits.completeAudit(any(), any())).thenReturn(Mono.just(audit));
        when(audits.cancelAudit(any())).thenReturn(Mono.just(audit));
        when(audits.getAudits(any())).thenReturn(Flux.just(audit));
        when(renewals.initiateRenewal(any(), any())).thenReturn(Mono.just(renewal));
        when(renewals.decideRenewal(any(), any())).thenReturn(Mono.just(renewal));
        when(renewals.bindRenewal(any(), any())).thenReturn(Mono.just(renewal));
        when(renewals.issueRenewal(any())).thenReturn(Mono.just(renewal));
        when(renewals.getRenewals(any())).thenReturn(Flux.just(renewal));
        when(subjectivities.addSubjectivity(any(), any())).thenReturn(Mono.just(subjectivity));
        when(subjectivities.satisfySubjectivity(any(), any())).thenReturn(Mono.just(subjectivity));
        when(subjectivities.waiveSubjectivity(any(), any())).thenReturn(Mono.just(subjectivity));
        when(subjectivities.getSubjectivities(any())).thenReturn(Flux.just(subjectivity));
        when(imports.importResult(any(), any())).thenReturn(Mono.just(imported));

        EndorsementController e = new EndorsementController(endorsements);
        StepVerifier.create(e.requestEndorsement(id, mock(RequestEndorsementRequest.class))).expectNextCount(1).verifyComplete();
        StepVerifier.create(e.approveEndorsement(id, id, id)).expectNextCount(1).verifyComplete();
        StepVerifier.create(e.rejectEndorsement(id, id, "reason", id)).expectNextCount(1).verifyComplete();
        StepVerifier.create(e.issueEndorsement(id, id)).expectNextCount(1).verifyComplete();
        StepVerifier.create(e.applyEndorsement(id, id)).expectNextCount(1).verifyComplete();
        StepVerifier.create(e.getEndorsements(id)).expectNext(endorsement).verifyComplete();

        PremiumAuditController a = new PremiumAuditController(audits);
        StepVerifier.create(a.initiateAudit(id, mock(InitiatePremiumAuditRequest.class))).expectNextCount(1).verifyComplete();
        StepVerifier.create(a.completeAudit(id, id, mock(CompletePremiumAuditRequest.class))).expectNextCount(1).verifyComplete();
        StepVerifier.create(a.cancelAudit(id, id)).expectNextCount(1).verifyComplete();
        StepVerifier.create(a.getAudits(id)).expectNext(audit).verifyComplete();

        RenewalController r = new RenewalController(renewals);
        StepVerifier.create(r.initiateRenewal(id, mock(InitiateRenewalRequest.class))).expectNextCount(1).verifyComplete();
        StepVerifier.create(r.decideRenewal(id, id, mock(RenewalDecisionRequest.class))).expectNextCount(1).verifyComplete();
        StepVerifier.create(r.bindRenewal(id, id, mock(BindRenewalRequest.class))).expectNextCount(1).verifyComplete();
        StepVerifier.create(r.issueRenewal(id, id)).expectNextCount(1).verifyComplete();
        StepVerifier.create(r.getRenewals(id)).expectNext(renewal).verifyComplete();

        SubjectivityController s = new SubjectivityController(subjectivities);
        StepVerifier.create(s.addSubjectivity(id, mock(AddSubjectivityRequest.class))).expectNextCount(1).verifyComplete();
        StepVerifier.create(s.satisfySubjectivity(id, id, mock(SatisfySubjectivityRequest.class))).expectNextCount(1).verifyComplete();
        StepVerifier.create(s.waiveSubjectivity(id, id, mock(WaiveSubjectivityRequest.class))).expectNextCount(1).verifyComplete();
        StepVerifier.create(s.getSubjectivities(id)).expectNext(subjectivity).verifyComplete();
        StepVerifier.create(new UnderwritingIntegrationController(imports).importUnderwritingResult(id, "Bearer token"))
                .expectNext(imported).verifyComplete();
    }
}
