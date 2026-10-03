package com.intellisure.quotepolicyservice.controller;

import com.intellisure.quotepolicyservice.dto.PremiumAuditResponse;
import com.intellisure.quotepolicyservice.dto.InitiatePremiumAuditRequest;
import com.intellisure.quotepolicyservice.dto.CompletePremiumAuditRequest;
import com.intellisure.quotepolicyservice.service.PremiumAuditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
public class PremiumAuditController {

    private final PremiumAuditService premiumAuditService;

    @PreAuthorize("hasAnyRole('UNDERWRITER', 'SYSTEM_ADMINISTRATOR')")
    @PostMapping("/{policyId}/audit")
    public Mono<ResponseEntity<PremiumAuditResponse>> initiateAudit(
            @PathVariable UUID policyId,
            @Valid @RequestBody InitiatePremiumAuditRequest request) {
        return premiumAuditService.initiateAudit(policyId, request)
                .map(ResponseEntity::ok);
    }

    @PreAuthorize("hasAnyRole('UNDERWRITER', 'SYSTEM_ADMINISTRATOR')")
    @PostMapping("/{policyId}/audit/{auditId}/complete")
    public Mono<ResponseEntity<PremiumAuditResponse>> completeAudit(
            @PathVariable UUID policyId,
            @PathVariable UUID auditId,
            @Valid @RequestBody CompletePremiumAuditRequest request) {
        return premiumAuditService.completeAudit(auditId, request)
                .map(ResponseEntity::ok);
    }

    @PreAuthorize("hasAnyRole('UNDERWRITER', 'SYSTEM_ADMINISTRATOR')")
    @PostMapping("/{policyId}/audit/{auditId}/cancel")
    public Mono<ResponseEntity<PremiumAuditResponse>> cancelAudit(
            @PathVariable UUID policyId,
            @PathVariable UUID auditId) {
        return premiumAuditService.cancelAudit(auditId)
                .map(ResponseEntity::ok);
    }

    @PreAuthorize("hasAnyRole('UNDERWRITER', 'SYSTEM_ADMINISTRATOR', 'POLICYHOLDER')")
    @GetMapping("/{policyId}/audits")
    public Flux<PremiumAuditResponse> getAudits(@PathVariable UUID policyId) {
        return Flux.empty(); // Implement query method in service
    }
}