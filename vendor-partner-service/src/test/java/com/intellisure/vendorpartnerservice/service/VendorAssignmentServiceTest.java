package com.intellisure.vendorpartnerservice.service;

import com.intellisure.vendorpartnerservice.dto.RegisterVendorRequest;
import com.intellisure.vendorpartnerservice.entity.Vendor;
import com.intellisure.vendorpartnerservice.entity.VendorAssignment;
import com.intellisure.vendorpartnerservice.repository.VendorAssignmentRepository;
import com.intellisure.vendorpartnerservice.repository.VendorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
    @InjectMocks VendorService service;

    @Test
    void registrationStartsVendorInPendingOnboarding() {
        Vendor saved = Vendor.builder().vendorId(UUID.randomUUID()).vendorName("Reliable Repairs")
                .vendorType("RESTORATION").status("PENDING_ONBOARDING").contactEmail("vendor@example.com")
                .contactPhone("555-0100").serviceRegions("NE").build();
        when(vendorRepository.save(any(Vendor.class))).thenReturn(Mono.just(saved));
        StepVerifier.create(service.registerVendor(new RegisterVendorRequest("Reliable Repairs",
                        "RESTORATION", "vendor@example.com", "555-0100", "NE")))
                .assertNext(result -> org.junit.jupiter.api.Assertions.assertEquals(
                        "PENDING_ONBOARDING", result.status())).verifyComplete();
    }

    @Test
    void assignmentStartsAssignedAndCompletionSetsCompletionDate() {
        UUID vendor = UUID.randomUUID(), claim = UUID.randomUUID();
        VendorAssignment assigned = VendorAssignment.builder().assignmentId(UUID.randomUUID())
                .vendorId(vendor).claimId(claim).serviceRequested("Emergency repair")
                .status("ASSIGNED").assignedDate(LocalDate.now()).build();
        when(assignmentRepository.save(any(VendorAssignment.class))).thenReturn(Mono.just(assigned));
        StepVerifier.create(service.assign(vendor, claim, "Emergency repair"))
                .assertNext(result -> org.junit.jupiter.api.Assertions.assertEquals("ASSIGNED", result.status()))
                .verifyComplete();

        when(assignmentRepository.findById(assigned.getAssignmentId())).thenReturn(Mono.just(assigned));
        when(assignmentRepository.save(assigned)).thenReturn(Mono.just(assigned));
        StepVerifier.create(service.updateAssignment(assigned.getAssignmentId(), "COMPLETED"))
                .assertNext(result -> {
                    org.junit.jupiter.api.Assertions.assertEquals("COMPLETED", result.status());
                    org.junit.jupiter.api.Assertions.assertNotNull(result.completedDate());
                }).verifyComplete();
    }

    @Test
    void acceptingAssignmentMovesItToAcceptedWithoutCompletionDate() {
        UUID assignmentId = UUID.randomUUID();
        VendorAssignment assigned = VendorAssignment.builder().assignmentId(assignmentId)
                .vendorId(UUID.randomUUID()).claimId(UUID.randomUUID())
                .serviceRequested("Emergency repair").status("ASSIGNED")
                .assignedDate(LocalDate.now()).build();
        when(assignmentRepository.findById(assignmentId)).thenReturn(Mono.just(assigned));
        when(assignmentRepository.save(assigned)).thenReturn(Mono.just(assigned));

        StepVerifier.create(service.updateAssignment(assignmentId, "ACCEPTED"))
                .assertNext(result -> {
                    org.junit.jupiter.api.Assertions.assertEquals("ACCEPTED", result.status());
                    org.junit.jupiter.api.Assertions.assertNull(result.completedDate());
                }).verifyComplete();
        verify(assignmentRepository).save(assigned);
    }

    @Test
    void vendorListingMapsAllOnboardedVendors() {
        Vendor vendor = Vendor.builder().vendorId(UUID.randomUUID())
                .vendorName("Reliable Repairs").vendorType("RESTORATION")
                .status("ACTIVE").build();
        when(vendorRepository.findAll()).thenReturn(reactor.core.publisher.Flux.just(vendor));

        StepVerifier.create(service.getVendors())
                .assertNext(result -> {
                    org.junit.jupiter.api.Assertions.assertEquals(vendor.getVendorId(), result.vendorId());
                    org.junit.jupiter.api.Assertions.assertEquals("ACTIVE", result.status());
                }).verifyComplete();
        verify(vendorRepository).findAll();
    }

    @Test
    void vendorStatusUpdatePreservesTheVendorIdentity() {
        UUID vendorId = UUID.randomUUID();
        Vendor vendor = Vendor.builder().vendorId(vendorId).vendorName("Reliable Repairs")
                .status("PENDING_ONBOARDING").build();
        when(vendorRepository.findById(vendorId)).thenReturn(Mono.just(vendor));
        when(vendorRepository.save(vendor)).thenReturn(Mono.just(vendor));

        StepVerifier.create(service.updateStatus(vendorId, "ACTIVE"))
                .assertNext(result -> {
                    org.junit.jupiter.api.Assertions.assertEquals(vendorId, result.vendorId());
                    org.junit.jupiter.api.Assertions.assertEquals("ACTIVE", result.status());
                }).verifyComplete();
        verify(vendorRepository).save(vendor);
    }
}
