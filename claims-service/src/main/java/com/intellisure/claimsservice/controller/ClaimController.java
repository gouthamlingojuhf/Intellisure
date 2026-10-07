package com.intellisure.claimsservice.controller;

import com.intellisure.claimsservice.dto.ClaimResponse;
import com.intellisure.claimsservice.dto.FileClaimRequest;
import com.intellisure.claimsservice.service.ClaimService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;

    @PostMapping
    public Mono<ClaimResponse> fileClaim(
            @Valid @RequestBody FileClaimRequest request) {
        return claimService.fileClaim(request);
    }

    @PostMapping("/fnol")
    public Mono<ClaimResponse> fnol(@Valid @RequestBody FileClaimRequest request) {
        return fileClaim(request);
    }

    @GetMapping
    public Flux<ClaimResponse> claims(@RequestParam(required = false) UUID customerId,
                                     @RequestParam(required = false) String status) {
        return claimService.getClaimsForCaller(customerId, status);
    }

    @GetMapping("/{claimId}")
    public Mono<ClaimResponse> get(@PathVariable UUID claimId) {
        return claimService.getClaim(claimId);
    }

    @GetMapping("/number/{claimNumber}")
    public Mono<ClaimResponse> getByClaimNumber(@PathVariable String claimNumber) {
        return claimService.getClaimByNumber(claimNumber);
    }

    @PatchMapping("/{claimId}/status")
    public Mono<ClaimResponse> status(@PathVariable UUID claimId, @RequestParam String value) {
        return claimService.updateStatus(claimId, value);
    }

    @PatchMapping("/{claimId}/assign-adjuster")
    public Mono<ClaimResponse> assignAdjuster(@PathVariable UUID claimId, @RequestBody Map<String, UUID> body) {
        UUID adjusterId = body.get("adjusterId");
        if (adjusterId == null) {
            throw new IllegalArgumentException("adjusterId is required");
        }
        return claimService.assignAdjuster(claimId, adjusterId);
    }

    @PatchMapping("/{claimId}/resign-adjuster")
    public Mono<ClaimResponse> resignAdjuster(@PathVariable UUID claimId) {
        return claimService.resignAdjuster(claimId);
    }

    @PostMapping("/{claimId}/assessment")
    public Mono<ClaimResponse> createAssessment(
            @PathVariable UUID claimId,
            @RequestParam String causeOfLoss,
            @RequestParam String findings,
            @RequestParam boolean covered,
            @RequestParam BigDecimal totalLossAmount,
            @RequestParam BigDecimal coveredLossAmount,
            @RequestParam BigDecimal deductibleApplied,
            @RequestParam BigDecimal netLossAmount,
            @RequestParam(required = false) UUID assessorId) {
        return claimService.createAssessment(claimId, causeOfLoss, findings, covered,
                totalLossAmount, coveredLossAmount, deductibleApplied, netLossAmount,
                assessorId == null ? UUID.randomUUID() : assessorId);
    }

    @PostMapping("/{claimId}/reserve")
    public Mono<ClaimResponse> createReserve(@PathVariable UUID claimId,
                                           @RequestParam BigDecimal reserve,
                                           @RequestParam String reserveReason,
                                           @RequestParam(required = false) UUID updatedBy) {
        return claimService.createReserve(claimId, reserve, reserveReason, updatedBy == null ? UUID.randomUUID() : updatedBy);
    }

    @PostMapping("/{claimId}/payout")
    public Mono<ClaimResponse> calculatePayout(@PathVariable UUID claimId,
                                             @RequestParam BigDecimal coveredLoss,
                                             @RequestParam BigDecimal deductible,
                                             @RequestParam BigDecimal policyLimit) {
        return claimService.calculatePayout(claimId, coveredLoss, deductible, policyLimit);
    }

    @PostMapping("/{claimId}/decision")
    public Mono<ClaimResponse> recordDecision(@PathVariable UUID claimId,
                                            @RequestParam String decision,
                                            @RequestParam(required = false) String reason,
                                            @RequestParam(required = false) UUID decidedBy) {
        return claimService.recordClaimDecision(claimId, decision, reason, decidedBy == null ? UUID.randomUUID() : decidedBy);
    }

    @PatchMapping("/{claimId}/settlement/approve")
    public Mono<ClaimResponse> approveSettlement(@PathVariable UUID claimId,
                                               @RequestParam UUID approvedBy,
                                               @RequestParam BigDecimal approvedAmount) {
        return claimService.approveSettlement(claimId, approvedBy, approvedAmount);
    }

    @PatchMapping("/{claimId}/payment")
    public Mono<ClaimResponse> recordPayment(@PathVariable UUID claimId,
                                            @RequestParam String paymentReference) {
        return claimService.recordPayment(claimId, paymentReference);
    }

    @PatchMapping("/{claimId}/close")
    public Mono<ClaimResponse> closeClaim(@PathVariable UUID claimId,
                                        @RequestParam String reason) {
        return claimService.closeClaim(claimId, reason);
    }

    @PostMapping("/{claimId}/close")
    public Mono<ClaimResponse> closeClaimPost(@PathVariable UUID claimId,
                                            @RequestParam String reason) {
        return claimService.closeClaim(claimId, reason);
    }
}
