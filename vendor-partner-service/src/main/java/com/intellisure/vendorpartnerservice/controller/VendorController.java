package com.intellisure.vendorpartnerservice.controller;

import com.intellisure.vendorpartnerservice.dto.UpdateVendorRequest;
import com.intellisure.vendorpartnerservice.dto.UpdateVendorStatusRequest;
import com.intellisure.vendorpartnerservice.dto.VendorListResponse;
import com.intellisure.vendorpartnerservice.dto.VendorResponse;
import com.intellisure.vendorpartnerservice.dto.VendorSearchRequest;
import com.intellisure.vendorpartnerservice.service.VendorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/vendors")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;

    @GetMapping("/{vendorId}")
    public Mono<VendorResponse> getVendor(@PathVariable UUID vendorId) {
        return vendorService.getVendor(vendorId);
    }

    @GetMapping
    public Mono<VendorListResponse> searchVendors(
            @RequestParam(required = false) String serviceType,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) java.math.BigDecimal radiusKm,
            @RequestParam(required = false) String capability,
            @RequestParam(required = false) String availabilityStatus,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        VendorSearchRequest request = new VendorSearchRequest(
                serviceType, location, radiusKm, capability, availabilityStatus, page, size);
        return vendorService.searchVendors(request);
    }

    @GetMapping("/recommendations")
    public Mono<VendorListResponse> getRecommendations(
            @RequestParam(required = false) String serviceType,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) java.math.BigDecimal radiusKm,
            @RequestParam(required = false) String capability,
            @RequestParam(required = false) String availabilityStatus,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        VendorSearchRequest request = new VendorSearchRequest(
                serviceType, location, radiusKm, capability, availabilityStatus, page, size);
        return vendorService.recommendVendors(request);
    }

    @PutMapping("/{vendorId}")
    public Mono<VendorResponse> updateVendor(@PathVariable UUID vendorId, 
                                             @Valid @RequestBody UpdateVendorRequest request) {
        return vendorService.updateVendor(vendorId, request);
    }

    @PatchMapping("/{vendorId}/status")
    public Mono<VendorResponse> updateVendorStatus(@PathVariable UUID vendorId, 
                                                   @Valid @RequestBody UpdateVendorStatusRequest request) {
        return vendorService.updateVendorStatus(vendorId, request);
    }
}