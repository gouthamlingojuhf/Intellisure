package com.intellisure.vendorpartnerservice.controller;

import com.intellisure.vendorpartnerservice.dto.*;
import com.intellisure.vendorpartnerservice.service.VendorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.util.UUID;

@RestController
@RequestMapping("/api/vendor-assignments")
@RequiredArgsConstructor
public class AssignmentController {
    private final VendorService vendorService;

    @PostMapping
    public Mono<AssignmentResponse> create(@Valid @RequestBody CreateAssignmentRequest request) {
        return vendorService.assign(request.vendorId(), request.claimId(), request.serviceRequested());
    }

    @PostMapping("/{id}/accept")
    public Mono<AssignmentResponse> accept(@PathVariable UUID id) {
        return vendorService.updateAssignment(id, "ACCEPTED");
    }

    @PatchMapping("/{id}/status")
    public Mono<AssignmentResponse> status(@PathVariable UUID id, @RequestParam String value) {
        return vendorService.updateAssignment(id, value);
    }

    @PostMapping("/{id}/complete")
    public Mono<AssignmentResponse> complete(@PathVariable UUID id) {
        return vendorService.updateAssignment(id, "COMPLETED");
    }
}
