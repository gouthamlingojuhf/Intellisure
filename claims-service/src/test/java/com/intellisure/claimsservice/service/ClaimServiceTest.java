package com.intellisure.claimsservice.service;

import com.intellisure.claimsservice.dto.FileClaimRequest;
import com.intellisure.claimsservice.entity.Claim;
import com.intellisure.claimsservice.repository.ClaimRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {
    @Mock ClaimRepository repository;
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
    void filesClaimWithFiledStatusAndMapsSavedEntity() {
        UUID policy = UUID.randomUUID(), customer = UUID.randomUUID();
        when(repository.save(any(Claim.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        FileClaimRequest request = new FileClaimRequest(policy, LocalDate.of(2026, 1, 2), "loss",
                new BigDecimal("1000"));
        StepVerifier.create(service.fileClaim(request, customer)).assertNext(r -> {
            assertEquals(customer, r.customerId()); assertEquals("FILED", r.status());
            assertEquals(new BigDecimal("1000"), r.estimatedLoss());
        }).verifyComplete();
    }

    @Test
    void missingClaimAndStatusUpdateReturnErrors() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Mono.empty());
        StepVerifier.create(service.getClaim(id)).expectErrorMessage("Claim not found: " + id).verify();
        StepVerifier.create(service.updateStatus(id, "SETTLED")).expectErrorMessage("Claim not found: " + id).verify();
    }
}
