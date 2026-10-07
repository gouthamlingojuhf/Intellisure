package com.intellisure.claimsservice.service;

import com.intellisure.claimsservice.client.CustomerPartyAdjusterClient;
import com.intellisure.claimsservice.dto.FileClaimRequest;
import com.intellisure.claimsservice.entity.Claim;
import com.intellisure.claimsservice.repository.ClaimRepository;
import com.intellisure.claimsservice.security.SecurityActorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.*;
import reactor.test.StepVerifier;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {
    @Mock ClaimRepository repository;
    @Mock CustomerPartyAdjusterClient customerPartyAdjusterClient;
    @Mock SecurityActorService securityActorService;
    @InjectMocks ClaimService service;

    @Test
    void listsAllClaimsWhenCustomerIsNullAndFiltersByCustomerOtherwise() {
        Claim claim = Claim.builder().claimId(UUID.randomUUID()).customerId(UUID.randomUUID()).status("FILED").build();
        when(repository.findAll()).thenReturn(Flux.just(claim));
        when(repository.findByCustomerId(claim.getCustomerId())).thenReturn(Flux.just(claim));
        StepVerifier.create(service.getClaims(null)).expectNextCount(1).verifyComplete();
        StepVerifier.create(service.getClaims(claim.getCustomerId())).expectNextCount(1).verifyComplete();
        verify(repository).findAll(); verify(repository).findByCustomerId(claim.getCustomerId());
    }

    @Test
    void filesClaimWithFnolStatusAndMapsSavedEntity() {
        UUID policy = UUID.randomUUID(), customer = UUID.randomUUID();
        UUID adjuster = UUID.randomUUID();
        when(repository.save(any(Claim.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(repository.findAll()).thenReturn(Flux.empty());
        when(customerPartyAdjusterClient.findAvailableAdjusters()).thenReturn(Flux.just(adjuster));
        FileClaimRequest request = new FileClaimRequest(policy, LocalDate.of(2026, 1, 2), "loss",
                new BigDecimal("1000"));
        StepVerifier.create(service.fileClaim(request, customer)).assertNext(r -> {
            assertEquals(customer, r.customerId()); assertEquals("OPEN", r.status());
            assertEquals(adjuster, r.assignedAdjusterId());
            assertEquals(new BigDecimal("1000"), r.estimatedLoss());
        }).verifyComplete();
    }

    @Test
    void filesClaimSuccessfullyWhenAdjusterLookupFails() {
        UUID policy = UUID.randomUUID(), customer = UUID.randomUUID();
        when(repository.save(any(Claim.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(customerPartyAdjusterClient.findAvailableAdjusters())
                .thenReturn(Flux.error(new RuntimeException("customer-party-service connection refused")));

        FileClaimRequest request = new FileClaimRequest(policy, LocalDate.of(2026, 1, 2), "loss",
                new BigDecimal("1000"));

        StepVerifier.create(service.fileClaim(request, customer))
                .assertNext(response -> {
                    assertEquals(customer, response.customerId());
                    assertEquals("FNOL_RECEIVED", response.status());
                    assertNull(response.assignedAdjusterId());
                    assertEquals(new BigDecimal("1000"), response.estimatedLoss());
                })
                .verifyComplete();
    }

    @Test
    void filesClaimSuccessfullyWhenAdjusterLookupReturnsEmpty() {
        UUID policy = UUID.randomUUID(), customer = UUID.randomUUID();
        when(repository.save(any(Claim.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(customerPartyAdjusterClient.findAvailableAdjusters()).thenReturn(Flux.empty());

        FileClaimRequest request = new FileClaimRequest(policy, LocalDate.of(2026, 1, 2), "loss",
                new BigDecimal("1000"));

        StepVerifier.create(service.fileClaim(request, customer))
                .assertNext(response -> {
                    assertEquals(customer, response.customerId());
                    assertEquals("FNOL_RECEIVED", response.status());
                    assertNull(response.assignedAdjusterId());
                })
                .verifyComplete();
    }

    @Test
    void assignsAdjusterAutomaticallyToLeastBusyAvailableAdjuster() {
        UUID customer = UUID.randomUUID();
        UUID adjusterA = UUID.randomUUID();
        UUID adjusterB = UUID.randomUUID();
        UUID policy = UUID.randomUUID();

        Claim openA1 = Claim.builder().claimId(UUID.randomUUID()).assignedAdjusterId(adjusterA).status("OPEN").build();
        Claim openA2 = Claim.builder().claimId(UUID.randomUUID()).assignedAdjusterId(adjusterA).status("RESERVED").build();
        Claim openB1 = Claim.builder().claimId(UUID.randomUUID()).assignedAdjusterId(adjusterB).status("OPEN").build();

        when(repository.save(any(Claim.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(repository.findAll()).thenReturn(Flux.just(openA1, openA2, openB1));
        when(customerPartyAdjusterClient.findAvailableAdjusters()).thenReturn(Flux.just(adjusterA, adjusterB));

        FileClaimRequest request = new FileClaimRequest(policy, LocalDate.of(2026, 1, 2), "loss",
                new BigDecimal("1000"));

        StepVerifier.create(service.fileClaim(request, customer))
                .assertNext(response -> {
                    assertEquals(adjusterB, response.assignedAdjusterId());
                    assertEquals("OPEN", response.status());
                })
                .verifyComplete();
    }

    @Test
    void adminCanOverrideAutomaticAssignmentAndResignAdjuster() {
        UUID claimId = UUID.randomUUID();
        UUID autoAdjuster = UUID.randomUUID();
        UUID manualAdjuster = UUID.randomUUID();
        Claim claim = Claim.builder()
                .claimId(claimId)
                .status("OPEN")
                .assignedAdjusterId(autoAdjuster)
                .build();

        when(repository.findById(claimId)).thenReturn(Mono.just(claim));
        when(repository.save(any(Claim.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(service.assignAdjuster(claimId, manualAdjuster))
                .assertNext(result -> {
                    assertEquals(manualAdjuster, result.assignedAdjusterId());
                    assertEquals("OPEN", result.status());
                })
                .verifyComplete();

        StepVerifier.create(service.resignAdjuster(claimId))
                .assertNext(result -> {
                    assertEquals(null, result.assignedAdjusterId());
                    assertEquals("OPEN", result.status());
                })
                .verifyComplete();
    }

    @Test
    void missingClaimAndStatusUpdateReturnErrors() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Mono.empty());
        StepVerifier.create(service.getClaim(id)).expectErrorMessage("Claim not found: " + id).verify();
        StepVerifier.create(service.updateStatus(id, "SETTLED")).expectErrorMessage("Claim not found: " + id).verify();
    }
}
