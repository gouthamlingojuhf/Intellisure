package com.intellisure.vendorpartnerservice.controller;

import com.intellisure.vendorpartnerservice.dto.RegisterVendorRequest;
import com.intellisure.vendorpartnerservice.dto.VendorResponse;
import com.intellisure.vendorpartnerservice.service.VendorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import java.util.UUID;
import com.intellisure.vendorpartnerservice.dto.AssignmentResponse;

@RestController
@RequestMapping("/api/vendors")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;

    @PostMapping
    public Mono<VendorResponse> registerVendor(@Valid @RequestBody RegisterVendorRequest request) {
        return vendorService.registerVendor(request);
    }

    @GetMapping
    public Flux<VendorResponse> vendors() { return vendorService.getVendors(); }

    @PatchMapping("/{vendorId}/status")
    public Mono<VendorResponse> status(@PathVariable UUID vendorId, @RequestParam String value) {
        return vendorService.updateStatus(vendorId, value);
    }

    @PostMapping("/{vendorId}/verify")
    public Mono<VendorResponse> verify(@PathVariable UUID vendorId) {
        return vendorService.updateStatus(vendorId, "ACTIVE");
    }

    @PostMapping("/assignments")
    public Mono<AssignmentResponse> assign(@RequestParam UUID vendorId, @RequestParam UUID claimId,
                                           @RequestParam String serviceRequested) {
        return vendorService.assign(vendorId, claimId, serviceRequested);
    }


    @PostMapping("/assignments/{assignmentId}/accept")
    public Mono<AssignmentResponse> accept(@PathVariable UUID assignmentId) {
        return vendorService.updateAssignment(assignmentId, "ACCEPTED");
    }

    @PostMapping("/assignments/{assignmentId}/complete")
    public Mono<AssignmentResponse> complete(@PathVariable UUID assignmentId) {
        return vendorService.updateAssignment(assignmentId, "COMPLETED");
    }
}
