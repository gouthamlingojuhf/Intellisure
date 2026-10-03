package com.intellisure.recoveryservice.controller;

import com.intellisure.recoveryservice.dto.CreateRecoverySupportRequest;
import com.intellisure.recoveryservice.dto.RecoverySupportRequestResponse;
import com.intellisure.recoveryservice.dto.UpdateSupportRequestStatusRequest;
import com.intellisure.recoveryservice.entity.RecoverySupportStatus;
import com.intellisure.recoveryservice.entity.SupportPriority;
import com.intellisure.recoveryservice.entity.SupportType;
import com.intellisure.recoveryservice.service.RecoverySupportRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/recovery/cases")
@RequiredArgsConstructor
public class RecoverySupportRequestController {

    private final RecoverySupportRequestService supportRequestService;

    @PostMapping("/{recoveryCaseId}/support-requests")
    public Mono<RecoverySupportRequestResponse> createSupportRequest(
            @PathVariable UUID recoveryCaseId,
            @Valid @RequestBody CreateRecoverySupportRequest request) {
        
        CreateRecoverySupportRequest requestWithCaseId = new CreateRecoverySupportRequest(
                recoveryCaseId, request.supportType(), request.description(),
                request.priority(), request.requiredByDate(), request.location(),
                request.vendorAssignmentId());
        return supportRequestService.createSupportRequest(requestWithCaseId);
    }

    @GetMapping("/{recoveryCaseId}/support-requests")
    public Flux<RecoverySupportRequestResponse> getSupportRequests(@PathVariable UUID recoveryCaseId) {
        return supportRequestService.getSupportRequests(recoveryCaseId);
    }

    @GetMapping("/support-requests/{supportRequestId}")
    public Mono<RecoverySupportRequestResponse> getSupportRequest(@PathVariable UUID supportRequestId) {
        return supportRequestService.getSupportRequest(supportRequestId);
    }

    @GetMapping("/support-requests")
    public Flux<RecoverySupportRequestResponse> getSupportRequestsByStatus(
            @RequestParam(required = false) com.intellisure.recoveryservice.entity.RecoverySupportStatus status,
            @RequestParam(required = false) com.intellisure.recoveryservice.entity.SupportPriority priority,
            @RequestParam(required = false) com.intellisure.recoveryservice.entity.SupportType supportType) {
        
        if (status != null) {
            return supportRequestService.getSupportRequestsByStatus(status);
        } else if (priority != null) {
            return supportRequestService.getSupportRequestsByPriority(priority);
        } else if (supportType != null) {
            return supportRequestService.getSupportRequestsByType(supportType);
        }
        return reactor.core.publisher.Flux.empty();
    }

    @GetMapping("/support-requests/overdue")
    public Flux<RecoverySupportRequestResponse> getOverdueSupportRequests() {
        return supportRequestService.getOverdueSupportRequests();
    }

    @PatchMapping("/support-requests/{supportRequestId}/status")
    public Mono<RecoverySupportRequestResponse> updateSupportRequestStatus(
            @PathVariable UUID supportRequestId,
            @Valid @RequestBody com.intellisure.recoveryservice.dto.UpdateSupportRequestStatusRequest request) {
        return supportRequestService.updateStatus(supportRequestId, request);
    }
}