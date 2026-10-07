package com.intellisure.vendorpartnerservice.service;

import com.intellisure.vendorpartnerservice.dto.AcceptAssignmentRequest;
import com.intellisure.vendorpartnerservice.dto.CreateVendorAssignmentRequest;
import com.intellisure.vendorpartnerservice.dto.DeclineAssignmentRequest;
import com.intellisure.vendorpartnerservice.dto.UpdateAssignmentStatusRequest;
import com.intellisure.vendorpartnerservice.dto.VendorAssignmentFilterRequest;
import com.intellisure.vendorpartnerservice.entity.AssignmentStatus;
import com.intellisure.vendorpartnerservice.entity.AssignmentType;
import com.intellisure.vendorpartnerservice.entity.Vendor;
import com.intellisure.vendorpartnerservice.entity.VendorActiveStatus;
import com.intellisure.vendorpartnerservice.entity.VendorAssignment;
import com.intellisure.vendorpartnerservice.entity.VendorType;
import com.intellisure.vendorpartnerservice.entity.VendorVerificationStatus;
import com.intellisure.vendorpartnerservice.repository.VendorAssignmentRepository;
import com.intellisure.vendorpartnerservice.repository.VendorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VendorAssignmentServiceTest")
class VendorAssignmentServiceTest {

    @Mock
    private VendorRepository vendorRepository;

    @Mock
    private VendorAssignmentRepository assignmentRepository;

    @InjectMocks
    private VendorAssignmentService service;

