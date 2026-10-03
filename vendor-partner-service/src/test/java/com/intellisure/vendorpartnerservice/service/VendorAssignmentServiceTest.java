package com.intellisure.vendorpartnerservice.service;

import com.intellisure.vendorpartnerservice.entity.Vendor;
import com.intellisure.vendorpartnerservice.entity.VendorAssignment;
import com.intellisure.vendorpartnerservice.entity.VendorType;
import com.intellisure.vendorpartnerservice.entity.VendorVerificationStatus;
import com.intellisure.vendorpartnerservice.entity.VendorActiveStatus;
import com.intellisure.vendorpartnerservice.entity.AssignmentStatus;
import com.intellisure.vendorpartnerservice.entity.AssignmentType;
import com.intellisure.vendorpartnerservice.repository.VendorAssignmentRepository;
import com.intellisure.vendorpartnerservice.repository.VendorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.time.LocalDate;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VendorAssignmentServiceTest {
    @Mock VendorRepository vendorRepository;
    @Mock VendorAssignmentRepository assignmentRepository;
    @InjectMocks VendorAssignmentService service;

    @Test
    void createAssignmentRequiresVerifiedActiveVendor() {
        UUID vendor = UUID.randomUUID(), claim = UUID.randomUUID();
        Vendor v = Vendor.builder().vendorId(vendor).legalName("Test").displayName("Test")
                .vendorType(VendorType.PROPERTY_RESTORATION).serviceTypes(java.util.List.of("RESTORATION"))
                .capabilities(java.util.List.of("RESTORATION")).serviceAreas(java.util.List.of("US"))
                .contactName("Test").contactPhone("555-0000").contactEmail("test@test.com")
                .verificationStatus(VendorVerificationStatus.PENDING).activeStatus(VendorActiveStatus.INACTIVE).build();
        when(vendorRepository.findById(vendor)).thenReturn(Mono.just(v));
        
        StepVerifier.create(service.createAssignment(new com.intellisure.vendorpartnerservice.dto.CreateVendorAssignmentRequest(
                        vendor, AssignmentType.CLAIM_TOWING.name(), claim, null, "Emergency repair", LocalDate.now(), "HIGH")))
                .expectError(IllegalStateException.class).verify();
    }

    @Test
    void assignmentFlowFromDispatchToAcceptToComplete() {
        UUID vendor = UUID.randomUUID(), claim = UUID.randomUUID();
        Vendor v = Vendor.builder().vendorId(vendor).legalName("Test").displayName("Test")
                .vendorType(VendorType.PROPERTY_RESTORATION).serviceTypes(java.util.List.of("RESTORATION"))
                .capabilities(java.util.List.of("RESTORATION")).serviceAreas(java.util.List.of("US"))
                .contactName("Test").contactPhone("555-0000").contactEmail("test@test.com")
                .verificationStatus(VendorVerificationStatus.VERIFIED).activeStatus(VendorActiveStatus.ACTIVE).build();
        when(vendorRepository.findById(vendor)).thenReturn(Mono.just(v));
        
        UUID assignmentId = UUID.randomUUID();
        VendorAssignment dispatched = VendorAssignment.builder().assignmentId(assignmentId)
                .vendorId(vendor).claimId(claim).assignmentType(AssignmentType.CLAIM_TOWING)
                .status(AssignmentStatus.DISPATCHED).taskDescription("Emergency repair")
                .dueDate(LocalDate.now()).priority("HIGH").build();
        when(assignmentRepository.save(any(VendorAssignment.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        StepVerifier.create(service.createAssignment(new com.intellisure.vendorpartnerservice.dto.CreateVendorAssignmentRequest(
                        vendor, AssignmentType.CLAIM_TOWING.name(), claim, null, "Emergency repair", LocalDate.now(), "HIGH")))
                .assertNext(result -> org.junit.jupiter.api.Assertions.assertEquals("DISPATCHED", result.status()))
                .verifyComplete();

        // Accept the assignment - findById returns the DISPATCHED assignment
        when(assignmentRepository.findById(assignmentId)).thenReturn(Mono.just(dispatched));
        StepVerifier.create(service.acceptAssignment(assignmentId, 
                        new com.intellisure.vendorpartnerservice.dto.AcceptAssignmentRequest("Accepted", LocalDate.now())))
                .assertNext(result -> org.junit.jupiter.api.Assertions.assertEquals("ACCEPTED", result.status()))
                .verifyComplete();

        // Complete the assignment - findById returns the ACCEPTED assignment
        VendorAssignment accepted = VendorAssignment.builder().assignmentId(assignmentId)
                .vendorId(vendor).claimId(claim).assignmentType(AssignmentType.CLAIM_TOWING)
                .status(AssignmentStatus.ACCEPTED).taskDescription("Emergency repair")
                .dueDate(LocalDate.now()).priority("HIGH").acceptedAt(java.time.LocalDateTime.now()).build();
        when(assignmentRepository.findById(assignmentId)).thenReturn(Mono.just(accepted));
        StepVerifier.create(service.updateAssignmentStatus(assignmentId, 
                        new com.intellisure.vendorpartnerservice.dto.UpdateAssignmentStatusRequest(
                                "COMPLETED", null, LocalDate.now(), null)))
                .assertNext(result -> {
                    org.junit.jupiter.api.Assertions.assertEquals("COMPLETED", result.status());
                    org.junit.jupiter.api.Assertions.assertNotNull(result.completedAt());
                }).verifyComplete();
    }

    @Test
    void acceptingAssignmentMovesItToAcceptedWithoutCompletionDate() {
        UUID assignmentId = UUID.randomUUID();
        VendorAssignment assigned = VendorAssignment.builder().assignmentId(assignmentId)
                .vendorId(UUID.randomUUID()).claimId(UUID.randomUUID())
                .assignmentType(AssignmentType.CLAIM_TOWING).status(AssignmentStatus.DISPATCHED)
                .taskDescription("Emergency repair").dueDate(LocalDate.now()).priority("HIGH")
                .build();
        when(assignmentRepository.findById(assignmentId)).thenReturn(Mono.just(assigned));
        when(assignmentRepository.save(any(VendorAssignment.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.acceptAssignment(assignmentId, 
                        new com.intellisure.vendorpartnerservice.dto.AcceptAssignmentRequest("Accepted", LocalDate.now())))
                .assertNext(result -> {
                    org.junit.jupiter.api.Assertions.assertEquals("ACCEPTED", result.status());
                    org.junit.jupiter.api.Assertions.assertNull(result.completedAt());
                }).verifyComplete();
        verify(assignmentRepository).save(any(VendorAssignment.class));
    }

    @Test
    void getAssignmentsByVendorId() {
        UUID vendorId = UUID.randomUUID();
        when(assignmentRepository.findByVendorId(vendorId)).thenReturn(Flux.empty());
        StepVerifier.create(service.getAssignments(new com.intellisure.vendorpartnerservice.dto.VendorAssignmentFilterRequest(
                vendorId, null, null, null, null, null, null, 0, 20)))
                .expectNextCount(0).verifyComplete();
    }
}