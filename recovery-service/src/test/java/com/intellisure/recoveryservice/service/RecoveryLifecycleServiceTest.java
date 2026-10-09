package com.intellisure.recoveryservice.service;

import com.intellisure.recoveryservice.client.VendorPartnerClient;
import com.intellisure.recoveryservice.dto.CreateRecoveryCaseRequest;
import com.intellisure.recoveryservice.dto.RecordRecoveryProgressRequest;
import com.intellisure.recoveryservice.dto.RecoveryCaseFilterRequest;
import com.intellisure.recoveryservice.dto.SelectRecoveryPathRequest;
import com.intellisure.recoveryservice.dto.UpdateRecoveryStatusRequest;
import com.intellisure.recoveryservice.dto.UpdateRecoveryCaseRequest;
import com.intellisure.recoveryservice.dto.CompleteRecoveryCaseRequest;
import com.intellisure.recoveryservice.dto.RecoveryEstimationRequest;
import com.intellisure.recoveryservice.dto.RecoveryEstimationResponse;
import com.intellisure.recoveryservice.entity.RecoveryCase;
import com.intellisure.recoveryservice.entity.RecoveryCaseStatus;
import com.intellisure.recoveryservice.entity.RecoveryPath;
import com.intellisure.recoveryservice.entity.RecoverySeverity;
import com.intellisure.recoveryservice.repository.RecoveryCaseRepository;
import com.intellisure.recoveryservice.security.SecurityActorService;
import org.junit.jupiter.api.DisplayName;
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
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecoveryLifecycleServiceTest")
class RecoveryLifecycleServiceTest {

    @Mock
    private RecoveryCaseRepository repository;

    @Mock
    private RecoveryEstimationService estimationService;

    @Mock
    private VendorPartnerClient vendorPartnerClient;

    @Mock
    private SecurityActorService securityActorService;

    @InjectMocks
    private RecoveryCaseService service;

