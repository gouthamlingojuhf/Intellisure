package com.intellisure.recoveryservice.service;

import com.intellisure.recoveryservice.dto.InitiateRecoveryRequest;
import com.intellisure.recoveryservice.entity.RecoveryCase;
import com.intellisure.recoveryservice.repository.RecoveryCaseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.math.BigDecimal;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecoveryLifecycleServiceTest {
    @Mock RecoveryCaseRepository repository;
    @InjectMocks RecoveryService service;

    @Test
    void initiatingRecoveryStartsAtZeroPercentAndDoesNotDecideCoverage() {
        UUID claim = UUID.randomUUID(), customer = UUID.randomUUID();
        RecoveryCase saved = RecoveryCase.builder().recoveryCaseId(UUID.randomUUID()).claimId(claim)
                .customerId(customer).severity("HIGH").status("INITIATED")
                .recoveryObjective("Restore operations").currentRestorePercent(BigDecimal.ZERO).build();
        when(repository.save(any(RecoveryCase.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(service.initiateRecovery(new InitiateRecoveryRequest(claim, customer,
                        "HIGH", "Restore operations")))
                .assertNext(result -> {
                    org.junit.jupiter.api.Assertions.assertEquals("INITIATED", result.status());
                    org.junit.jupiter.api.Assertions.assertEquals(BigDecimal.ZERO, result.currentRestorePercent());
                }).verifyComplete();
        verify(repository).save(any(RecoveryCase.class));
    }

    @Test
    void missingRecoveryCaseIsReported() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Mono.empty());
        StepVerifier.create(service.getCase(id)).expectError(IllegalArgumentException.class).verify();
    }

    @Test
    void listingRecoveryCasesUsesCustomerFilter() {
        UUID customer = UUID.randomUUID();
        RecoveryCase recoveryCase = RecoveryCase.builder().recoveryCaseId(UUID.randomUUID())
                .customerId(customer).status("INITIATED").currentRestorePercent(BigDecimal.ZERO).build();
        when(repository.findByCustomerId(customer)).thenReturn(reactor.core.publisher.Flux.just(recoveryCase));

        StepVerifier.create(service.getCases(customer))
                .assertNext(result -> org.junit.jupiter.api.Assertions.assertEquals(customer, result.customerId()))
                .verifyComplete();
        verify(repository).findByCustomerId(customer);
        verify(repository, never()).findAll();
    }

    @Test
    void listingRecoveryCasesWithoutCustomerFilterReadsAllCases() {
        RecoveryCase recoveryCase = RecoveryCase.builder().recoveryCaseId(UUID.randomUUID())
                .status("INITIATED").currentRestorePercent(BigDecimal.ZERO).build();
        when(repository.findAll()).thenReturn(reactor.core.publisher.Flux.just(recoveryCase));

        StepVerifier.create(service.getCases(null))
                .assertNext(result -> org.junit.jupiter.api.Assertions.assertEquals(
                        "INITIATED", result.status()))
                .verifyComplete();
        verify(repository).findAll();
        verify(repository, never()).findByCustomerId(any());
    }
}
