package com.intellisure.claimsservice.service;

import com.intellisure.claimsservice.client.CustomerPartyAdjusterClient;
import com.intellisure.claimsservice.client.PolicyOwnershipClient;
import com.intellisure.claimsservice.dto.ClaimResponse;
import com.intellisure.claimsservice.entity.Claim;
import com.intellisure.claimsservice.exception.BusinessException;
import com.intellisure.claimsservice.repository.ClaimRepository;
import com.intellisure.claimsservice.security.SecurityActorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimServiceBranchCoverageTest {
    @Mock ClaimRepository repository;
    @Mock CustomerPartyAdjusterClient adjusterClient;
    @Mock PolicyOwnershipClient ownershipClient;
    @Mock SecurityActorService security;

    private ClaimService service() { return new ClaimService(repository, adjusterClient, ownershipClient, security); }

    @Test
    void callerListingCoversStaffCustomerAndStatusBranches() {
        Claim open = Claim.builder().claimId(UUID.randomUUID()).customerId(UUID.randomUUID()).status("OPEN").claimNumber("OPEN").build();
        Claim closed = Claim.builder().claimId(UUID.randomUUID()).customerId(open.getCustomerId()).status("CLOSED").claimNumber("CLOSED").build();
        when(security.hasAnyRole(any(String[].class))).thenReturn(Mono.just(true));
        when(repository.findByStatus("OPEN")).thenReturn(Flux.just(open));
        when(repository.findByCustomerId(open.getCustomerId())).thenReturn(Flux.just(open));
        StepVerifier.create(service().getClaimsForCaller(open.getCustomerId(), "open")).expectNextCount(1).verifyComplete();
        StepVerifier.create(service().getClaimsForCaller(open.getCustomerId(), null)).expectNextCount(1).verifyComplete();
        StepVerifier.create(service().getClaimsForCaller(open.getCustomerId(), " ")).expectNextCount(1).verifyComplete();
        when(security.hasAnyRole(any(String[].class))).thenReturn(Mono.just(false));
        when(security.currentCustomerId()).thenReturn(Mono.just(open.getCustomerId()));
        when(repository.findByCustomerId(open.getCustomerId())).thenReturn(Flux.just(open, closed));
        StepVerifier.create(service().getClaimsForCaller(UUID.randomUUID(), null)).expectNextCount(2).verifyComplete();
        StepVerifier.create(service().getClaimsForCaller(UUID.randomUUID(), "OPEN")).expectNextCount(1).verifyComplete();
        StepVerifier.create(service().getClaimsForCaller(UUID.randomUUID(), " ")).expectNextCount(2).verifyComplete();
        when(security.currentCustomerId()).thenReturn(Mono.empty());
        StepVerifier.create(service().getClaimsForCaller(null, null)).verifyComplete();
        when(repository.findByStatus("OPEN")).thenReturn(Flux.just(open));
        StepVerifier.create(service().getClaimsByStatus(" open ")).expectNextCount(1).verifyComplete();
        when(repository.findByStatus("OPEN")).thenReturn(Flux.just(open));
        StepVerifier.create(service().getClaimsByStatus(null)).expectNextCount(1).verifyComplete();
        StepVerifier.create(service().getClaimsByStatus(" ")).expectNextCount(1).verifyComplete();
    }

    @Test
    void claimAccessAndTransitionBranchesAreCovered() {
        UUID id = UUID.randomUUID();
        Claim claim = Claim.builder().claimId(id).customerId(UUID.randomUUID()).claimNumber("CLM-1").status("OPEN").build();
        when(repository.findById(id)).thenReturn(Mono.just(claim));
        when(repository.findByClaimNumber("CLM-1")).thenReturn(Mono.just(claim));
        when(security.assertClaimAccess(claim)).thenReturn(Mono.empty());
        StepVerifier.create(service().getClaim(id)).expectNextCount(1).verifyComplete();
        StepVerifier.create(service().getClaimByNumber("CLM-1")).expectNextCount(1).verifyComplete();
        assertDoesNotThrow(() -> service().validateStatusTransition(null, "OPEN"));
        assertDoesNotThrow(() -> service().validateStatusTransition("OPEN", null));
        assertDoesNotThrow(() -> service().validateStatusTransition("OPEN", "open"));
        assertDoesNotThrow(() -> service().validateStatusTransition("OPEN", "RESERVED"));
        assertDoesNotThrow(() -> service().validateStatusTransition("FNOL_RECEIVED", "OPEN"));
        assertDoesNotThrow(() -> service().validateStatusTransition("DENIED", "CLOSED"));
        assertDoesNotThrow(() -> service().validateStatusTransition("CLOSED", "CLOSED"));
        when(repository.save(claim)).thenReturn(Mono.just(claim));
        StepVerifier.create(service().updateStatus(id, "closed")).assertNext(r -> assertEquals("CLOSED", r.status())).verifyComplete();
        when(repository.findById(id)).thenReturn(Mono.empty());
        StepVerifier.create(service().assignAdjuster(id, UUID.randomUUID())).expectError(IllegalArgumentException.class).verify();
        StepVerifier.create(service().resignAdjuster(id)).expectError(IllegalArgumentException.class).verify();
    }

    @Test
    void assessmentPayoutDecisionAndClosureEdgeBranchesAreCovered() {
        UUID id = UUID.randomUUID();
        Claim claim = Claim.builder().claimId(id).status("OPEN").payoutAmount(new BigDecimal("7")).build();
        when(repository.findById(id)).thenReturn(Mono.just(claim));
        when(repository.save(any(Claim.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        ClaimService service = service();
        StepVerifier.create(service.createAssessment(id, "THEFT", "not covered", false, BigDecimal.TEN, null, null, null, UUID.randomUUID()))
                .assertNext(r -> { assertEquals("COVERAGE_REVIEW", r.status()); assertEquals(Boolean.FALSE, r.coverageConfirmed()); }).verifyComplete();
        StepVerifier.create(service.calculatePayout(id, BigDecimal.ONE, BigDecimal.TEN, BigDecimal.ZERO))
                .assertNext(r -> assertEquals(BigDecimal.ZERO, r.payoutAmount())).verifyComplete();
        StepVerifier.create(service.calculatePayout(id, new BigDecimal("100"), BigDecimal.ONE, new BigDecimal("25")))
                .assertNext(r -> assertEquals(new BigDecimal("25"), r.payoutAmount())).verifyComplete();
        StepVerifier.create(service.calculatePayout(id, null, null, null))
                .assertNext(r -> assertEquals(BigDecimal.ZERO, r.payoutAmount())).verifyComplete();
        StepVerifier.create(service.createReserve(id, null, "pending", UUID.randomUUID()))
                .assertNext(r -> assertEquals("RESERVED", r.status())).verifyComplete();
        StepVerifier.create(service.approveSettlement(id, UUID.randomUUID(), null)).expectNextCount(1).verifyComplete();
        StepVerifier.create(service.recordClaimDecision(id, " invalid ", null, UUID.randomUUID())).expectError(BusinessException.class).verify();
        StepVerifier.create(service.recordClaimDecision(id, " approved ", "covered", UUID.randomUUID()))
                .assertNext(r -> assertEquals("APPROVED", r.coverageDecision())).verifyComplete();
        claim.setCoverageDecision("DENIED"); claim.setStatus("DENIED");
        StepVerifier.create(service.closeClaim(id, null)).assertNext(r -> { assertEquals("CLOSED", r.status()); assertEquals("Closed", r.closureReason()); }).verifyComplete();
        claim.setCoverageDecision("APPROVED"); claim.setStatus("RECOVERY");
        StepVerifier.create(service.closeClaim(id, "recovery complete")).expectNextCount(1).verifyComplete();
        claim.setStatus("PAID");
        StepVerifier.create(service.closeClaim(id, "paid complete")).expectNextCount(1).verifyComplete();
        when(repository.findById(id)).thenReturn(Mono.empty());
        StepVerifier.create(service.recordClaimDecision(id, "APPROVED", null, null)).expectError(IllegalArgumentException.class).verify();
    }

    @Test
    void automaticAssignmentHandlesNullClientAndInactiveClaims() {
        UUID id = UUID.randomUUID();
        Claim claim = Claim.builder().claimId(id).status("FNOL_RECEIVED").build();
        ClaimService withoutClient = new ClaimService(repository, null, ownershipClient, security);
        when(repository.save(any(Claim.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        StepVerifier.create(withoutClient.fileClaim(new com.intellisure.claimsservice.dto.FileClaimRequest(UUID.randomUUID(), LocalDate.now(), "loss", null), UUID.randomUUID()))
                .assertNext(r -> assertEquals("FNOL_RECEIVED", r.status())).verifyComplete();
        when(repository.findAll()).thenReturn(Flux.just(
                Claim.builder().assignedAdjusterId(UUID.randomUUID()).status("CLOSED").build(),
                Claim.builder().assignedAdjusterId(UUID.randomUUID()).status(null).build()));
        when(adjusterClient.findAvailableAdjusters()).thenReturn(Flux.just(UUID.randomUUID()));
        when(repository.save(any(Claim.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        StepVerifier.create(service().fileClaim(new com.intellisure.claimsservice.dto.FileClaimRequest(UUID.randomUUID(), LocalDate.now(), "loss", null), UUID.randomUUID()))
                .assertNext(ClaimResponse::status).verifyComplete();
    }
}
