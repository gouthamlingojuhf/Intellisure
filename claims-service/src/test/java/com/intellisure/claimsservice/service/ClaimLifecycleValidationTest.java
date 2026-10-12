package com.intellisure.claimsservice.service;

import com.intellisure.claimsservice.entity.Claim;
import com.intellisure.claimsservice.exception.BusinessException;
import com.intellisure.claimsservice.repository.ClaimRepository;
import com.intellisure.claimsservice.security.SecurityActorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClaimLifecycleValidationTest")
class ClaimLifecycleValidationTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private SecurityActorService securityActorService;

    @InjectMocks
    private ClaimService claimService;

    @Test
    @DisplayName("Direct transition from FNOL to SETTLED is rejected")
    void directTransitionFromFnolToSettledRejected() {
        assertThrows(BusinessException.class, () ->
                claimService.validateStatusTransition("FNOL_RECEIVED", "SETTLED"));
    }

    @Test
    @DisplayName("Transition from CLOSED status is rejected")
    void transitionFromClosedRejected() {
        assertThrows(BusinessException.class, () ->
                claimService.validateStatusTransition("CLOSED", "OPEN"));
    }

    @Test
    @DisplayName("DENIED claim can only transition to CLOSED")
    void deniedClaimCanOnlyTransitionToClosed() {
        assertThrows(BusinessException.class, () ->
                claimService.validateStatusTransition("DENIED", "RESERVED"));
        claimService.validateStatusTransition("DENIED", "CLOSED");
    }

    @Test
    @DisplayName("Closing claim without a decision is rejected")
    void closingClaimWithoutDecisionRejected() {
        UUID claimId = UUID.randomUUID();
        Claim claim = Claim.builder()
                .claimId(claimId)
                .status("FNOL_RECEIVED")
                .coverageDecision(null)
                .build();

        when(claimRepository.findById(claimId)).thenReturn(Mono.just(claim));

        StepVerifier.create(claimService.closeClaim(claimId, "Completed"))
                .expectError(BusinessException.class)
                .verify();
    }

    @Test
    @DisplayName("Closing approved claim without settlement/payment/recovery is rejected")
    void closingApprovedClaimWithoutSettlementRejected() {
        UUID claimId = UUID.randomUUID();
        Claim claim = Claim.builder()
                .claimId(claimId)
                .status("APPROVED")
                .coverageDecision("APPROVED")
                .build();

        when(claimRepository.findById(claimId)).thenReturn(Mono.just(claim));

        StepVerifier.create(claimService.closeClaim(claimId, "Done"))
                .expectError(BusinessException.class)
                .verify();
    }

    @Test
    @DisplayName("Closing settled/paid approved claim succeeds")
    void closingSettledClaimSucceeds() {
        UUID claimId = UUID.randomUUID();
        Claim claim = Claim.builder()
                .claimId(claimId)
                .status("SETTLED")
                .coverageDecision("APPROVED")
                .build();

        when(claimRepository.findById(claimId)).thenReturn(Mono.just(claim));
        when(claimRepository.save(any(Claim.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(claimService.closeClaim(claimId, "Settled and closed"))
                .assertNext(res -> {
                    org.junit.jupiter.api.Assertions.assertEquals("CLOSED", res.status());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Settlement > $10,000 rejected when caller is not Claims Manager")
    void settlementAboveThresholdRejectedForAdjuster() {
        UUID claimId = UUID.randomUUID();
        Claim claim = Claim.builder()
                .claimId(claimId)
                .status("UNDER_INVESTIGATION")
                .payoutAmount(new java.math.BigDecimal("15000.00"))
                .build();

        when(claimRepository.findById(claimId)).thenReturn(Mono.just(claim));
        when(securityActorService.hasAnyRole("CLAIMS_MANAGER", "ADMIN", "SYSTEM_ADMINISTRATOR"))
                .thenReturn(Mono.just(false));

        StepVerifier.create(claimService.approveSettlement(claimId, UUID.randomUUID(), new java.math.BigDecimal("15000.00")))
                .expectError(com.intellisure.claimsservice.exception.AccessDeniedBusinessException.class)
                .verify();
    }

    @Test
    @DisplayName("Settlement > $10,000 approved when caller has Claims Manager role")
    void settlementAboveThresholdApprovedForManager() {
        UUID claimId = UUID.randomUUID();
        Claim claim = Claim.builder()
                .claimId(claimId)
                .status("UNDER_INVESTIGATION")
                .payoutAmount(new java.math.BigDecimal("15000.00"))
                .build();

        when(claimRepository.findById(claimId)).thenReturn(Mono.just(claim));
        when(securityActorService.hasAnyRole("CLAIMS_MANAGER", "ADMIN", "SYSTEM_ADMINISTRATOR"))
                .thenReturn(Mono.just(true));
        when(claimRepository.save(any(Claim.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(claimService.approveSettlement(claimId, UUID.randomUUID(), new java.math.BigDecimal("15000.00")))
                .assertNext(res -> {
                    org.junit.jupiter.api.Assertions.assertEquals("APPROVED", res.status());
                    org.junit.jupiter.api.Assertions.assertEquals(new java.math.BigDecimal("15000.00"), res.payoutAmount());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Settlement <= $10,000 approved by adjuster without manager role")
    void settlementBelowThresholdApprovedForAdjuster() {
        UUID claimId = UUID.randomUUID();
        Claim claim = Claim.builder()
                .claimId(claimId)
                .status("UNDER_INVESTIGATION")
                .payoutAmount(new java.math.BigDecimal("5000.00"))
                .build();

        when(claimRepository.findById(claimId)).thenReturn(Mono.just(claim));
        when(claimRepository.save(any(Claim.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(claimService.approveSettlement(claimId, UUID.randomUUID(), new java.math.BigDecimal("5000.00")))
                .assertNext(res -> {
                    org.junit.jupiter.api.Assertions.assertEquals("APPROVED", res.status());
                    org.junit.jupiter.api.Assertions.assertEquals(new java.math.BigDecimal("5000.00"), res.payoutAmount());
                })
                .verifyComplete();
    }
}
