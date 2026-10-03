package com.intellisure.vendorpartnerservice.repository;

import com.intellisure.vendorpartnerservice.entity.VendorOnboardingRequest;
import com.intellisure.vendorpartnerservice.entity.OnboardingStatus;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.UUID;

public interface VendorOnboardingRequestRepository extends R2dbcRepository<VendorOnboardingRequest, UUID> {
    Flux<VendorOnboardingRequest> findByVendorId(UUID vendorId);
    Flux<VendorOnboardingRequest> findByStatus(OnboardingStatus status);
    Mono<VendorOnboardingRequest> findByVendorIdAndStatus(UUID vendorId, OnboardingStatus status);
    Mono<Boolean> existsByVendorIdAndStatusIn(UUID vendorId, Collection<OnboardingStatus> statuses);
}