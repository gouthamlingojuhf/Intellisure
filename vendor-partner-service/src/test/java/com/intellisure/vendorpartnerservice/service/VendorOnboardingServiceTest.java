package com.intellisure.vendorpartnerservice.service;

import com.intellisure.vendorpartnerservice.dto.VendorOnboardingRequest;
import com.intellisure.vendorpartnerservice.dto.VerifyVendorRequest;
import com.intellisure.vendorpartnerservice.entity.*;
import com.intellisure.vendorpartnerservice.repository.VendorOnboardingRequestRepository;
import com.intellisure.vendorpartnerservice.repository.VendorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VendorOnboardingServiceTest {
    @Mock VendorOnboardingRequestRepository onboardingRepository;
    @Mock VendorRepository vendorRepository;
    @InjectMocks VendorOnboardingService service;

    @Test
    void createsNewVendorAndSubmittedOnboardingRequest() {
        VendorOnboardingRequest request = onboardingRequest("New Vendor");
        when(vendorRepository.findByLegalName(request.legalName())).thenReturn(Mono.empty());
        when(vendorRepository.save(any(Vendor.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(onboardingRepository.save(any(com.intellisure.vendorpartnerservice.entity.VendorOnboardingRequest.class)))
                .thenAnswer(i -> Mono.just(i.getArgument(0)));
        StepVerifier.create(service.submitOnboardingRequest(request))
                .assertNext(response -> assertEquals("SUBMITTED", response.status())).verifyComplete();
        verify(vendorRepository).save(any(Vendor.class));
        verify(onboardingRepository).save(any(com.intellisure.vendorpartnerservice.entity.VendorOnboardingRequest.class));
    }

    @Test
    void rejectsDuplicatePendingRequestAndAllowsExistingVendorWithoutPendingRequest() {
        Vendor vendor = vendor(UUID.randomUUID());
        VendorOnboardingRequest request = onboardingRequest(vendor.getLegalName());
        when(vendorRepository.findByLegalName(vendor.getLegalName())).thenReturn(Mono.just(vendor));
        when(onboardingRepository.findByVendorIdAndStatus(vendor.getVendorId(), OnboardingStatus.SUBMITTED))
                .thenReturn(Mono.just(onboarding(vendor.getVendorId(), OnboardingStatus.SUBMITTED)));
        StepVerifier.create(service.submitOnboardingRequest(request)).expectError(IllegalStateException.class).verify();

        when(onboardingRepository.findByVendorIdAndStatus(vendor.getVendorId(), OnboardingStatus.SUBMITTED)).thenReturn(Mono.empty());
        when(onboardingRepository.save(any(com.intellisure.vendorpartnerservice.entity.VendorOnboardingRequest.class)))
                .thenAnswer(i -> Mono.just(i.getArgument(0)));
        StepVerifier.create(service.submitOnboardingRequest(request)).assertNext(r -> assertEquals(vendor.getVendorId(), r.vendorId())).verifyComplete();
    }

    @Test
    void verifiesAndRejectsSubmittedVendorRequests() {
        UUID vendorId = UUID.randomUUID(); UUID reviewer = UUID.randomUUID();
        Vendor vendor = vendor(vendorId);
        com.intellisure.vendorpartnerservice.entity.VendorOnboardingRequest pending = onboarding(vendorId, OnboardingStatus.SUBMITTED);
        when(onboardingRepository.findByVendorIdAndStatus(vendorId, OnboardingStatus.SUBMITTED)).thenReturn(Mono.just(pending));
        when(vendorRepository.findById(vendorId)).thenReturn(Mono.just(vendor));
        when(vendorRepository.save(any(Vendor.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(onboardingRepository.save(any(com.intellisure.vendorpartnerservice.entity.VendorOnboardingRequest.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        VerifyVendorRequest approve = new VerifyVendorRequest("APPROVE", null, List.of(reviewer));
        StepVerifier.create(service.verifyVendor(vendorId, approve)).assertNext(r -> assertEquals("VERIFIED", r.status())).verifyComplete();
        assertEquals(VendorVerificationStatus.VERIFIED, vendor.getVerificationStatus()); assertEquals(VendorActiveStatus.ACTIVE, vendor.getActiveStatus());

        pending.setStatus(OnboardingStatus.SUBMITTED);
        VerifyVendorRequest reject = new VerifyVendorRequest("REJECT", "Missing evidence", List.of(reviewer));
        StepVerifier.create(service.verifyVendor(vendorId, reject)).assertNext(r -> assertEquals("REJECTED", r.status())).verifyComplete();
        assertEquals(VendorVerificationStatus.REJECTED, vendor.getVerificationStatus());
        when(onboardingRepository.findAll()).thenReturn(Flux.just(pending));
        StepVerifier.create(service.getOnboardingRequests()).expectNextCount(1).verifyComplete();
    }

    private VendorOnboardingRequest onboardingRequest(String name) {
        return new VendorOnboardingRequest(name, name + " Display", "PROPERTY_RESTORATION", List.of("RESTORATION"),
                List.of("WATER_MITIGATION"), List.of("AREA"), "Contact", "555", "vendor@example.com", List.of());
    }

    private Vendor vendor(UUID id) {
        return Vendor.builder().vendorId(id).legalName("Existing Vendor").displayName("Existing")
                .vendorType(VendorType.PROPERTY_RESTORATION).verificationStatus(VendorVerificationStatus.PENDING)
                .activeStatus(VendorActiveStatus.INACTIVE).build();
    }

    private com.intellisure.vendorpartnerservice.entity.VendorOnboardingRequest onboarding(UUID vendorId, OnboardingStatus status) {
        return com.intellisure.vendorpartnerservice.entity.VendorOnboardingRequest.builder()
                .onboardingRequestId(UUID.randomUUID()).vendorId(vendorId).status(status).build();
    }
}
