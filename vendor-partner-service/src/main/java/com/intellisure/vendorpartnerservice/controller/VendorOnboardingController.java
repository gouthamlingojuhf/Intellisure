package com.intellisure.vendorpartnerservice.controller;

import com.intellisure.vendorpartnerservice.dto.VendorOnboardingListResponse;
import com.intellisure.vendorpartnerservice.dto.VendorOnboardingRequest;
import com.intellisure.vendorpartnerservice.dto.VendorOnboardingResponse;
import com.intellisure.vendorpartnerservice.dto.VerifyVendorRequest;
import com.intellisure.vendorpartnerservice.service.VendorOnboardingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/vendors")
@RequiredArgsConstructor
public class VendorOnboardingController {

    private final VendorOnboardingService onboardingService;

    @PostMapping("/onboarding-requests")
    public Mono<VendorOnboardingResponse> submitOnboardingRequest(@Valid @RequestBody VendorOnboardingRequest request) {
        return onboardingService.submitOnboardingRequest(request);
    }

    @GetMapping("/onboarding-requests")
    public Flux<VendorOnboardingResponse> getOnboardingRequests() {
        return onboardingService.getOnboardingRequests();
    }

    @PostMapping("/{vendorId}/verify")
    public Mono<VendorOnboardingResponse> verifyVendor(@PathVariable UUID vendorId, 
                                                       @Valid @RequestBody VerifyVendorRequest request) {
        return onboardingService.verifyVendor(vendorId, request);
    }
}