package com.intellisure.claimsservice.service;

import com.intellisure.claimsservice.entity.*;
import com.intellisure.claimsservice.repository.*;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimsSupportingServicesTest {

    @Mock BusinessIncomeRepository businessIncomeRepository;
    @Mock ClaimFinancialsRepository financialsRepository;
    @Mock CoverageDecisionRepository decisionRepository;
    @Mock PaymentRepository paymentRepository;
    @Mock SalvageRepository salvageRepository;
    @Mock SubrogationRepository subrogationRepository;
    @Mock ClaimRepository claimRepository;
    @Mock R2dbcEntityTemplate entityTemplate;

    @Test
    void businessIncomeCoversCreateUpdateLossConfirmationAndValidation() {
        BusinessIncomeService service = new BusinessIncomeService(businessIncomeRepository, claimRepository, entityTemplate);
        UUID claimId = UUID.randomUUID();
        Claim claim = Claim.builder().claimId(claimId).build();
        when(claimRepository.findById(claimId)).thenReturn(Mono.just(claim));
        when(entityTemplate.insert(any(BusinessIncome.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(entityTemplate.update(any(BusinessIncome.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(businessIncomeRepository.findByClaimId(claimId)).thenReturn(Mono.empty());

        StepVerifier.create(service.createBusinessIncome(claimId, new BigDecimal("100000"), 2, 30,
                        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 1), UUID.randomUUID()))
                .assertNext(income -> {
                    assertEquals(BigDecimal.ZERO, income.getEstimatedLoss());
                    assertFalse(income.getCoverageConfirmed());
                }).verifyComplete();
        when(businessIncomeRepository.findByClaimId(claimId)).thenReturn(Mono.empty());
        StepVerifier.create(service.createBusinessIncome(claimId, new BigDecimal("100000"), 2, 30,
                        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 1), UUID.randomUUID())).expectNextCount(1).verifyComplete();

        BusinessIncome income = BusinessIncome.builder().claimId(claimId).waitingPeriodDays(2)
                .restorationPeriodDays(30).estimatedLoss(BigDecimal.ZERO).build();
        when(businessIncomeRepository.findByClaimId(claimId)).thenReturn(Mono.just(income));
        StepVerifier.create(service.updateBusinessIncome(claimId, new BigDecimal("200000"), null, 45,
                        null, null, UUID.randomUUID())).expectNext(income).verifyComplete();
        StepVerifier.create(service.updateBusinessIncome(claimId, new BigDecimal("300000"), 3, 60,
                        LocalDate.now(), LocalDate.now().plusDays(10), UUID.randomUUID())).expectNext(income).verifyComplete();
        StepVerifier.create(service.recordLoss(claimId, new BigDecimal("5000"), UUID.randomUUID())).expectNext(income).verifyComplete();
        StepVerifier.create(service.recordActualLoss(claimId, new BigDecimal("4500"), UUID.randomUUID())).expectNext(income).verifyComplete();
        StepVerifier.create(service.confirmCoverage(claimId, UUID.randomUUID())).assertNext(updated -> assertTrue(updated.getCoverageConfirmed())).verifyComplete();
        StepVerifier.create(service.calculateLoss(claimId, new BigDecimal("36500"), 10, UUID.randomUUID()))
                .assertNext(updated -> assertEquals(new BigDecimal("1000.00"), updated.getEstimatedLoss())).verifyComplete();
        income.setWaitingPeriodDays(2);
        income.setRestorationPeriodDays(null);
        StepVerifier.create(service.calculateLoss(claimId, new BigDecimal("36500"), 10, UUID.randomUUID()))
                .expectErrorMessage("Waiting period and restoration period must be set").verify();

        income.setWaitingPeriodDays(null);
        StepVerifier.create(service.calculateLoss(claimId, new BigDecimal("36500"), 10, UUID.randomUUID()))
                .expectErrorMessage("Waiting period and restoration period must be set").verify();
        when(businessIncomeRepository.findByClaimId(claimId)).thenReturn(Mono.empty());
        StepVerifier.create(service.getBusinessIncome(claimId)).expectError().verify();
        StepVerifier.create(service.updateBusinessIncome(claimId, null, null, null, null, null, null)).expectError().verify();
        when(claimRepository.findById(claimId)).thenReturn(Mono.empty());
        StepVerifier.create(service.createBusinessIncome(claimId, null, null, null, null, null, null))
                .expectError(IllegalArgumentException.class).verify();
    }

    @Test
    void financialsCoversInitializationUpdatesAndPaymentGuard() {
        ClaimFinancialsService service = new ClaimFinancialsService(financialsRepository, claimRepository, entityTemplate);
        UUID claimId = UUID.randomUUID();
        when(claimRepository.findById(claimId)).thenReturn(Mono.just(Claim.builder().claimId(claimId).build()));
        when(financialsRepository.findByClaimId(claimId)).thenReturn(Mono.empty());
        when(entityTemplate.insert(any(ClaimFinancials.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(entityTemplate.update(any(ClaimFinancials.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        StepVerifier.create(service.initializeFinancials(claimId, null, UUID.randomUUID()))
                .assertNext(f -> { assertEquals(BigDecimal.ZERO, f.getReserveAmount()); assertEquals(BigDecimal.ZERO, f.getOutstandingReserve()); }).verifyComplete();
        ClaimFinancials f = ClaimFinancials.builder().claimId(claimId).reserveAmount(new BigDecimal("1000"))
                .totalIncurred(new BigDecimal("1500")).paidAmount(new BigDecimal("200")).build();
        when(financialsRepository.findByClaimId(claimId)).thenReturn(Mono.just(f));
        StepVerifier.create(service.updateReserve(claimId, new BigDecimal("1200"), "increase", UUID.randomUUID())).expectNext(f).verifyComplete();
        StepVerifier.create(service.updateIncurred(claimId, new BigDecimal("2000"), UUID.randomUUID())).expectNext(f).verifyComplete();
        f.setTotalIncurred(new BigDecimal("2000")); f.setReserveAmount(new BigDecimal("1200")); f.setPaidAmount(new BigDecimal("200"));
        StepVerifier.create(service.recordPayment(claimId, new BigDecimal("300"), UUID.randomUUID())).expectNext(f).verifyComplete();
        StepVerifier.create(service.recordPayment(claimId, new BigDecimal("5000"), UUID.randomUUID()))
                .expectErrorMessage("Payment amount exceeds total incurred").verify();
        when(financialsRepository.findByClaimId(claimId)).thenReturn(Mono.empty());
        StepVerifier.create(service.getFinancials(claimId)).expectError().verify();
        when(financialsRepository.findByClaimId(claimId)).thenReturn(Mono.empty());
        StepVerifier.create(service.initializeFinancials(claimId, BigDecimal.ONE, UUID.randomUUID())).expectNextCount(1).verifyComplete();
        when(claimRepository.findById(claimId)).thenReturn(Mono.empty());
        StepVerifier.create(service.initializeFinancials(claimId, BigDecimal.ONE, UUID.randomUUID())).expectError(IllegalArgumentException.class).verify();
    }

    @Test
    void coverageDecisionCoversCreateExistingAndMissingPaths() {
        CoverageDecisionService service = new CoverageDecisionService(decisionRepository, claimRepository, financialsRepository, entityTemplate);
        UUID claimId = UUID.randomUUID();
        UUID decisionId = UUID.randomUUID();
        when(decisionRepository.findAllByClaimId(claimId)).thenReturn(Flux.empty());
        StepVerifier.create(service.getDecisions(claimId)).verifyComplete();
        when(decisionRepository.findById(decisionId)).thenReturn(Mono.empty());
        StepVerifier.create(service.getDecision(decisionId)).expectError().verify();
        when(claimRepository.findById(claimId)).thenReturn(Mono.just(Claim.builder().claimId(claimId).build()));
        when(decisionRepository.findByClaimIdAndCoverageCode(claimId, "PROP")).thenReturn(Mono.empty());
        when(entityTemplate.insert(any(CoverageDecision.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(entityTemplate.update(any(CoverageDecision.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        StepVerifier.create(service.createDecision(claimId, "PROP", "APPROVED", "covered", UUID.randomUUID()))
                .assertNext(d -> assertEquals("APPROVED", d.getDecision())).verifyComplete();
        CoverageDecision existing = CoverageDecision.builder().decisionId(decisionId).claimId(claimId).coverageCode("PROP").build();
        when(decisionRepository.findByClaimIdAndCoverageCode(claimId, "PROP")).thenReturn(Mono.just(existing));
        StepVerifier.create(service.createDecision(claimId, "PROP", "DENIED", "excluded", UUID.randomUUID()))
                .assertNext(d -> assertEquals("DENIED", d.getDecision())).verifyComplete();
        StepVerifier.create(service.getDecisionsByClaim(claimId)).verifyComplete();
        when(claimRepository.findById(claimId)).thenReturn(Mono.empty());
        StepVerifier.create(service.createDecision(claimId, "PROP", "DENIED", null, null)).expectError(IllegalArgumentException.class).verify();
    }

    @Test
    void paymentCoversQueriesCreationFinancialUpdateAndStatus() {
        PaymentService service = new PaymentService(paymentRepository, financialsRepository, claimRepository, entityTemplate);
        UUID claimId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        when(paymentRepository.findAllByClaimId(claimId)).thenReturn(Flux.just(Payment.builder().status("PENDING").build(), Payment.builder().status("COMPLETED").build()));
        StepVerifier.create(service.getPayments(claimId)).expectNextCount(2).verifyComplete();
        StepVerifier.create(service.getPendingPayments(claimId)).expectNextCount(1).verifyComplete();
        when(paymentRepository.findById(paymentId)).thenReturn(Mono.empty());
        StepVerifier.create(service.getPayment(paymentId)).expectError().verify();
        when(paymentRepository.findByPaymentReference("PAY-1")).thenReturn(Mono.empty());
        StepVerifier.create(service.getPaymentByReference("PAY-1")).expectError().verify();
        when(claimRepository.findById(claimId)).thenReturn(Mono.just(Claim.builder().claimId(claimId).build()));
        when(entityTemplate.insert(any(Payment.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        ClaimFinancials f = ClaimFinancials.builder().claimId(claimId).reserveAmount(new BigDecimal("5000")).paidAmount(BigDecimal.ZERO).build();
        when(financialsRepository.findByClaimId(claimId)).thenReturn(Mono.just(f));
        when(entityTemplate.update(any(ClaimFinancials.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        StepVerifier.create(service.createPayment(claimId, "PAY-1", new BigDecimal("500"), "INDEMNITY", "ACH", "REF", "note", UUID.randomUUID()))
                .assertNext(p -> assertEquals("PENDING", p.getStatus())).verifyComplete();
        Payment payment = Payment.builder().paymentId(paymentId).status("PENDING").build();
        when(paymentRepository.findById(paymentId)).thenReturn(Mono.just(payment));
        when(entityTemplate.update(any(Payment.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        StepVerifier.create(service.updatePaymentStatus(paymentId, "AUTHORIZED")).assertNext(p -> assertEquals("AUTHORIZED", p.getStatus())).verifyComplete();
        StepVerifier.create(service.completePayment(paymentId)).assertNext(p -> assertEquals("COMPLETED", p.getStatus())).verifyComplete();
        when(claimRepository.findById(claimId)).thenReturn(Mono.empty());
        StepVerifier.create(service.createPayment(claimId, "PAY-2", BigDecimal.ONE, null, null, null, null, null)).expectError(IllegalArgumentException.class).verify();
    }

    @Test
    void salvageAndSubrogationCoverCreateUpdatesRecoveryAndQueries() {
        SalvageService salvage = new SalvageService(salvageRepository, claimRepository, entityTemplate);
        SubrogationService subrogation = new SubrogationService(subrogationRepository, claimRepository, entityTemplate);
        UUID claimId = UUID.randomUUID();
        when(salvageRepository.findAllByClaimId(claimId)).thenReturn(Flux.empty());
        when(salvageRepository.findByClaimIdAndStatus(claimId, "PENDING")).thenReturn(Flux.empty());
        when(salvageRepository.findByClaimIdAndStatus(claimId, "SOLD")).thenReturn(Flux.empty());
        StepVerifier.create(salvage.getSalvages(claimId)).verifyComplete();
        StepVerifier.create(salvage.getPendingSalvages(claimId)).verifyComplete();
        StepVerifier.create(salvage.getSoldSalvages(claimId)).verifyComplete();
        UUID salvageId = UUID.randomUUID();
        when(salvageRepository.findById(salvageId)).thenReturn(Mono.empty());
        StepVerifier.create(salvage.getSalvage(salvageId)).expectError().verify();
        when(claimRepository.findById(claimId)).thenReturn(Mono.just(Claim.builder().claimId(claimId).build()));
        when(entityTemplate.insert(any(Salvage.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(entityTemplate.update(any(Salvage.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(entityTemplate.update(any(Subrogation.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        StepVerifier.create(salvage.createSalvage(claimId, "Equipment", null, null, UUID.randomUUID()))
                .assertNext(s -> { assertEquals(BigDecimal.ZERO, s.getEstimatedValue()); assertEquals("PENDING", s.getStatus()); }).verifyComplete();
        StepVerifier.create(salvage.createSalvage(claimId, "Equipment", BigDecimal.TEN, "READY", UUID.randomUUID()))
                .assertNext(s -> { assertEquals(BigDecimal.TEN, s.getEstimatedValue()); assertEquals("READY", s.getStatus()); }).verifyComplete();
        Salvage savedSalvage = Salvage.builder().salvageId(salvageId).build();
        when(salvageRepository.findById(salvageId)).thenReturn(Mono.just(savedSalvage));
        StepVerifier.create(salvage.updateSalvage(salvageId, "Updated", new BigDecimal("10"), "READY", "Buyer", LocalDate.now(), new BigDecimal("8"), UUID.randomUUID())).expectNext(savedSalvage).verifyComplete();
        StepVerifier.create(salvage.updateSalvage(salvageId, null, null, null, null, null, null, UUID.randomUUID())).expectNext(savedSalvage).verifyComplete();
        StepVerifier.create(salvage.recordSale(salvageId, "Buyer", LocalDate.now(), new BigDecimal("8"), UUID.randomUUID())).assertNext(s -> assertEquals("SOLD", s.getStatus())).verifyComplete();

        when(subrogationRepository.findAllByClaimId(claimId)).thenReturn(Flux.empty());
        when(subrogationRepository.findByClaimIdAndStatus(claimId, "OPEN")).thenReturn(Flux.empty());
        when(subrogationRepository.findByClaimIdAndStatus(claimId, "CLOSED")).thenReturn(Flux.empty());
        StepVerifier.create(subrogation.getSubrogations(claimId)).verifyComplete();
        StepVerifier.create(subrogation.getOpenSubrogations(claimId)).verifyComplete();
        StepVerifier.create(subrogation.getClosedSubrogations(claimId)).verifyComplete();
        UUID subrogationId = UUID.randomUUID();
        when(subrogationRepository.findById(subrogationId)).thenReturn(Mono.empty());
        StepVerifier.create(subrogation.getSubrogation(subrogationId)).expectError().verify();
        when(entityTemplate.insert(any(Subrogation.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        StepVerifier.create(subrogation.createSubrogation(claimId, "Third party", "Carrier", new BigDecimal("100"), "notes", UUID.randomUUID()))
                .assertNext(s -> assertEquals("OPEN", s.getStatus())).verifyComplete();
        Subrogation saved = Subrogation.builder().subrogationId(subrogationId).amountClaimed(new BigDecimal("100")).build();
        when(subrogationRepository.findById(subrogationId)).thenReturn(Mono.just(saved));
        StepVerifier.create(subrogation.updateSubrogation(subrogationId, "Updated", "New carrier", new BigDecimal("120"), "OPEN", "more", UUID.randomUUID())).expectNext(saved).verifyComplete();
        StepVerifier.create(subrogation.updateSubrogation(subrogationId, null, null, null, null, null, UUID.randomUUID())).expectNext(saved).verifyComplete();
        StepVerifier.create(subrogation.recordRecovery(subrogationId, new BigDecimal("50"), UUID.randomUUID())).assertNext(s -> assertEquals("PARTIAL", s.getStatus())).verifyComplete();
        StepVerifier.create(subrogation.recordRecovery(subrogationId, new BigDecimal("120"), UUID.randomUUID())).assertNext(s -> assertEquals("CLOSED", s.getStatus())).verifyComplete();
        StepVerifier.create(subrogation.closeSubrogation(subrogationId, UUID.randomUUID())).assertNext(s -> assertEquals("CLOSED", s.getStatus())).verifyComplete();
        when(claimRepository.findById(claimId)).thenReturn(Mono.empty());
        StepVerifier.create(salvage.createSalvage(claimId, "x", BigDecimal.ONE, "PENDING", null)).expectError(IllegalArgumentException.class).verify();
        StepVerifier.create(subrogation.createSubrogation(claimId, "x", "y", BigDecimal.ONE, null, null)).expectError(IllegalArgumentException.class).verify();
    }
}
