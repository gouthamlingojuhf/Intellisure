package com.intellisure.vendorpartnerservice.service;

import com.intellisure.vendorpartnerservice.dto.RegisterVendorRequest;
import com.intellisure.vendorpartnerservice.entity.Vendor;
import com.intellisure.vendorpartnerservice.repository.*;
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
class VendorServiceTest {
    @Mock VendorRepository vendorRepository; @Mock VendorAssignmentRepository assignmentRepository;
    @InjectMocks VendorService service;

    @Test
    void registersVendorPendingOnboarding() {
        when(vendorRepository.save(any(Vendor.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        StepVerifier.create(service.registerVendor(new RegisterVendorRequest("Acme", "TOWING", "a@b.test", "555", "US")))
                .assertNext(v -> assertEquals("PENDING_ONBOARDING", v.status())).verifyComplete();
    }

    @Test
    void updatesVendorStatusAndListsVendors() {
        Vendor vendor = Vendor.builder().vendorId(UUID.randomUUID()).status("PENDING_ONBOARDING").build();
        when(vendorRepository.findById(vendor.getVendorId())).thenReturn(Mono.just(vendor));
        when(vendorRepository.save(vendor)).thenReturn(Mono.just(vendor));
        StepVerifier.create(service.updateStatus(vendor.getVendorId(), "ACTIVE"))
                .assertNext(v -> assertEquals("ACTIVE", v.status())).verifyComplete();
        when(vendorRepository.findAll()).thenReturn(Flux.just(vendor));
        StepVerifier.create(service.getVendors()).expectNextCount(1).verifyComplete();
    }

    @Test
    void completingAssignmentSetsCompletionDate() {
        com.intellisure.vendorpartnerservice.entity.VendorAssignment assignment =
                com.intellisure.vendorpartnerservice.entity.VendorAssignment.builder().assignmentId(UUID.randomUUID()).status("ACCEPTED").build();
        when(assignmentRepository.findById(assignment.getAssignmentId())).thenReturn(Mono.just(assignment));
        when(assignmentRepository.save(assignment)).thenReturn(Mono.just(assignment));
        StepVerifier.create(service.updateAssignment(assignment.getAssignmentId(), "COMPLETED"))
                .assertNext(a -> assertEquals("COMPLETED", a.status())).verifyComplete();
        org.junit.jupiter.api.Assertions.assertNotNull(assignment.getCompletedDate());
    }
}
