package com.intellisure.recoveryservice.controller;

import com.intellisure.recoveryservice.dto.CreateRecoveryCaseRequest;
import com.intellisure.recoveryservice.dto.RecoveryCaseFilterRequest;
import com.intellisure.recoveryservice.dto.RecoveryCaseListResponse;
import com.intellisure.recoveryservice.dto.RecoveryCaseResponse;
import com.intellisure.recoveryservice.dto.UpdateRecoveryCaseRequest;
import com.intellisure.recoveryservice.dto.UpdateRecoveryStatusRequest;
import com.intellisure.recoveryservice.dto.CompleteRecoveryCaseRequest;
import com.intellisure.recoveryservice.dto.RecoveryEstimationRequest;
import com.intellisure.recoveryservice.dto.RecoveryEstimationResponse;
import com.intellisure.recoveryservice.entity.RecoveryCaseStatus;
import com.intellisure.recoveryservice.entity.RecoverySeverity;
import com.intellisure.recoveryservice.service.RecoveryCaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/recovery/cases")
@RequiredArgsConstructor
public class RecoveryCaseController {

    private final RecoveryCaseService recoveryCaseService;

    @PostMapping
    public Mono<RecoveryCaseResponse> createCase(@Valid @RequestBody CreateRecoveryCaseRequest request) {
        return recoveryCaseService.createCase(request);
    }

    @GetMapping("/{recoveryCaseId}")
    public Mono<RecoveryCaseResponse> getCase(@PathVariable UUID recoveryCaseId) {
        return recoveryCaseService.getCase(recoveryCaseId);
    }

    @GetMapping
    public Mono<com.intellisure.recoveryservice.dto.RecoveryCaseListResponse> getCases(
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) UUID ownerId,
            @RequestParam(required = false) RecoveryCaseStatus status,
            @RequestParam(required = false) com.intellisure.recoveryservice.entity.RecoverySeverity severity,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        RecoveryCaseFilterRequest filter = new RecoveryCaseFilterRequest(
                customerId, ownerId, status, severity, fromDate, toDate, page, size);
        return recoveryCaseService.getCases(filter);
    }

    @PutMapping("/{recoveryCaseId}")
    public Mono<RecoveryCaseResponse> updateCase(
            @PathVariable UUID recoveryCaseId,
            @Valid @RequestBody com.intellisure.recoveryservice.dto.UpdateRecoveryCaseRequest request) {
        return recoveryCaseService.updateCase(recoveryCaseId, request);
    }

    @PatchMapping("/{recoveryCaseId}/status")
    public Mono<RecoveryCaseResponse> updateStatus(
            @PathVariable UUID recoveryCaseId,
            @Valid @RequestBody UpdateRecoveryStatusRequest request) {
        return recoveryCaseService.updateStatus(recoveryCaseId, request);
    }

    @PostMapping("/{recoveryCaseId}/complete")
    public Mono<RecoveryCaseResponse> completeCase(
            @PathVariable UUID recoveryCaseId,
            @Valid @RequestBody CompleteRecoveryCaseRequest request) {
        return recoveryCaseService.completeCase(recoveryCaseId, request);
    }

    @PostMapping("/{recoveryCaseId}/estimate")
    public Mono<com.intellisure.recoveryservice.dto.RecoveryEstimationResponse> estimateRecovery(
            @PathVariable UUID recoveryCaseId,
            @Valid @RequestBody RecoveryEstimationRequest request) {
        return recoveryCaseService.estimateRecoveryAsync(request);
    }
}