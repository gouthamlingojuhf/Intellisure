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
}
