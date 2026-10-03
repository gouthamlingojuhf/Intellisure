package com.intellisure.claimsservice.service;

import com.intellisure.claimsservice.dto.FileClaimRequest;
import com.intellisure.claimsservice.entity.Claim;
import com.intellisure.claimsservice.repository.ClaimRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimLifecycleServiceTest {
    @Mock ClaimRepository repository;
    @InjectMocks ClaimService service;

    @Test
    void filingClaimStartsFiledLifecycleAndPreservesEstimate() {
        UUID customer = UUID.randomUUID();
        UUID policy = UUID.randomUUID();
        Claim saved = Claim.builder().claimId(UUID.randomUUID()).policyId(policy).customerId(customer)
                .claimNumber("CLM-1").status("FILED").incidentDate(LocalDate.of(2026, 1, 2))
                .reportedDate(LocalDate.now()).description("Water damage")
                .estimatedLoss(new BigDecimal("5000")).build();
        when(repository.save(any(Claim.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(service.fileClaim(new FileClaimRequest(policy, LocalDate.of(2026, 1, 2),
                        "Water damage", new BigDecimal("5000")), customer))
                .assertNext(result -> {
                    org.junit.jupiter.api.Assertions.assertEquals("FILED", result.status());
                    org.junit.jupiter.api.Assertions.assertEquals(new BigDecimal("5000"), result.estimatedLoss());
                    org.junit.jupiter.api.Assertions.assertNull(result.payoutAmount());
                }).verifyComplete();
        verify(repository).save(any(Claim.class));
    }

    @Test
    void statusUpdateIsPersistedAndMissingClaimIsRejected() {
        UUID id = UUID.randomUUID();
        Claim claim = Claim.builder().claimId(id).status("FILED").build();
        when(repository.findById(id)).thenReturn(Mono.just(claim));
        when(repository.save(claim)).thenReturn(Mono.just(claim));
        StepVerifier.create(service.updateStatus(id, "UNDER_REVIEW"))
                .assertNext(result -> org.junit.jupiter.api.Assertions.assertEquals("UNDER_REVIEW", result.status()))
                .verifyComplete();

        UUID missing = UUID.randomUUID();
        when(repository.findById(missing)).thenReturn(Mono.empty());
        StepVerifier.create(service.getClaim(missing)).expectError(IllegalArgumentException.class).verify();
    }

    @Test
    void listingClaimsUsesCustomerFilterWhenProvided() {
        UUID customer = UUID.randomUUID();
        Claim claim = Claim.builder().claimId(UUID.randomUUID()).customerId(customer)
                .status("FILED").claimNumber("CLM-2").build();
        when(repository.findByCustomerId(customer)).thenReturn(reactor.core.publisher.Flux.just(claim));

        StepVerifier.create(service.getClaims(customer))
                .assertNext(result -> org.junit.jupiter.api.Assertions.assertEquals(customer, result.customerId()))
                .verifyComplete();
        verify(repository).findByCustomerId(customer);
        verify(repository, never()).findAll();
    }

    @Test
    void lifecycleAdvancesThroughAssignmentAssessmentReserveAndClosure() {
        UUID claimId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID adjusterId = UUID.randomUUID();
        UUID policyId = UUID.randomUUID();

        Claim claim = Claim.builder()
                .claimId(claimId)
                .policyId(policyId)
                .customerId(customerId)
                .claimNumber("CLM-2026-0001")
                .status("FNOL_RECEIVED")
                .estimatedLoss(new BigDecimal("10000"))
                .build();

        when(repository.findById(claimId)).thenReturn(Mono.just(claim));
        when(repository.save(any(Claim.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(service.assignAdjuster(claimId, adjusterId))
                .assertNext(result -> {
                    org.junit.jupiter.api.Assertions.assertEquals("OPEN", result.status());
                    org.junit.jupiter.api.Assertions.assertEquals(adjusterId, result.assignedAdjusterId());
                })
                .verifyComplete();

        StepVerifier.create(service.createAssessment(claimId, "FIRE", "Kitchen fire caused by electrical issue", true,
                        new BigDecimal("12000"), new BigDecimal("10000"), new BigDecimal("500"), new BigDecimal("9500"), adjusterId))
                .assertNext(result -> {
                    org.junit.jupiter.api.Assertions.assertEquals("COVERAGE_REVIEW", result.status());
                    org.junit.jupiter.api.Assertions.assertEquals("FIRE", result.incidentType());
                })
                .verifyComplete();

        StepVerifier.create(service.createReserve(claimId, new BigDecimal("9000"), "Initial reserve for fire loss", adjusterId))
                .assertNext(result -> org.junit.jupiter.api.Assertions.assertEquals("RESERVED", result.status()))
                .verifyComplete();

        StepVerifier.create(service.calculatePayout(claimId, new BigDecimal("9000"), new BigDecimal("500"), new BigDecimal("20000")))
                .assertNext(result -> org.junit.jupiter.api.Assertions.assertEquals(new BigDecimal("8500"), result.payableAmount()))
                .verifyComplete();

        StepVerifier.create(service.approveSettlement(claimId, adjusterId, new BigDecimal("8500")))
                .assertNext(result -> org.junit.jupiter.api.Assertions.assertEquals("APPROVED", result.status()))
                .verifyComplete();

        StepVerifier.create(service.recordPayment(claimId, "PMT-001"))
                .assertNext(result -> org.junit.jupiter.api.Assertions.assertEquals("SETTLED", result.status()))
                .verifyComplete();

        StepVerifier.create(service.closeClaim(claimId, "Repair completed and funds disbursed"))
                .assertNext(result -> org.junit.jupiter.api.Assertions.assertEquals("CLOSED", result.status()))
                .verifyComplete();
    }

    @Test
    void listingClaimsWithoutCustomerFilterReadsAllClaims() {
        Claim claim = Claim.builder().claimId(UUID.randomUUID()).status("FILED")
                .claimNumber("CLM-ALL").build();
        when(repository.findAll()).thenReturn(reactor.core.publisher.Flux.just(claim));

        StepVerifier.create(service.getClaims(null))
                .assertNext(result -> org.junit.jupiter.api.Assertions.assertEquals("CLM-ALL", result.claimNumber()))
                .verifyComplete();
        verify(repository).findAll();
        verify(repository, never()).findByCustomerId(any());
    }
}
