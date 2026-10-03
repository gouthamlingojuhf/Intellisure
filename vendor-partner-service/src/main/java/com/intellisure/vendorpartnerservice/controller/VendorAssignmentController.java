package com.intellisure.vendorpartnerservice.controller;

import com.intellisure.vendorpartnerservice.dto.AcceptAssignmentRequest;
import com.intellisure.vendorpartnerservice.dto.CreateVendorAssignmentRequest;
import com.intellisure.vendorpartnerservice.dto.DeclineAssignmentRequest;
import com.intellisure.vendorpartnerservice.dto.UpdateAssignmentStatusRequest;
import com.intellisure.vendorpartnerservice.dto.VendorAssignmentListResponse;
import com.intellisure.vendorpartnerservice.dto.VendorAssignmentResponse;
import com.intellisure.vendorpartnerservice.dto.VendorAssignmentFilterRequest;
import com.intellisure.vendorpartnerservice.service.VendorAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/vendor-assignments")
@RequiredArgsConstructor
public class VendorAssignmentController {

    private final VendorAssignmentService assignmentService;

    @PostMapping
    public Mono<VendorAssignmentResponse> createAssignment(@Valid @RequestBody CreateVendorAssignmentRequest request) {
        return assignmentService.createAssignment(request);
    }

    @GetMapping("/{assignmentId}")
    public Mono<VendorAssignmentResponse> getAssignment(@PathVariable UUID assignmentId) {
        return assignmentService.getAssignment(assignmentId);
    }

    @GetMapping
    public Mono<VendorAssignmentListResponse> getAssignments(
            @RequestParam(required = false) UUID vendorId,
            @RequestParam(required = false) UUID claimId,
            @RequestParam(required = false) UUID recoveryCaseId,
            @RequestParam(required = false) String assignmentType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) java.time.LocalDate fromDate,
            @RequestParam(required = false) java.time.LocalDate toDate,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        VendorAssignmentFilterRequest filter = new VendorAssignmentFilterRequest(
                vendorId, claimId, recoveryCaseId, assignmentType, status, fromDate, toDate, page, size);
        
        return assignmentService.getAssignments(filter)
                .collectList()
                .map(list -> new VendorAssignmentListResponse(list, page, size, (long) list.size()));
    }

    @PostMapping("/{assignmentId}/accept")
    public Mono<VendorAssignmentResponse> acceptAssignment(@PathVariable UUID assignmentId, 
                                                           @Valid @RequestBody AcceptAssignmentRequest request) {
        return assignmentService.acceptAssignment(assignmentId, request);
    }

    @PostMapping("/{assignmentId}/decline")
    public Mono<VendorAssignmentResponse> declineAssignment(@PathVariable UUID assignmentId, 
                                                            @Valid @RequestBody DeclineAssignmentRequest request) {
        return assignmentService.declineAssignment(assignmentId, request);
    }

    @PatchMapping("/{assignmentId}/status")
    public Mono<VendorAssignmentResponse> updateAssignmentStatus(@PathVariable UUID assignmentId, 
                                                                 @Valid @RequestBody UpdateAssignmentStatusRequest request) {
        return assignmentService.updateAssignmentStatus(assignmentId, request);
    }
}