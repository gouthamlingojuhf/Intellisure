package com.intellisure.claimsservice.controller;

import com.intellisure.claimsservice.dto.*;
import com.intellisure.claimsservice.entity.*;
import com.intellisure.claimsservice.mapper.*;
import com.intellisure.claimsservice.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClaimsControllerCoverageTest {
    @Mock BusinessIncomeService businessIncomeService;
    @Mock ClaimFinancialsService financialsService;
    @Mock CoverageDecisionService decisionService;
    @Mock PaymentService paymentService;
    @Mock SalvageService salvageService;
    @Mock SubrogationService subrogationService;
    @Mock ClaimService claimService;

    private final UUID id = UUID.randomUUID();
    private final UUID user = UUID.randomUUID();

    @Test
    void businessIncomeControllerDelegatesAllOperations() {
        BusinessIncomeController c = new BusinessIncomeController(businessIncomeService, BusinessIncomeMapper.INSTANCE);
        BusinessIncome income = BusinessIncome.builder().businessIncomeId(id).claimId(id).estimatedLoss(BigDecimal.ONE).build();
        when(businessIncomeService.createBusinessIncome(any(), any(), any(), any(), any(), any(), any())).thenReturn(Mono.just(income));
        when(businessIncomeService.getBusinessIncome(id)).thenReturn(Mono.just(income));
        when(businessIncomeService.updateBusinessIncome(any(), any(), any(), any(), any(), any(), any())).thenReturn(Mono.just(income));
        when(businessIncomeService.recordLoss(any(), any(), any())).thenReturn(Mono.just(income));
        when(businessIncomeService.recordActualLoss(any(), any(), any())).thenReturn(Mono.just(income));
        when(businessIncomeService.confirmCoverage(any(), any())).thenReturn(Mono.just(income));
        when(businessIncomeService.calculateLoss(any(), any(), any(), any())).thenReturn(Mono.just(income));
        CreateBusinessIncomeRequest create = new CreateBusinessIncomeRequest(BigDecimal.TEN, 2, 30, LocalDate.now(), LocalDate.now().plusDays(1), user);
        UpdateBusinessIncomeRequest update = new UpdateBusinessIncomeRequest(BigDecimal.ONE, 1, 2, null, null, user);
        RecordBusinessIncomeLossRequest loss = new RecordBusinessIncomeLossRequest(BigDecimal.ONE, user);
        CalculateBusinessIncomeLossRequest calculate = new CalculateBusinessIncomeLossRequest(BigDecimal.TEN, 2, user);
        StepVerifier.create(c.createBusinessIncome(id, create)).expectNextCount(1).verifyComplete();
        StepVerifier.create(c.getBusinessIncome(id)).expectNextCount(1).verifyComplete();
        StepVerifier.create(c.updateBusinessIncome(id, update)).expectNextCount(1).verifyComplete();
        StepVerifier.create(c.recordLoss(id, loss)).expectNextCount(1).verifyComplete();
        StepVerifier.create(c.recordActualLoss(id, loss)).expectNextCount(1).verifyComplete();
        StepVerifier.create(c.confirmCoverage(id, user)).expectNextCount(1).verifyComplete();
        StepVerifier.create(c.calculateLoss(id, calculate)).expectNextCount(1).verifyComplete();
    }

    @Test
    void claimControllerDelegatesLifecycleAndValidatesAdjusterInput() {
        ClaimController c = new ClaimController(claimService);
        ClaimResponse response = new ClaimResponse(id, id, user, "CLM-1", "OPEN", LocalDate.now(), LocalDate.now(), "loss", BigDecimal.ONE, null, null, LocalDateTime.now(), LocalDateTime.now(), null, null, null, null, false, null);
        FileClaimRequest request = new FileClaimRequest(id, LocalDate.now(), "loss", BigDecimal.ONE);
        when(claimService.fileClaim(request)).thenReturn(Mono.just(response));
        when(claimService.getClaimsForCaller(any(), any())).thenReturn(Flux.just(response));
        when(claimService.getClaim(id)).thenReturn(Mono.just(response));
        when(claimService.getClaimByNumber("CLM-1")).thenReturn(Mono.just(response));
        when(claimService.updateStatus(any(), any())).thenReturn(Mono.just(response));
        when(claimService.assignAdjuster(any(), any())).thenReturn(Mono.just(response));
        when(claimService.resignAdjuster(any())).thenReturn(Mono.just(response));
        when(claimService.createAssessment(any(), anyString(), anyString(), anyBoolean(), any(), any(), any(), any(), any())).thenReturn(Mono.just(response));
        when(claimService.createReserve(any(), any(), anyString(), any())).thenReturn(Mono.just(response));
        when(claimService.calculatePayout(any(), any(), any(), any())).thenReturn(Mono.just(response));
        when(claimService.recordClaimDecision(any(), anyString(), any(), any())).thenReturn(Mono.just(response));
        when(claimService.approveSettlement(any(), any(), any())).thenReturn(Mono.just(response));
        when(claimService.recordPayment(any(), anyString())).thenReturn(Mono.just(response));
        when(claimService.closeClaim(any(), anyString())).thenReturn(Mono.just(response));
        UUID adjuster = UUID.randomUUID();
        StepVerifier.create(c.fileClaim(request)).expectNext(response).verifyComplete();
        StepVerifier.create(c.fnol(request)).expectNext(response).verifyComplete();
        StepVerifier.create(c.claims(user, "OPEN")).expectNext(response).verifyComplete();
        StepVerifier.create(c.get(id)).expectNext(response).verifyComplete();
        StepVerifier.create(c.getByClaimNumber("CLM-1")).expectNext(response).verifyComplete();
        StepVerifier.create(c.status(id, "OPEN")).expectNext(response).verifyComplete();
        StepVerifier.create(c.assignAdjuster(id, Map.of("adjusterId", adjuster))).expectNext(response).verifyComplete();
        assertThrows(IllegalArgumentException.class, () -> c.assignAdjuster(id, Map.of()));
        StepVerifier.create(c.resignAdjuster(id)).expectNext(response).verifyComplete();
        StepVerifier.create(c.createAssessment(id, "FIRE", "findings", true, BigDecimal.TEN, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, null)).expectNext(response).verifyComplete();
        StepVerifier.create(c.createAssessment(id, "FIRE", "findings", true, BigDecimal.TEN, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, user)).expectNext(response).verifyComplete();
        StepVerifier.create(c.createReserve(id, BigDecimal.ONE, "reason", null)).expectNext(response).verifyComplete();
        StepVerifier.create(c.createReserve(id, BigDecimal.ONE, "reason", user)).expectNext(response).verifyComplete();
        StepVerifier.create(c.calculatePayout(id, BigDecimal.TEN, BigDecimal.ONE, BigDecimal.ONE)).expectNext(response).verifyComplete();
        StepVerifier.create(c.recordDecision(id, "APPROVED", "reason", null)).expectNext(response).verifyComplete();
        StepVerifier.create(c.recordDecision(id, "APPROVED", "reason", user)).expectNext(response).verifyComplete();
        StepVerifier.create(c.approveSettlement(id, user, BigDecimal.ONE)).expectNext(response).verifyComplete();
        StepVerifier.create(c.recordPayment(id, "PAY-1")).expectNext(response).verifyComplete();
        StepVerifier.create(c.closeClaim(id, "done")).expectNext(response).verifyComplete();
        StepVerifier.create(c.closeClaimPost(id, "done")).expectNext(response).verifyComplete();
    }

    @Test
    void financialsCoveragePaymentSalvageAndSubrogationControllersDelegate() {
        ClaimFinancials financials = ClaimFinancials.builder().financialId(id).claimId(id).build();
        ClaimFinancialsController fc = new ClaimFinancialsController(financialsService, ClaimFinancialsMapper.INSTANCE);
        when(financialsService.initializeFinancials(any(), any(), any())).thenReturn(Mono.just(financials));
        when(financialsService.getFinancials(id)).thenReturn(Mono.just(financials));
        when(financialsService.updateReserve(any(), any(), anyString(), any())).thenReturn(Mono.just(financials));
        when(financialsService.recordPayment(any(), any(), any())).thenReturn(Mono.just(financials));
        when(financialsService.updateIncurred(any(), any(), any())).thenReturn(Mono.just(financials));
        StepVerifier.create(fc.initializeFinancials(id, new InitiateFinancialsRequest(BigDecimal.ONE, user))).expectNextCount(1).verifyComplete();
        StepVerifier.create(fc.getFinancials(id)).expectNextCount(1).verifyComplete();
        StepVerifier.create(fc.updateReserve(id, new UpdateReserveRequest(BigDecimal.ONE, "reason", user))).expectNextCount(1).verifyComplete();
        StepVerifier.create(fc.recordPayment(id, new RecordPaymentRequest(BigDecimal.ONE, user))).expectNextCount(1).verifyComplete();
        StepVerifier.create(fc.updateIncurred(id, new UpdateIncurredRequest(BigDecimal.ONE, user))).expectNextCount(1).verifyComplete();

        CoverageDecision decision = CoverageDecision.builder().decisionId(id).claimId(id).coverageCode("PROP").decision("APPROVED").build();
        CoverageDecisionController dc = new CoverageDecisionController(decisionService, CoverageDecisionMapper.INSTANCE);
        when(decisionService.createDecision(any(), anyString(), anyString(), any(), any())).thenReturn(Mono.just(decision));
        when(decisionService.getDecisionsByClaim(id)).thenReturn(Flux.just(decision));
        when(decisionService.getDecision(id)).thenReturn(Mono.just(decision));
        StepVerifier.create(dc.createDecision(id, new CreateCoverageDecisionRequest("PROP", "APPROVED", "ok", user))).expectNextCount(1).verifyComplete();
        StepVerifier.create(dc.getDecisions(id)).expectNextCount(1).verifyComplete();
        StepVerifier.create(dc.getDecision(id, id)).expectNextCount(1).verifyComplete();
        StepVerifier.create(dc.getDecisionByCoverage(id, "PROP")).expectNextCount(1).verifyComplete();

        Payment payment = Payment.builder().paymentId(id).claimId(id).paymentReference("PAY-1").amount(BigDecimal.ONE).build();
        PaymentController pc = new PaymentController(paymentService, PaymentMapper.INSTANCE);
        when(paymentService.createPayment(any(), anyString(), any(), anyString(), anyString(), any(), any(), any())).thenReturn(Mono.just(payment));
        when(paymentService.getPayments(id)).thenReturn(Flux.just(payment));
        when(paymentService.getPayment(id)).thenReturn(Mono.just(payment));
        when(paymentService.getPaymentByReference("PAY-1")).thenReturn(Mono.just(payment));
        when(paymentService.updatePaymentStatus(any(), anyString())).thenReturn(Mono.just(payment));
        when(paymentService.completePayment(id)).thenReturn(Mono.just(payment));
        when(paymentService.getPendingPayments(id)).thenReturn(Flux.just(payment));
        CreatePaymentRequest cp = new CreatePaymentRequest("PAY-1", BigDecimal.ONE, "INDEMNITY", "ACH", null, null, user);
        StepVerifier.create(pc.createPayment(id, cp)).expectNextCount(1).verifyComplete();
        StepVerifier.create(pc.getPayments(id)).expectNextCount(1).verifyComplete();
        StepVerifier.create(pc.getPayment(id, id)).expectNextCount(1).verifyComplete();
        StepVerifier.create(pc.getPaymentByReference(id, "PAY-1")).expectNextCount(1).verifyComplete();
        StepVerifier.create(pc.updatePaymentStatus(id, id, new UpdatePaymentStatusRequest("COMPLETED"))).expectNextCount(1).verifyComplete();
        StepVerifier.create(pc.completePayment(id, id)).expectNextCount(1).verifyComplete();
        StepVerifier.create(pc.getPendingPayments(id)).expectNextCount(1).verifyComplete();

        Salvage salvage = Salvage.builder().salvageId(id).claimId(id).description("x").build();
        SalvageController sc = new SalvageController(salvageService, SalvageMapper.INSTANCE);
        when(salvageService.createSalvage(any(), anyString(), any(), any(), any())).thenReturn(Mono.just(salvage));
        when(salvageService.getSalvages(id)).thenReturn(Flux.just(salvage));
        when(salvageService.getSalvage(id)).thenReturn(Mono.just(salvage));
        when(salvageService.updateSalvage(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(Mono.just(salvage));
        when(salvageService.recordSale(any(), any(), any(), any(), any())).thenReturn(Mono.just(salvage));
        when(salvageService.getPendingSalvages(id)).thenReturn(Flux.just(salvage));
        when(salvageService.getSoldSalvages(id)).thenReturn(Flux.just(salvage));
        StepVerifier.create(sc.createSalvage(id, new CreateSalvageRequest("x", BigDecimal.ONE, "PENDING", user))).expectNextCount(1).verifyComplete();
        StepVerifier.create(sc.getSalvages(id)).expectNextCount(1).verifyComplete();
        StepVerifier.create(sc.getSalvage(id, id)).expectNextCount(1).verifyComplete();
        StepVerifier.create(sc.updateSalvage(id, id, new UpdateSalvageRequest("x", BigDecimal.ONE, "READY", "buyer", LocalDate.now(), BigDecimal.ONE, user))).expectNextCount(1).verifyComplete();
        StepVerifier.create(sc.recordSale(id, id, new RecordSalvageSaleRequest("buyer", LocalDate.now(), BigDecimal.ONE, user))).expectNextCount(1).verifyComplete();
        StepVerifier.create(sc.getPendingSalvages(id)).expectNextCount(1).verifyComplete();
        StepVerifier.create(sc.getSoldSalvages(id)).expectNextCount(1).verifyComplete();

        Subrogation subrogation = Subrogation.builder().subrogationId(id).claimId(id).thirdPartyName("x").build();
        SubrogationController subc = new SubrogationController(subrogationService, SubrogationMapper.INSTANCE);
        when(subrogationService.createSubrogation(any(), anyString(), any(), any(), any(), any())).thenReturn(Mono.just(subrogation));
        when(subrogationService.getSubrogations(id)).thenReturn(Flux.just(subrogation));
        when(subrogationService.getSubrogation(id)).thenReturn(Mono.just(subrogation));
        when(subrogationService.updateSubrogation(any(), any(), any(), any(), any(), any(), any())).thenReturn(Mono.just(subrogation));
        when(subrogationService.recordRecovery(any(), any(), any())).thenReturn(Mono.just(subrogation));
        when(subrogationService.closeSubrogation(any(), any())).thenReturn(Mono.just(subrogation));
        when(subrogationService.getOpenSubrogations(id)).thenReturn(Flux.just(subrogation));
        when(subrogationService.getClosedSubrogations(id)).thenReturn(Flux.just(subrogation));
        StepVerifier.create(subc.createSubrogation(id, new CreateSubrogationRequest("x", "carrier", BigDecimal.ONE, "note", user))).expectNextCount(1).verifyComplete();
        StepVerifier.create(subc.getSubrogations(id)).expectNextCount(1).verifyComplete();
        StepVerifier.create(subc.getSubrogation(id, id)).expectNextCount(1).verifyComplete();
        StepVerifier.create(subc.updateSubrogation(id, id, new UpdateSubrogationRequest("x", "carrier", BigDecimal.ONE, "OPEN", "note", user))).expectNextCount(1).verifyComplete();
        StepVerifier.create(subc.recordRecovery(id, id, new RecordSubrogationRecoveryRequest(BigDecimal.ONE, user))).expectNextCount(1).verifyComplete();
        StepVerifier.create(subc.closeSubrogation(id, id, user)).expectNextCount(1).verifyComplete();
        StepVerifier.create(subc.getOpenSubrogations(id)).expectNextCount(1).verifyComplete();
        StepVerifier.create(subc.getClosedSubrogations(id)).expectNextCount(1).verifyComplete();
    }
}
