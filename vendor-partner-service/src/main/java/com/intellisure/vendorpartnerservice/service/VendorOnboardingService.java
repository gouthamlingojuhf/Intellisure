package com.intellisure.vendorpartnerservice.service;

import com.intellisure.vendorpartnerservice.dto.VendorOnboardingResponse;
import com.intellisure.vendorpartnerservice.dto.VerifyVendorRequest;
import com.intellisure.vendorpartnerservice.entity.OnboardingStatus;
import com.intellisure.vendorpartnerservice.entity.Vendor;
import com.intellisure.vendorpartnerservice.entity.VendorOnboardingRequest;
import com.intellisure.vendorpartnerservice.entity.VendorType;
import com.intellisure.vendorpartnerservice.entity.VendorVerificationStatus;
import com.intellisure.vendorpartnerservice.entity.VendorActiveStatus;
import com.intellisure.vendorpartnerservice.repository.VendorOnboardingRequestRepository;
import com.intellisure.vendorpartnerservice.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VendorOnboardingService {

    private final VendorOnboardingRequestRepository onboardingRepository;
    private final VendorRepository vendorRepository;

    public Mono<VendorOnboardingResponse> submitOnboardingRequest(com.intellisure.vendorpartnerservice.dto.VendorOnboardingRequest request) {
        return vendorRepository.findByLegalName(request.legalName())
                .flatMap(existingVendor -> onboardingRepository.findByVendorIdAndStatus(existingVendor.getVendorId(), OnboardingStatus.SUBMITTED)
                        .flatMap(existingRequest -> Mono.<VendorOnboardingResponse>error(new IllegalStateException("Onboarding request already pending for this vendor")))
                        .switchIfEmpty(Mono.defer(() -> createOnboardingRequest(existingVendor, request))))
                .switchIfEmpty(Mono.defer(() -> createNewVendorAndOnboarding(request)));
    }

    private Mono<VendorOnboardingResponse> createNewVendorAndOnboarding(com.intellisure.vendorpartnerservice.dto.VendorOnboardingRequest request) {
        Vendor vendor = Vendor.builder()
                .vendorId(UUID.randomUUID())
                .legalName(request.legalName())
                .displayName(request.displayName())
                .vendorType(VendorType.valueOf(request.vendorType()))
                .serviceTypes(request.serviceTypes())
                .capabilities(request.capabilities())
                .serviceAreas(request.serviceAreas())
                .contactName(request.contactName())
                .contactPhone(request.contactPhone())
                .contactEmail(request.contactEmail())
                .verificationStatus(VendorVerificationStatus.PENDING)
                .activeStatus(VendorActiveStatus.INACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isNew(true)
                .build();

        return vendorRepository.save(vendor)
                .flatMap(savedVendor -> createOnboardingRequest(savedVendor, request));
    }

    private Mono<VendorOnboardingResponse> createOnboardingRequest(Vendor vendor, com.intellisure.vendorpartnerservice.dto.VendorOnboardingRequest request) {
        com.intellisure.vendorpartnerservice.entity.VendorOnboardingRequest onboardingRequest = com.intellisure.vendorpartnerservice.entity.VendorOnboardingRequest.builder()
                .onboardingRequestId(UUID.randomUUID())
                .vendorId(vendor.getVendorId())
                .status(OnboardingStatus.SUBMITTED)
                .submittedAt(LocalDateTime.now())
                .isNew(true)
                .build();

        return onboardingRepository.save(onboardingRequest)
                .map(this::mapToResponse);
    }

    public Flux<VendorOnboardingResponse> getOnboardingRequests() {
        return onboardingRepository.findAll()
                .map(this::mapToResponse);
    }

    public Mono<VendorOnboardingResponse> verifyVendor(UUID vendorId, VerifyVendorRequest request) {
        return onboardingRepository.findByVendorIdAndStatus(vendorId, OnboardingStatus.SUBMITTED)
                .flatMap(onboardingRequest -> {
                    onboardingRequest.setNew(false);
                    if ("APPROVE".equalsIgnoreCase(request.verificationDecision())) {
                        onboardingRequest.setStatus(OnboardingStatus.VERIFIED);
                        onboardingRequest.setReviewedAt(LocalDateTime.now());
                        onboardingRequest.setReviewerId(request.verifiedDocumentIds() != null && !request.verifiedDocumentIds().isEmpty() ? request.verifiedDocumentIds().get(0) : null);
                        
                        return vendorRepository.findById(vendorId)
                                .flatMap(vendor -> {
                                    vendor.setVerificationStatus(VendorVerificationStatus.VERIFIED);
                                    vendor.setActiveStatus(VendorActiveStatus.ACTIVE);
                                    vendor.setUpdatedAt(LocalDateTime.now());
                                    vendor.setNew(false);
                                    return vendorRepository.save(vendor)
                                            .then(onboardingRepository.save(onboardingRequest))
                                            .thenReturn(mapToResponse(onboardingRequest));
                                });
                    } else {
                        onboardingRequest.setStatus(OnboardingStatus.REJECTED);
                        onboardingRequest.setReviewedAt(LocalDateTime.now());
                        onboardingRequest.setReviewerId(request.verifiedDocumentIds() != null && !request.verifiedDocumentIds().isEmpty() ? request.verifiedDocumentIds().get(0) : null);
                        onboardingRequest.setRejectionReason(request.verificationNote());
                        
                        return vendorRepository.findById(vendorId)
                                .flatMap(vendor -> {
                                    vendor.setVerificationStatus(VendorVerificationStatus.REJECTED);
                                    vendor.setUpdatedAt(LocalDateTime.now());
                                    vendor.setNew(false);
                                    return vendorRepository.save(vendor)
                                            .then(onboardingRepository.save(onboardingRequest))
                                            .thenReturn(mapToResponse(onboardingRequest));
                                });
                    }
                });
    }

    private VendorOnboardingResponse mapToResponse(com.intellisure.vendorpartnerservice.entity.VendorOnboardingRequest entity) {
        return new VendorOnboardingResponse(
                entity.getOnboardingRequestId(),
                entity.getVendorId(),
                entity.getStatus().name(),
                entity.getSubmittedAt(),
                entity.getReviewedAt(),
                entity.getReviewerId(),
                entity.getRejectionReason()
        );
    }
}