    @BeforeEach
    void allowExistingLifecycleTestsToActAsStaff() {
        lenient().when(securityActorService.hasAnyRole(any(String[].class))).thenReturn(Mono.just(true));
        lenient().when(securityActorService.assertCustomerAccess(nullable(UUID.class))).thenReturn(Mono.empty());
        lenient().when(repository.findByClaimId(any(UUID.class))).thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("Initiating recovery starts at zero percent and default CUSTOMER_MANAGED path")
    void initiatingRecoveryStartsAtZeroPercentAndDoesNotDecideCoverage() {
        UUID claim = UUID.randomUUID(), customer = UUID.randomUUID();
        RecoveryCase saved = RecoveryCase.builder()
                .recoveryCaseId(UUID.randomUUID())
                .claimId(claim)
                .customerId(customer)
                .severity(RecoverySeverity.HIGH)
                .status(RecoveryCaseStatus.INITIATED)
                .recoveryPath(RecoveryPath.CUSTOMER_MANAGED)
                .recoveryObjective("Restore operations")
                .currentRestorePercent(BigDecimal.ZERO)
                .build();
        when(repository.save(any(RecoveryCase.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(service.createCase(new CreateRecoveryCaseRequest(claim, customer,
                        RecoverySeverity.HIGH, "Restore operations", null, null)))
                .assertNext(result -> {
                    assertEquals(RecoveryCaseStatus.INITIATED, result.status());
                    assertEquals(BigDecimal.ZERO, result.currentRestorePercent());
                    assertEquals(RecoveryPath.CUSTOMER_MANAGED, result.recoveryPath());
                }).verifyComplete();
        verify(repository).save(any(RecoveryCase.class));
    }

    @Test
    @DisplayName("Selecting NETWORK_VENDOR with vendorId dispatches vendor assignment and advances to PLANNING")
    void networkVendorWithVendorIdSelection() {
        UUID caseId = UUID.randomUUID();
        UUID claimId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID vendorId = UUID.randomUUID();

        RecoveryCase existingCase = RecoveryCase.builder()
                .recoveryCaseId(caseId)
                .claimId(claimId)
                .customerId(customerId)
                .status(RecoveryCaseStatus.INITIATED)
                .currentRestorePercent(BigDecimal.ZERO)
                .build();

        when(repository.findById(caseId)).thenReturn(Mono.just(existingCase));
        when(vendorPartnerClient.createVendorAssignment(eq(vendorId), eq(claimId), eq(caseId), any(), any()))
                .thenReturn(Mono.just(Map.of("assignmentId", UUID.randomUUID().toString())));
        when(repository.save(any(RecoveryCase.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        SelectRecoveryPathRequest request = new SelectRecoveryPathRequest(
                RecoveryPath.NETWORK_VENDOR, vendorId, "Network restoration", LocalDate.now().plusWeeks(2), "Customer chose network vendor");

        StepVerifier.create(service.selectRecoveryPath(caseId, request))
                .assertNext(response -> {
                    assertEquals(RecoveryPath.NETWORK_VENDOR, response.recoveryPath());
                    assertEquals(RecoveryCaseStatus.PLANNING, response.status());
                    assertEquals("Customer chose network vendor", response.recoveryNotes());
                })
                .verifyComplete();

        verify(vendorPartnerClient, times(1)).createVendorAssignment(eq(vendorId), eq(claimId), eq(caseId), any(), any());
        verify(repository).save(any(RecoveryCase.class));
    }

    @Test
    @DisplayName("Selecting NETWORK_VENDOR without vendorId does not blindly assign vendor")
    void networkVendorWithoutVendorIdDoesNotAssignVendor() {
        UUID caseId = UUID.randomUUID();
        RecoveryCase existingCase = RecoveryCase.builder()
                .recoveryCaseId(caseId)
                .claimId(UUID.randomUUID())
                .status(RecoveryCaseStatus.INITIATED)
                .build();

        when(repository.findById(caseId)).thenReturn(Mono.just(existingCase));
        when(repository.save(any(RecoveryCase.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        SelectRecoveryPathRequest request = new SelectRecoveryPathRequest(
                RecoveryPath.NETWORK_VENDOR, null, null, null, "Vendor selection pending");

        StepVerifier.create(service.selectRecoveryPath(caseId, request))
                .assertNext(response -> {
                    assertEquals(RecoveryPath.NETWORK_VENDOR, response.recoveryPath());
                    assertEquals(RecoveryCaseStatus.PLANNING, response.status());
                })
                .verifyComplete();

        verify(vendorPartnerClient, never()).createVendorAssignment(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Selecting CUSTOMER_VENDOR updates path to CUSTOMER_VENDOR and does NOT assign vendor")
    void customerVendorSelection() {
        UUID caseId = UUID.randomUUID();
        RecoveryCase existingCase = RecoveryCase.builder()
                .recoveryCaseId(caseId)
                .claimId(UUID.randomUUID())
                .status(RecoveryCaseStatus.INITIATED)
                .build();

        when(repository.findById(caseId)).thenReturn(Mono.just(existingCase));
        when(repository.save(any(RecoveryCase.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        SelectRecoveryPathRequest request = new SelectRecoveryPathRequest(
                RecoveryPath.CUSTOMER_VENDOR, null, null, null, "Using own vendor");

        StepVerifier.create(service.selectRecoveryPath(caseId, request))
                .assertNext(response -> {
                    assertEquals(RecoveryPath.CUSTOMER_VENDOR, response.recoveryPath());
                    assertEquals(RecoveryCaseStatus.PLANNING, response.status());
                    assertEquals("Using own vendor", response.recoveryNotes());
                })
                .verifyComplete();

        verify(vendorPartnerClient, never()).createVendorAssignment(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Selecting CUSTOMER_MANAGED updates path to CUSTOMER_MANAGED and does NOT assign vendor")
    void customerManagedSelection() {
        UUID caseId = UUID.randomUUID();
        RecoveryCase existingCase = RecoveryCase.builder()
                .recoveryCaseId(caseId)
                .claimId(UUID.randomUUID())
                .status(RecoveryCaseStatus.INITIATED)
                .build();

        when(repository.findById(caseId)).thenReturn(Mono.just(existingCase));
        when(repository.save(any(RecoveryCase.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        SelectRecoveryPathRequest request = new SelectRecoveryPathRequest(
                RecoveryPath.CUSTOMER_MANAGED, null, null, null, "Self managed restoration");

        StepVerifier.create(service.selectRecoveryPath(caseId, request))
                .assertNext(response -> {
                    assertEquals(RecoveryPath.CUSTOMER_MANAGED, response.recoveryPath());
                    assertEquals(RecoveryCaseStatus.PLANNING, response.status());
                    assertEquals("Self managed restoration", response.recoveryNotes());
                })
                .verifyComplete();

        verify(vendorPartnerClient, never()).createVendorAssignment(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Confirmation that customer-owned recovery never creates vendor assignment even if vendorId is passed")
    void customerOwnedRecoveryNeverCreatesVendorAssignmentEvenIfVendorIdProvided() {
        UUID caseId = UUID.randomUUID();
        UUID rogueVendorId = UUID.randomUUID();
        RecoveryCase existingCase = RecoveryCase.builder()
                .recoveryCaseId(caseId)
                .claimId(UUID.randomUUID())
                .status(RecoveryCaseStatus.INITIATED)
                .build();

        when(repository.findById(caseId)).thenReturn(Mono.just(existingCase));
        when(repository.save(any(RecoveryCase.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        // Test with CUSTOMER_VENDOR
        SelectRecoveryPathRequest requestCustomerVendor = new SelectRecoveryPathRequest(
                RecoveryPath.CUSTOMER_VENDOR, rogueVendorId, "Desc", LocalDate.now(), "Notes");

        StepVerifier.create(service.selectRecoveryPath(caseId, requestCustomerVendor))
                .assertNext(response -> assertEquals(RecoveryPath.CUSTOMER_VENDOR, response.recoveryPath()))
                .verifyComplete();

        // Test with CUSTOMER_MANAGED
        SelectRecoveryPathRequest requestCustomerManaged = new SelectRecoveryPathRequest(
                RecoveryPath.CUSTOMER_MANAGED, rogueVendorId, "Desc", LocalDate.now(), "Notes");

        StepVerifier.create(service.selectRecoveryPath(caseId, requestCustomerManaged))
                .assertNext(response -> assertEquals(RecoveryPath.CUSTOMER_MANAGED, response.recoveryPath()))
                .verifyComplete();

        // Confirm never called
        verify(vendorPartnerClient, never()).createVendorAssignment(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Invalid recovery path is rejected")
    void invalidRecoveryPathIsRejected() {
        UUID caseId = UUID.randomUUID();

        // Null request or null path
        StepVerifier.create(service.selectRecoveryPath(caseId, (SelectRecoveryPathRequest) null))
                .expectError(IllegalArgumentException.class)
                .verify();

        StepVerifier.create(service.selectRecoveryPath(caseId, new SelectRecoveryPathRequest(null, null, null, null, null)))
                .expectError(IllegalArgumentException.class)
                .verify();

        // Invalid string path
        StepVerifier.create(service.selectRecoveryPath(caseId, "NON_EXISTENT_PATH"))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    @DisplayName("Selecting path on COMPLETED or CANCELLED case is rejected")
    void selectingPathOnTerminalCaseIsRejected() {
        UUID caseId = UUID.randomUUID();
        RecoveryCase completedCase = RecoveryCase.builder()
                .recoveryCaseId(caseId)
                .status(RecoveryCaseStatus.COMPLETED)
                .build();

        when(repository.findById(caseId)).thenReturn(Mono.just(completedCase));

        StepVerifier.create(service.selectRecoveryPath(caseId, RecoveryPath.NETWORK_VENDOR))
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    @DisplayName("Invalid state transitions are rejected")
    void invalidStateTransitionsAreRejected() {
        // Direct transition from INITIATED to COMPLETED rejected
        assertThrows(IllegalStateException.class, () ->
                service.validateStatusTransition(RecoveryCaseStatus.INITIATED, RecoveryCaseStatus.COMPLETED));

        // Direct transition from INITIATED to BUSINESS_RESTORED rejected
        assertThrows(IllegalStateException.class, () ->
                service.validateStatusTransition(RecoveryCaseStatus.INITIATED, RecoveryCaseStatus.BUSINESS_RESTORED));

        // Transition from CANCELLED rejected
        assertThrows(IllegalStateException.class, () ->
                service.validateStatusTransition(RecoveryCaseStatus.CANCELLED, RecoveryCaseStatus.IN_PROGRESS));

        // Transition from COMPLETED rejected unless REOPENED
        assertThrows(IllegalStateException.class, () ->
                service.validateStatusTransition(RecoveryCaseStatus.COMPLETED, RecoveryCaseStatus.PLANNING));

        // Valid transitions succeed without error
        assertDoesNotThrow(() ->
                service.validateStatusTransition(RecoveryCaseStatus.INITIATED, RecoveryCaseStatus.PLANNING));
        assertDoesNotThrow(() ->
                service.validateStatusTransition(RecoveryCaseStatus.PLANNING, RecoveryCaseStatus.IN_PROGRESS));
        assertDoesNotThrow(() ->
                service.validateStatusTransition(RecoveryCaseStatus.IN_PROGRESS, RecoveryCaseStatus.BUSINESS_RESTORED));
        assertDoesNotThrow(() ->
                service.validateStatusTransition(RecoveryCaseStatus.COMPLETED, RecoveryCaseStatus.REOPENED));
    }

    @Test
    @DisplayName("updateStatus rejects invalid lifecycle transitions via reactive flow")
    void updateStatusRejectsInvalidTransitions() {
        UUID caseId = UUID.randomUUID();
        RecoveryCase initiatedCase = RecoveryCase.builder()
                .recoveryCaseId(caseId)
                .status(RecoveryCaseStatus.INITIATED)
                .build();

        when(repository.findById(caseId)).thenReturn(Mono.just(initiatedCase));

        StepVerifier.create(service.updateStatus(caseId, new UpdateRecoveryStatusRequest(RecoveryCaseStatus.COMPLETED)))
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    @DisplayName("Recording progress updates restore percent and status")
    void recordProgressUpdatesStatusAndRestorationDate() {
        UUID caseId = UUID.randomUUID();
        RecoveryCase existingCase = RecoveryCase.builder()
                .recoveryCaseId(caseId)
                .status(RecoveryCaseStatus.IN_PROGRESS)
                .currentRestorePercent(BigDecimal.valueOf(25))
                .build();

        when(repository.findById(caseId)).thenReturn(Mono.just(existingCase));
        when(repository.save(any(RecoveryCase.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(service.recordProgress(caseId, new RecordRecoveryProgressRequest(BigDecimal.valueOf(100), "Fully restored")))
                .assertNext(response -> {
                    assertEquals(RecoveryCaseStatus.BUSINESS_RESTORED, response.status());
                    assertEquals(BigDecimal.valueOf(100), response.currentRestorePercent());
                    assertNotNull(response.actualRestorationDate());
                    assertEquals("Fully restored", response.recoveryNotes());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Missing recovery case is reported")
    void missingRecoveryCaseIsReported() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Mono.empty());
        StepVerifier.create(service.getCase(id)).expectError(IllegalArgumentException.class).verify();
    }

    @Test
    @DisplayName("Listing recovery cases uses customer filter")
    void listingRecoveryCasesUsesCustomerFilter() {
        UUID customer = UUID.randomUUID();
        RecoveryCase recoveryCase = RecoveryCase.builder().recoveryCaseId(UUID.randomUUID())
                .customerId(customer).status(RecoveryCaseStatus.INITIATED).currentRestorePercent(BigDecimal.ZERO).build();
        when(repository.findByCustomerId(customer)).thenReturn(Flux.just(recoveryCase));

        StepVerifier.create(service.getCases(new RecoveryCaseFilterRequest(customer, null, null, null, null, null, 0, 20)))
                .assertNext(result -> assertEquals(customer, result.items().get(0).customerId()))
                .verifyComplete();
        verify(repository).findByCustomerId(customer);
        verify(repository, never()).findAll();
    }

    @Test
    @DisplayName("Listing recovery cases without customer filter reads all cases")
    void listingRecoveryCasesWithoutCustomerFilterReadsAllCases() {
        RecoveryCase recoveryCase = RecoveryCase.builder().recoveryCaseId(UUID.randomUUID())
                .status(RecoveryCaseStatus.INITIATED).currentRestorePercent(BigDecimal.ZERO).build();
        when(repository.findAll()).thenReturn(Flux.just(recoveryCase));

        StepVerifier.create(service.getCases(new RecoveryCaseFilterRequest(null, null, null, null, null, null, 0, 20)))
                .assertNext(result -> assertEquals(
                        RecoveryCaseStatus.INITIATED, result.items().get(0).status()))
                .verifyComplete();
        verify(repository).findAll();
        verify(repository, never()).findByCustomerId(any());
    }

    @Test
    void progressMovesCasesThroughInProgressPartialAndRestoredStates() {
        UUID id = UUID.randomUUID();
        RecoveryCase recoveryCase = baseCase(id, RecoveryCaseStatus.INITIATED);
        when(repository.findById(id)).thenReturn(Mono.just(recoveryCase));
        when(repository.save(any(RecoveryCase.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(service.recordProgress(id, new RecordRecoveryProgressRequest(BigDecimal.valueOf(10), "started")))
                .assertNext(r -> assertEquals(RecoveryCaseStatus.IN_PROGRESS, r.status())).verifyComplete();
        recoveryCase.setStatus(RecoveryCaseStatus.IN_PROGRESS);
        StepVerifier.create(service.recordProgress(id, new RecordRecoveryProgressRequest(BigDecimal.valueOf(50), "half")))
                .assertNext(r -> assertEquals(RecoveryCaseStatus.BUSINESS_PARTIALLY_RESTORED, r.status())).verifyComplete();
        recoveryCase.setStatus(RecoveryCaseStatus.BUSINESS_PARTIALLY_RESTORED);
        recoveryCase.setActualRestorationDate(LocalDate.now());
        StepVerifier.create(service.recordProgress(id, new RecordRecoveryProgressRequest(BigDecimal.valueOf(100), "done")))
                .assertNext(r -> assertEquals(RecoveryCaseStatus.BUSINESS_RESTORED, r.status())).verifyComplete();
    }

    @Test
    void updateCaseAppliesOnlyProvidedFieldsAndCompleteCaseAddsSummary() {
        UUID id = UUID.randomUUID();
        RecoveryCase recoveryCase = baseCase(id, RecoveryCaseStatus.PLANNING);
        when(repository.findById(id)).thenReturn(Mono.just(recoveryCase));
        when(repository.save(any(RecoveryCase.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(service.updateCase(id, new UpdateRecoveryCaseRequest(
                        RecoverySeverity.CRITICAL, "New objective", LocalDate.now().plusDays(10), BigDecimal.valueOf(20), UUID.randomUUID())))
                .assertNext(r -> {
                    assertEquals(RecoverySeverity.CRITICAL, r.severity());
                    assertEquals(BigDecimal.valueOf(20), r.currentRestorePercent());
                }).verifyComplete();

        StepVerifier.create(service.completeCase(id, new CompleteRecoveryCaseRequest("Restored operations", "SUCCESS")))
                .assertNext(r -> {
                    assertEquals(RecoveryCaseStatus.COMPLETED, r.status());
                    assertEquals(BigDecimal.valueOf(100), r.currentRestorePercent());
                    assertTrue(r.recoveryObjective().contains("COMPLETION"));
                }).verifyComplete();
    }

    @Test
    void statusUpdateCompletesAndRejectsInvalidRequests() {
        UUID id = UUID.randomUUID();
        RecoveryCase recoveryCase = baseCase(id, RecoveryCaseStatus.PLANNING);
        when(repository.findById(id)).thenReturn(Mono.just(recoveryCase));
        when(repository.save(any(RecoveryCase.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        StepVerifier.create(service.updateStatus(id, new UpdateRecoveryStatusRequest(RecoveryCaseStatus.BUSINESS_RESTORED)))
                .assertNext(r -> {
                    assertEquals(BigDecimal.valueOf(100), r.currentRestorePercent());
                    assertNotNull(r.actualRestorationDate());
                }).verifyComplete();

        StepVerifier.create(service.updateStatus(id, null)).expectError(IllegalArgumentException.class).verify();
        StepVerifier.create(service.recordProgress(id, null)).expectError(IllegalArgumentException.class).verify();
        assertThrows(IllegalArgumentException.class, () -> service.validateStatusTransition((RecoveryCaseStatus) null, RecoveryCaseStatus.PLANNING));
        assertThrows(IllegalArgumentException.class, () -> service.validateStatusTransition("bad", "PLANNING"));
    }

    @Test
    void stringAndEnumPathOverloadsAndEstimationDelegateCorrectly() {
        UUID id = UUID.randomUUID();
        RecoveryCase recoveryCase = baseCase(id, RecoveryCaseStatus.PLANNING);
        when(repository.findById(id)).thenReturn(Mono.just(recoveryCase));
        when(repository.save(any(RecoveryCase.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        StepVerifier.create(service.selectRecoveryPath(id, " customer_managed "))
                .assertNext(r -> assertEquals(RecoveryPath.CUSTOMER_MANAGED, r.recoveryPath())).verifyComplete();
        StepVerifier.create(service.selectRecoveryPath(id, (RecoveryPath) null)).expectError(IllegalArgumentException.class).verify();
        StepVerifier.create(service.selectRecoveryPath(id, " ")).expectError(IllegalArgumentException.class).verify();

        RecoveryEstimationResponse estimation = new RecoveryEstimationResponse(UUID.randomUUID(), UUID.randomUUID(),
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, "notes", java.time.LocalDateTime.now());
        when(estimationService.estimateRecoveryAsync(any())).thenReturn(java.util.concurrent.CompletableFuture.completedFuture(estimation));
        StepVerifier.create(service.estimateRecoveryAsync(new RecoveryEstimationRequest(UUID.randomUUID(), UUID.randomUUID(),
                        BigDecimal.TEN, 0.2, RecoverySeverity.LOW, null, null)))
                .expectNext(estimation).verifyComplete();
    }

    @Test
    void terminalCasesRejectProgressAndPathChange() {
        UUID id = UUID.randomUUID();
        for (RecoveryCaseStatus status : new RecoveryCaseStatus[] {RecoveryCaseStatus.COMPLETED, RecoveryCaseStatus.CANCELLED}) {
            RecoveryCase recoveryCase = baseCase(id, status);
            when(repository.findById(id)).thenReturn(Mono.just(recoveryCase));
            StepVerifier.create(service.recordProgress(id, new RecordRecoveryProgressRequest(BigDecimal.TEN, null)))
                    .expectError(IllegalStateException.class).verify();
            StepVerifier.create(service.selectRecoveryPath(id, RecoveryPath.CUSTOMER_MANAGED))
                    .expectError(IllegalStateException.class).verify();
        }
    }

    @Test
    void coversRemainingProgressStatusAndPathValidationBranches() {
        UUID id = UUID.randomUUID();
        RecoveryCase recoveryCase = baseCase(id, RecoveryCaseStatus.IN_PROGRESS);
        when(repository.findById(id)).thenReturn(Mono.just(recoveryCase));
        when(repository.save(any(RecoveryCase.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        StepVerifier.create(service.recordProgress(id, new RecordRecoveryProgressRequest(null, null)))
                .expectError(IllegalArgumentException.class).verify();
        StepVerifier.create(service.recordProgress(id, new RecordRecoveryProgressRequest(BigDecimal.valueOf(10), " ")))
                .assertNext(r -> assertEquals(RecoveryCaseStatus.IN_PROGRESS, r.status())).verifyComplete();
        recoveryCase.setStatus(RecoveryCaseStatus.ON_HOLD);
        StepVerifier.create(service.recordProgress(id, new RecordRecoveryProgressRequest(BigDecimal.valueOf(50), null)))
                .assertNext(r -> assertEquals(RecoveryCaseStatus.ON_HOLD, r.status())).verifyComplete();
        recoveryCase.setStatus(RecoveryCaseStatus.PLANNING);
        StepVerifier.create(service.recordProgress(id, new RecordRecoveryProgressRequest(BigDecimal.ZERO, null)))
                .assertNext(r -> assertEquals(RecoveryCaseStatus.PLANNING, r.status())).verifyComplete();
        StepVerifier.create(service.selectRecoveryPath(id, (String) null)).expectError(IllegalArgumentException.class).verify();

        assertDoesNotThrow(() -> service.validateStatusTransition(RecoveryCaseStatus.PLANNING, RecoveryCaseStatus.PLANNING));
        assertThrows(IllegalArgumentException.class, () -> service.validateStatusTransition(RecoveryCaseStatus.PLANNING, null));
        assertThrows(IllegalStateException.class, () -> service.validateStatusTransition(RecoveryCaseStatus.IN_PROGRESS, RecoveryCaseStatus.REOPENED));
        assertDoesNotThrow(() -> service.validateStatusTransition(" planning ", "in_progress"));
        assertThrows(IllegalArgumentException.class, () -> service.validateStatusTransition("planning", "bad"));
        assertThrows(IllegalArgumentException.class, () -> service.validateStatusTransition((String) null, "planning"));
    }

    @Test
    void coversStaffFilterPriorityAndDefaultPaginationBranches() {
        RecoveryCase recoveryCase = baseCase(UUID.randomUUID(), RecoveryCaseStatus.PLANNING);
        when(securityActorService.hasAnyRole(any(String[].class))).thenReturn(Mono.just(true));
        when(repository.findByOwnerId(any())).thenReturn(Flux.just(recoveryCase));
        when(repository.findByStatus(RecoveryCaseStatus.PLANNING)).thenReturn(Flux.just(recoveryCase));
        when(repository.findBySeverity(RecoverySeverity.MEDIUM)).thenReturn(Flux.just(recoveryCase));
        when(repository.findAll()).thenReturn(Flux.just(recoveryCase));
        UUID owner = UUID.randomUUID();
        StepVerifier.create(service.getCases(new RecoveryCaseFilterRequest(null, owner, null, null, null, null, null, null)))
                .assertNext(r -> { assertEquals(0, r.page()); assertEquals(20, r.size()); }).verifyComplete();
        StepVerifier.create(service.getCases(new RecoveryCaseFilterRequest(null, null, RecoveryCaseStatus.PLANNING, null, null, null, 0, 20))).expectNextCount(1).verifyComplete();
        StepVerifier.create(service.getCases(new RecoveryCaseFilterRequest(null, null, null, RecoverySeverity.MEDIUM, null, null, 0, 20))).expectNextCount(1).verifyComplete();
        StepVerifier.create(service.getCases(new RecoveryCaseFilterRequest(null, null, null, null, null, null, 0, 20))).expectNextCount(1).verifyComplete();
    }

    @Test
    void coversCompletionWithoutSummaryAndAlreadyRestoredDate() {
        UUID id = UUID.randomUUID();
        RecoveryCase recoveryCase = baseCase(id, RecoveryCaseStatus.PLANNING);
        recoveryCase.setRecoveryObjective(null);
        recoveryCase.setActualRestorationDate(LocalDate.now());
        when(repository.findById(id)).thenReturn(Mono.just(recoveryCase));
        when(repository.save(any(RecoveryCase.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        StepVerifier.create(service.completeCase(id, null)).assertNext(r -> assertEquals(RecoveryCaseStatus.COMPLETED, r.status())).verifyComplete();
        recoveryCase.setStatus(RecoveryCaseStatus.PLANNING);
        StepVerifier.create(service.completeCase(id, new CompleteRecoveryCaseRequest(null, null)))
                .assertNext(r -> assertEquals(RecoveryCaseStatus.COMPLETED, r.status())).verifyComplete();
    }

    @Test
    void coversNoOpUpdateFieldsAndNonCompletingStatusUpdate() {
        UUID id = UUID.randomUUID();
        RecoveryCase recoveryCase = baseCase(id, RecoveryCaseStatus.PLANNING);
        recoveryCase.setActualRestorationDate(LocalDate.now());
        when(repository.findById(id)).thenReturn(Mono.just(recoveryCase));
        when(repository.save(any(RecoveryCase.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        StepVerifier.create(service.updateCase(id, new UpdateRecoveryCaseRequest(null, null, null, null, null)))
                .assertNext(r -> assertEquals(RecoveryCaseStatus.PLANNING, r.status())).verifyComplete();
        StepVerifier.create(service.updateStatus(id, new UpdateRecoveryStatusRequest(RecoveryCaseStatus.IN_PROGRESS)))
                .assertNext(r -> assertEquals(RecoveryCaseStatus.IN_PROGRESS, r.status())).verifyComplete();
        recoveryCase.setStatus(RecoveryCaseStatus.PLANNING);
        StepVerifier.create(service.updateStatus(id, new UpdateRecoveryStatusRequest(RecoveryCaseStatus.BUSINESS_RESTORED)))
                .assertNext(r -> assertEquals(RecoveryCaseStatus.BUSINESS_RESTORED, r.status())).verifyComplete();
    }

    private RecoveryCase baseCase(UUID id, RecoveryCaseStatus status) {
        return RecoveryCase.builder().recoveryCaseId(id).claimId(UUID.randomUUID()).customerId(UUID.randomUUID())
                .severity(RecoverySeverity.MEDIUM).status(status).recoveryPath(RecoveryPath.CUSTOMER_MANAGED)
                .recoveryObjective("Restore operations").currentRestorePercent(BigDecimal.ZERO)
                .createdAt(java.time.LocalDateTime.now()).updatedAt(java.time.LocalDateTime.now()).isNew(false).build();
    }
}
