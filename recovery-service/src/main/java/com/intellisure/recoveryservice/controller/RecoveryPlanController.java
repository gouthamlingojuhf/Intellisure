package com.intellisure.recoveryservice.controller;

import com.intellisure.recoveryservice.dto.CreateRecoveryPlanRequest;
import com.intellisure.recoveryservice.dto.RecoveryPlanResponse;
import com.intellisure.recoveryservice.entity.RecoveryPlanStatus;
import com.intellisure.recoveryservice.service.RecoveryPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/recovery/cases")
@RequiredArgsConstructor
public class RecoveryPlanController {

    private final RecoveryPlanService recoveryPlanService;

    @PostMapping("/{recoveryCaseId}/plan")
    public Mono<RecoveryPlanResponse> createPlan(
            @PathVariable UUID recoveryCaseId,
            @Valid @RequestBody CreateRecoveryPlanRequest request) {
        
        CreateRecoveryPlanRequest requestWithCaseId = new CreateRecoveryPlanRequest(
                recoveryCaseId, request.planSummary(), request.priorityActions(),
                request.vendorAssignmentIds(), request.temporaryResourceNeeds(),
                request.targetMilestones(), request.status());
        return recoveryPlanService.createPlan(requestWithCaseId);
    }

    @GetMapping("/{recoveryCaseId}/plan")
    public Mono<RecoveryPlanResponse> getPlanByCaseId(@PathVariable UUID recoveryCaseId) {
        return recoveryPlanService.getPlanByCaseId(recoveryCaseId);
    }

    @GetMapping("/plans/{recoveryPlanId}")
    public Mono<RecoveryPlanResponse> getPlan(@PathVariable UUID recoveryPlanId) {
        return recoveryPlanService.getPlan(recoveryPlanId);
    }

    @GetMapping("/plans")
    public reactor.core.publisher.Flux<RecoveryPlanResponse> getPlansByStatus(
            @RequestParam(required = false) com.intellisure.recoveryservice.entity.RecoveryPlanStatus status) {
        if (status != null) {
            return recoveryPlanService.getPlansByStatus(status);
        }
        return reactor.core.publisher.Flux.empty();
    }

    @PatchMapping("/plans/{recoveryPlanId}/status")
    public Mono<RecoveryPlanResponse> updatePlanStatus(
            @PathVariable UUID recoveryPlanId,
            @RequestParam com.intellisure.recoveryservice.entity.RecoveryPlanStatus status) {
        return recoveryPlanService.updatePlanStatus(recoveryPlanId, status);
    }
}