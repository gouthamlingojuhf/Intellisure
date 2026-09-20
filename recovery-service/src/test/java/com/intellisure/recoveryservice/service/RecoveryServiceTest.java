package com.intellisure.recoveryservice.service;

import com.intellisure.recoveryservice.dto.InitiateRecoveryRequest;
import com.intellisure.recoveryservice.entity.RecoveryCase;
import com.intellisure.recoveryservice.repository.RecoveryCaseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.*;
import reactor.test.StepVerifier;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecoveryServiceTest {
    @Mock RecoveryCaseRepository repository;
    @InjectMocks RecoveryService service;

    @Test
    void initiatesCaseWithZeroProgress() {
        UUID claim = UUID.randomUUID(), customer = UUID.randomUUID();
        when(repository.save(any(RecoveryCase.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        StepVerifier.create(service.initiateRecovery(new InitiateRecoveryRequest(claim, customer, "HIGH", "restore")))
                .assertNext(r -> { assertEquals("INITIATED", r.status()); assertEquals(java.math.BigDecimal.ZERO, r.currentRestorePercent()); })
                .verifyComplete();
    }

    @Test
    void filtersCasesAndReportsMissingCase() {
        UUID customer = UUID.randomUUID(), id = UUID.randomUUID();
        when(repository.findByCustomerId(customer)).thenReturn(Flux.just(RecoveryCase.builder()
                .recoveryCaseId(id).customerId(customer).status("INITIATED").build()));
        when(repository.findById(id)).thenReturn(Mono.empty());
        StepVerifier.create(service.getCases(customer)).expectNextCount(1).verifyComplete();
        StepVerifier.create(service.getCase(id)).expectErrorMessage("Recovery case not found: " + id).verify();
    }
}
