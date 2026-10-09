package com.intellisure.recoveryservice.service;

import com.intellisure.recoveryservice.client.VendorPartnerClient;
import com.intellisure.recoveryservice.dto.CreateRecoveryCaseRequest;
import com.intellisure.recoveryservice.entity.RecoveryCase;
import com.intellisure.recoveryservice.entity.RecoveryCaseStatus;
import com.intellisure.recoveryservice.entity.RecoverySeverity;
import com.intellisure.recoveryservice.exception.AccessDeniedBusinessException;
import com.intellisure.recoveryservice.repository.RecoveryCaseRepository;
import com.intellisure.recoveryservice.security.SecurityActorService;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecoveryServiceTest {
    @Mock RecoveryCaseRepository repository;
    @Mock RecoveryEstimationService estimationService;
    @Mock VendorPartnerClient vendorPartnerClient;
    @Mock SecurityActorService securityActorService;
    @InjectMocks RecoveryCaseService service;

    @BeforeEach
    void allowExistingServiceTestsToActAsStaff() {
        lenient().when(securityActorService.hasAnyRole(any(String[].class))).thenReturn(Mono.just(true));
        lenient().when(securityActorService.assertCustomerAccess(nullable(UUID.class))).thenReturn(Mono.empty());
        lenient().when(repository.findByClaimId(any(UUID.class))).thenReturn(Mono.empty());
    }

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
    void doesNotCreateCaseForUnauthorizedCustomer() {
        UUID claim = UUID.randomUUID(), customer = UUID.randomUUID();
        when(securityActorService.assertCustomerAccess(customer))
                .thenReturn(Mono.error(new AccessDeniedBusinessException(
                        "not authorized"
                )));

        StepVerifier.create(service.createCase(new CreateRecoveryCaseRequest(
                        claim,
                        customer,
                        RecoverySeverity.HIGH,
                        "restore",
                        null,
                        null
                )))
                .expectError(AccessDeniedBusinessException.class)
                .verify();

        verify(repository, never()).save(any(RecoveryCase.class));
    }

    @Test
    void returnsExistingCaseInsteadOfCreatingDuplicateForClaim() {
        UUID claim = UUID.randomUUID();
        UUID customer = UUID.randomUUID();
        RecoveryCase existing = RecoveryCase.builder()
                .recoveryCaseId(UUID.randomUUID())
                .claimId(claim)
                .customerId(customer)
                .severity(RecoverySeverity.MEDIUM)
                .status(RecoveryCaseStatus.PLANNING)
                .recoveryPath(com.intellisure.recoveryservice.entity.RecoveryPath.CUSTOMER_MANAGED)
                .currentRestorePercent(BigDecimal.ZERO)
                .build();
        when(repository.findByClaimId(claim)).thenReturn(Mono.just(existing));

        StepVerifier.create(service.createCase(new CreateRecoveryCaseRequest(
                        claim,
                        customer,
                        RecoverySeverity.HIGH,
                        "restore",
                        null,
                        null
                )))
                .assertNext(response -> org.junit.jupiter.api.Assertions.assertEquals(
                        existing.getRecoveryCaseId(), response.recoveryCaseId()))
                .verifyComplete();

        verify(repository, never()).save(any(RecoveryCase.class));
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
