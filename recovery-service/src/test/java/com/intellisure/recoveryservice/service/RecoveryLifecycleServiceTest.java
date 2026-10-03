package com.intellisure.recoveryservice.service;

import com.intellisure.recoveryservice.dto.CreateRecoveryCaseRequest;
import com.intellisure.recoveryservice.dto.RecoveryCaseFilterRequest;
import com.intellisure.recoveryservice.entity.RecoveryCase;
import com.intellisure.recoveryservice.entity.RecoveryCaseStatus;
import com.intellisure.recoveryservice.entity.RecoverySeverity;
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
    @InjectMocks RecoveryCaseService service;

    @Test
    void initiatingRecoveryStartsAtZeroPercentAndDoesNotDecideCoverage() {
        UUID claim = UUID.randomUUID(), customer = UUID.randomUUID();
        RecoveryCase saved = RecoveryCase.builder().recoveryCaseId(UUID.randomUUID()).claimId(claim)
                .customerId(customer).severity(RecoverySeverity.HIGH).status(RecoveryCaseStatus.INITIATED)
                .recoveryObjective("Restore operations").currentRestorePercent(BigDecimal.ZERO).build();
        when(repository.save(any(RecoveryCase.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(service.createCase(new CreateRecoveryCaseRequest(claim, customer,
                        RecoverySeverity.HIGH, "Restore operations", null, null)))
                .assertNext(result -> {
                    org.junit.jupiter.api.Assertions.assertEquals(RecoveryCaseStatus.INITIATED, result.status());
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
                .customerId(customer).status(RecoveryCaseStatus.INITIATED).currentRestorePercent(BigDecimal.ZERO).build();
        when(repository.findByCustomerId(customer)).thenReturn(reactor.core.publisher.Flux.just(recoveryCase));

        StepVerifier.create(service.getCases(new RecoveryCaseFilterRequest(customer, null, null, null, null, null, 0, 20)))
                .assertNext(result -> org.junit.jupiter.api.Assertions.assertEquals(customer, result.items().get(0).customerId()))
                .verifyComplete();
        verify(repository).findByCustomerId(customer);
        verify(repository, never()).findAll();
    }

    @Test
    void listingRecoveryCasesWithoutCustomerFilterReadsAllCases() {
        RecoveryCase recoveryCase = RecoveryCase.builder().recoveryCaseId(UUID.randomUUID())
                .status(RecoveryCaseStatus.INITIATED).currentRestorePercent(BigDecimal.ZERO).build();
        when(repository.findAll()).thenReturn(reactor.core.publisher.Flux.just(recoveryCase));

        StepVerifier.create(service.getCases(new RecoveryCaseFilterRequest(null, null, null, null, null, null, 0, 20)))
                .assertNext(result -> org.junit.jupiter.api.Assertions.assertEquals(
                        RecoveryCaseStatus.INITIATED, result.items().get(0).status()))
                .verifyComplete();
        verify(repository).findAll();
        verify(repository, never()).findByCustomerId(any());
    }
}