    private Vendor createVerifiedActiveVendor(UUID vendorId) {
        return Vendor.builder()
                .vendorId(vendorId)
                .legalName("Pro Restorations Inc")
                .displayName("Pro Restorations")
                .vendorType(VendorType.PROPERTY_RESTORATION)
                .serviceTypes(List.of("RESTORATION", "CLEANUP"))
                .capabilities(List.of("WATER_MITIGATION", "FIRE_RESTORE"))
                .serviceAreas(List.of("US-NE", "US-NY"))
                .contactName("John Lead")
                .contactPhone("555-0199")
                .contactEmail("dispatch@prorestore.com")
                .verificationStatus(VendorVerificationStatus.VERIFIED)
                .activeStatus(VendorActiveStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("Network vendor assignment succeeds for verified and active vendor")
    void networkVendorAssignmentSucceeds() {
        UUID vendorId = UUID.randomUUID();
        UUID claimId = UUID.randomUUID();
        UUID recoveryCaseId = UUID.randomUUID();
        Vendor vendor = createVerifiedActiveVendor(vendorId);

        when(vendorRepository.findById(vendorId)).thenReturn(Mono.just(vendor));
        when(assignmentRepository.save(any(VendorAssignment.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        CreateVendorAssignmentRequest request = new CreateVendorAssignmentRequest(
                vendorId, "RESTORATION", claimId, recoveryCaseId, "NETWORK_VENDOR",
                "Full restoration of premises", LocalDate.now().plusWeeks(2), "HIGH"
        );

        StepVerifier.create(service.createAssignment(request))
                .assertNext(response -> {
                    assertEquals(vendorId, response.vendorId());
                    assertEquals("RESTORATION", response.assignmentType());
                    assertEquals(claimId, response.claimId());
                    assertEquals(recoveryCaseId, response.recoveryCaseId());
                    assertEquals("DISPATCHED", response.status());
                    assertEquals("HIGH", response.priority());
                })
                .verifyComplete();

        verify(assignmentRepository).save(any(VendorAssignment.class));
    }

    @Test
    @DisplayName("Invalid vendor rejected when vendor does not exist in repository")
    void invalidVendorRejected() {
        UUID nonExistentVendorId = UUID.randomUUID();
        when(vendorRepository.findById(nonExistentVendorId)).thenReturn(Mono.empty());

        CreateVendorAssignmentRequest request = new CreateVendorAssignmentRequest(
                nonExistentVendorId, "RESTORATION", UUID.randomUUID(), UUID.randomUUID(),
                "Task", LocalDate.now().plusDays(5), "MEDIUM"
        );

        StepVerifier.create(service.createAssignment(request))
                .expectError(IllegalArgumentException.class)
                .verify();

        verify(assignmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Inactive vendor rejected even if verified")
    void inactiveVendorRejected() {
        UUID vendorId = UUID.randomUUID();
        Vendor inactiveVendor = Vendor.builder()
                .vendorId(vendorId)
                .legalName("Inactive Restorations")
                .displayName("Inactive Restorations")
                .verificationStatus(VendorVerificationStatus.VERIFIED)
                .activeStatus(VendorActiveStatus.INACTIVE)
                .build();

        when(vendorRepository.findById(vendorId)).thenReturn(Mono.just(inactiveVendor));

        CreateVendorAssignmentRequest request = new CreateVendorAssignmentRequest(
                vendorId, "RESTORATION", UUID.randomUUID(), UUID.randomUUID(),
                "Task", LocalDate.now().plusDays(5), "MEDIUM"
        );

        StepVerifier.create(service.createAssignment(request))
                .expectError(IllegalStateException.class)
                .verify();

        verify(assignmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Unverified vendor rejected even if active")
    void unverifiedVendorRejected() {
        UUID vendorId = UUID.randomUUID();
        Vendor unverifiedVendor = Vendor.builder()
                .vendorId(vendorId)
                .legalName("Unverified Restorations")
                .displayName("Unverified Restorations")
                .verificationStatus(VendorVerificationStatus.PENDING)
                .activeStatus(VendorActiveStatus.ACTIVE)
                .build();

        when(vendorRepository.findById(vendorId)).thenReturn(Mono.just(unverifiedVendor));

        CreateVendorAssignmentRequest request = new CreateVendorAssignmentRequest(
                vendorId, "RESTORATION", UUID.randomUUID(), UUID.randomUUID(),
                "Task", LocalDate.now().plusDays(5), "MEDIUM"
        );

        StepVerifier.create(service.createAssignment(request))
                .expectError(IllegalStateException.class)
                .verify();

        verify(assignmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Customer-owned recovery (CUSTOMER_VENDOR or CUSTOMER_MANAGED) cannot create assignment")
    void customerOwnedRecoveryCannotCreateAssignment() {
        UUID vendorId = UUID.randomUUID();

        // 1. CUSTOMER_VENDOR path
        CreateVendorAssignmentRequest requestCustomerVendor = new CreateVendorAssignmentRequest(
                vendorId, "RESTORATION", UUID.randomUUID(), UUID.randomUUID(), "CUSTOMER_VENDOR",
                "Task", LocalDate.now().plusDays(5), "MEDIUM"
        );

        StepVerifier.create(service.createAssignment(requestCustomerVendor))
                .expectError(IllegalStateException.class)
                .verify();

        // 2. CUSTOMER_MANAGED path
        CreateVendorAssignmentRequest requestCustomerManaged = new CreateVendorAssignmentRequest(
                vendorId, "RESTORATION", UUID.randomUUID(), UUID.randomUUID(), "CUSTOMER_MANAGED",
                "Task", LocalDate.now().plusDays(5), "MEDIUM"
        );

        StepVerifier.create(service.createAssignment(requestCustomerManaged))
                .expectError(IllegalStateException.class)
                .verify();

        verify(vendorRepository, never()).findById(any(UUID.class));
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Vendor accepts assignment when dispatched")
    void vendorAcceptsAssignment() {
        UUID assignmentId = UUID.randomUUID();
        VendorAssignment dispatched = VendorAssignment.builder()
                .assignmentId(assignmentId)
                .vendorId(UUID.randomUUID())
                .assignmentType(AssignmentType.RESTORATION)
                .status(AssignmentStatus.DISPATCHED)
                .taskDescription("Task")
                .build();

        when(assignmentRepository.findById(assignmentId)).thenReturn(Mono.just(dispatched));
        when(assignmentRepository.save(any(VendorAssignment.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        AcceptAssignmentRequest request = new AcceptAssignmentRequest("Accepting repair task", LocalDate.now());

        StepVerifier.create(service.acceptAssignment(assignmentId, request))
                .assertNext(res -> {
                    assertEquals("ACCEPTED", res.status());
                    assertNotNull(res.acceptedAt());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Vendor starts work transitioning ACCEPTED to IN_PROGRESS")
    void vendorStartsWork() {
        UUID assignmentId = UUID.randomUUID();
        VendorAssignment accepted = VendorAssignment.builder()
                .assignmentId(assignmentId)
                .vendorId(UUID.randomUUID())
                .assignmentType(AssignmentType.RESTORATION)
                .status(AssignmentStatus.ACCEPTED)
                .taskDescription("Task")
                .build();

        when(assignmentRepository.findById(assignmentId)).thenReturn(Mono.just(accepted));
        when(assignmentRepository.save(any(VendorAssignment.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        UpdateAssignmentStatusRequest request = new UpdateAssignmentStatusRequest(
                "IN_PROGRESS", "Starting on-site work", LocalDate.now(), null
        );

        StepVerifier.create(service.updateAssignmentStatus(assignmentId, request))
                .assertNext(res -> assertEquals("IN_PROGRESS", res.status()))
                .verifyComplete();
    }

    @Test
    @DisplayName("Vendor completes assignment transitioning IN_PROGRESS to COMPLETED")
    void vendorCompletesAssignment() {
        UUID assignmentId = UUID.randomUUID();
        VendorAssignment inProgress = VendorAssignment.builder()
                .assignmentId(assignmentId)
                .vendorId(UUID.randomUUID())
                .assignmentType(AssignmentType.RESTORATION)
                .status(AssignmentStatus.IN_PROGRESS)
                .taskDescription("Task")
                .build();

        when(assignmentRepository.findById(assignmentId)).thenReturn(Mono.just(inProgress));
        when(assignmentRepository.save(any(VendorAssignment.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        List<UUID> evidence = List.of(UUID.randomUUID(), UUID.randomUUID());
        UpdateAssignmentStatusRequest request = new UpdateAssignmentStatusRequest(
                "COMPLETED", "Work completed successfully", LocalDate.now(), evidence
        );

        StepVerifier.create(service.updateAssignmentStatus(assignmentId, request))
                .assertNext(res -> {
                    assertEquals("COMPLETED", res.status());
                    assertNotNull(res.completedAt());
                    assertEquals(2, res.evidenceDocumentIds().size());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Vendor declines assignment when dispatched")
    void vendorDeclinesAssignment() {
        UUID assignmentId = UUID.randomUUID();
        VendorAssignment dispatched = VendorAssignment.builder()
                .assignmentId(assignmentId)
                .vendorId(UUID.randomUUID())
                .assignmentType(AssignmentType.RESTORATION)
                .status(AssignmentStatus.DISPATCHED)
                .build();

        when(assignmentRepository.findById(assignmentId)).thenReturn(Mono.just(dispatched));
        when(assignmentRepository.save(any(VendorAssignment.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        DeclineAssignmentRequest request = new DeclineAssignmentRequest("Capacity constrained");

        StepVerifier.create(service.declineAssignment(assignmentId, request))
                .assertNext(res -> assertEquals("DECLINED", res.status()))
                .verifyComplete();
    }

    @Test
    @DisplayName("Invalid assignment transitions are rejected")
    void invalidAssignmentTransitionRejected() {
        // Direct transition from DISPATCHED to IN_PROGRESS rejected
        assertThrows(IllegalStateException.class, () ->
                service.validateStatusTransition(AssignmentStatus.DISPATCHED, AssignmentStatus.IN_PROGRESS));

        // Direct transition from DISPATCHED to COMPLETED rejected
        assertThrows(IllegalStateException.class, () ->
                service.validateStatusTransition(AssignmentStatus.DISPATCHED, AssignmentStatus.COMPLETED));

        // Transition from COMPLETED rejected
        assertThrows(IllegalStateException.class, () ->
                service.validateStatusTransition(AssignmentStatus.COMPLETED, AssignmentStatus.IN_PROGRESS));

        // Transition from CANCELLED rejected
        assertThrows(IllegalStateException.class, () ->
                service.validateStatusTransition(AssignmentStatus.CANCELLED, AssignmentStatus.ACCEPTED));

        // Transition from DECLINED rejected
        assertThrows(IllegalStateException.class, () ->
                service.validateStatusTransition(AssignmentStatus.DECLINED, AssignmentStatus.ACCEPTED));

        // Valid transitions succeed
        assertDoesNotThrow(() ->
                service.validateStatusTransition(AssignmentStatus.DISPATCHED, AssignmentStatus.ACCEPTED));
        assertDoesNotThrow(() ->
                service.validateStatusTransition(AssignmentStatus.ACCEPTED, AssignmentStatus.IN_PROGRESS));
        assertDoesNotThrow(() ->
                service.validateStatusTransition(AssignmentStatus.IN_PROGRESS, AssignmentStatus.COMPLETED));
    }

    @Test
    @DisplayName("Query assignments by vendorId")
    void getAssignmentsByVendorId() {
        UUID vendorId = UUID.randomUUID();
        when(assignmentRepository.findByVendorId(vendorId)).thenReturn(Flux.empty());

        StepVerifier.create(service.getAssignments(new VendorAssignmentFilterRequest(
                vendorId, null, null, null, null, null, null, 0, 20)))
                .expectNextCount(0)
                .verifyComplete();
    }
}