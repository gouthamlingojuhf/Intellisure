package com.intellisure.quotepolicyservice.controller;

import com.intellisure.quotepolicyservice.dto.RenewalTransactionResponse;
import com.intellisure.quotepolicyservice.dto.InitiateRenewalRequest;
import com.intellisure.quotepolicyservice.dto.RenewalDecisionRequest;
import com.intellisure.quotepolicyservice.dto.BindRenewalRequest;
import com.intellisure.quotepolicyservice.service.RenewalService;
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
public class RenewalController {

    private final RenewalService renewalService;

    @PreAuthorize("hasAnyRole('UNDERWRITER', 'SYSTEM_ADMINISTRATOR')")
    @PostMapping("/{policyId}/renewal")
    public Mono<ResponseEntity<RenewalTransactionResponse>> initiateRenewal(
            @PathVariable UUID policyId,
            @Valid @RequestBody InitiateRenewalRequest request) {
        return renewalService.initiateRenewal(policyId, request)
                .map(ResponseEntity::ok);
    }

    @PreAuthorize("hasAnyRole('UNDERWRITER', 'SYSTEM_ADMINISTRATOR')")
    @PostMapping("/{policyId}/renewal/{renewalId}/decide")
    public Mono<ResponseEntity<RenewalTransactionResponse>> decideRenewal(
            @PathVariable UUID policyId,
            @PathVariable UUID renewalId,
            @Valid @RequestBody RenewalDecisionRequest request) {
        return renewalService.decideRenewal(renewalId, request)
                .map(ResponseEntity::ok);
    }

    @PreAuthorize("hasAnyRole('UNDERWRITER', 'SYSTEM_ADMINISTRATOR')")
    @PostMapping("/{policyId}/renewal/{renewalId}/bind")
    public Mono<ResponseEntity<RenewalTransactionResponse>> bindRenewal(
            @PathVariable UUID policyId,
            @PathVariable UUID renewalId,
            @Valid @RequestBody BindRenewalRequest request) {
        return renewalService.bindRenewal(renewalId, request)
                .map(ResponseEntity::ok);
    }

    @PreAuthorize("hasAnyRole('UNDERWRITER', 'SYSTEM_ADMINISTRATOR')")
    @PostMapping("/{policyId}/renewal/{renewalId}/issue")
    public Mono<ResponseEntity<RenewalTransactionResponse>> issueRenewal(
            @PathVariable UUID policyId,
            @PathVariable UUID renewalId) {
        return renewalService.issueRenewal(renewalId)
                .map(ResponseEntity::ok);
    }

    @PreAuthorize("hasAnyRole('UNDERWRITER', 'SYSTEM_ADMINISTRATOR', 'POLICYHOLDER')")
    @GetMapping("/{policyId}/renewals")
    public Flux<RenewalTransactionResponse> getRenewals(@PathVariable UUID policyId) {
        return Flux.empty(); // Implement query method in service
    }
}