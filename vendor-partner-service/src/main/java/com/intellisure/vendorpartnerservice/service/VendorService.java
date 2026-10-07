package com.intellisure.vendorpartnerservice.service;

import com.intellisure.vendorpartnerservice.dto.UpdateVendorRequest;
import com.intellisure.vendorpartnerservice.dto.UpdateVendorStatusRequest;
import com.intellisure.vendorpartnerservice.dto.VendorResponse;
import com.intellisure.vendorpartnerservice.dto.VendorSearchRequest;
import com.intellisure.vendorpartnerservice.dto.VendorListResponse;
import com.intellisure.vendorpartnerservice.entity.Vendor;
import com.intellisure.vendorpartnerservice.entity.VendorActiveStatus;
import com.intellisure.vendorpartnerservice.entity.VendorType;
import com.intellisure.vendorpartnerservice.entity.VendorVerificationStatus;
import com.intellisure.vendorpartnerservice.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VendorService {

    private final VendorRepository vendorRepository;

    public Mono<VendorResponse> getVendor(UUID vendorId) {
        return vendorRepository.findById(vendorId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Vendor not found: " + vendorId)))
                .map(this::mapToResponse);
    }

    public Mono<VendorListResponse> recommendVendors(VendorSearchRequest request) {
        // Discovery and recommendation: returns eligible candidate vendors matching criteria.
        // NOTE: Recommendation MUST NOT create an assignment.
        return searchVendors(request);
    }

    public Mono<VendorListResponse> searchVendors(VendorSearchRequest request) {
        Flux<Vendor> vendors;
        
        if (request.serviceType() != null) {
            vendors = vendorRepository.findByServiceTypesContaining(request.serviceType());
        } else if (request.capability() != null) {
            vendors = vendorRepository.findByServiceTypesContaining(request.capability());
        } else if (request.location() != null) {
            vendors = vendorRepository.findByServiceAreasContaining(request.location());
        } else {
            vendors = vendorRepository.findAll();
        }
        
        return vendors
                .filter(v -> v.getVerificationStatus() == VendorVerificationStatus.VERIFIED &&
                           v.getActiveStatus() == VendorActiveStatus.ACTIVE)
                .map(this::mapToResponse)
                .collectList()
                .map(list -> new VendorListResponse(list, request.page() != null ? request.page() : 0, 
                        request.size() != null ? request.size() : 20, (long) list.size()));
    }

    public Mono<VendorResponse> updateVendor(UUID vendorId, UpdateVendorRequest request) {
        return vendorRepository.findById(vendorId)
                .flatMap(vendor -> {
                    if (request.displayName() != null) vendor.setDisplayName(request.displayName());
                    if (request.serviceTypes() != null) vendor.setServiceTypes(request.serviceTypes());
                    if (request.capabilities() != null) vendor.setCapabilities(request.capabilities());
                    if (request.serviceAreas() != null) vendor.setServiceAreas(request.serviceAreas());
                    if (request.contactName() != null) vendor.setContactName(request.contactName());
                    if (request.contactPhone() != null) vendor.setContactPhone(request.contactPhone());
                    if (request.contactEmail() != null) vendor.setContactEmail(request.contactEmail());
                    vendor.setUpdatedAt(LocalDateTime.now());
                    vendor.setNew(false);
                    return vendorRepository.save(vendor);
                })
                .map(this::mapToResponse);
    }

    public Mono<VendorResponse> updateVendorStatus(UUID vendorId, UpdateVendorStatusRequest request) {
        return vendorRepository.findById(vendorId)
                .flatMap(vendor -> {
                    VendorActiveStatus newStatus = VendorActiveStatus.valueOf(request.activeStatus());
                    vendor.setActiveStatus(newStatus);
                    vendor.setUpdatedAt(LocalDateTime.now());
                    vendor.setNew(false);
                    return vendorRepository.save(vendor);
                })
                .map(this::mapToResponse);
    }

    private VendorResponse mapToResponse(Vendor vendor) {
        return new VendorResponse(
                vendor.getVendorId(),
                vendor.getLegalName(),
                vendor.getDisplayName(),
                vendor.getVendorType().name(),
                vendor.getServiceTypes(),
                vendor.getCapabilities(),
                vendor.getServiceAreas(),
                vendor.getVerificationStatus().name(),
                vendor.getActiveStatus().name(),
                vendor.getContactPhone(),
                vendor.getContactEmail()
        );
    }
}