package com.intellisure.recoveryservice.service;

import com.intellisure.recoveryservice.client.VendorPartnerClient;
import com.intellisure.recoveryservice.dto.CreateRecoveryCaseRequest;
import com.intellisure.recoveryservice.entity.RecoveryCase;
import com.intellisure.recoveryservice.entity.RecoveryCaseStatus;
import com.intellisure.recoveryservice.entity.RecoverySeverity;
import com.intellisure.recoveryservice.repository.RecoveryCaseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.math.BigDecimal;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecoveryServiceTest {
    @Mock RecoveryCaseRepository repository;
    @Mock RecoveryEstimationService estimationService;
    @Mock VendorPartnerClient vendorPartnerClient;
    @InjectMocks RecoveryCaseService service;

    @Test
    void initiatesCaseWithZeroProgress() {
        UUID claim = UUID.randomUUID(), customer = UUID.randomUUID();
        when(repository.save(any(RecoveryCase.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        StepVerifier.create(service.createCase(new CreateRecoveryCaseRequest(claim, customer, RecoverySeverity.HIGH, "restore", null, null)))
                .assertNext(r -> { 
                    org.junit.jupiter.api.Assertions.assertEquals(RecoveryCaseStatus.INITIATED, r.status()); 
                    org.junit.jupiter.api.Assertions.assertEquals(BigDecimal.ZERO, r.currentRestorePercent()); 
                })
                .verifyComplete();
    }

    @Test
    void filtersCasesAndReportsMissingCase() {
        UUID customer = UUID.randomUUID(), id = UUID.randomUUID();
        when(repository.findByCustomerId(customer)).thenReturn(Flux.just(RecoveryCase.builder()
                .recoveryCaseId(id).customerId(customer).status(RecoveryCaseStatus.INITIATED).build()));
        when(repository.findById(id)).thenReturn(Mono.empty());
        StepVerifier.create(service.getCases(new com.intellisure.recoveryservice.dto.RecoveryCaseFilterRequest(customer, null, null, null, null, null, 0, 20))).expectNextCount(1).verifyComplete();
        StepVerifier.create(service.getCase(id)).expectErrorMessage("Recovery case not found: " + id).verify();
    }
}