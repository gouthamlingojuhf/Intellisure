package com.intellisure.quotepolicyservice.controller;

import com.intellisure.quotepolicyservice.dto.EndorsementResponse;
import com.intellisure.quotepolicyservice.dto.RequestEndorsementRequest;
import com.intellisure.quotepolicyservice.service.EndorsementService;
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
public class EndorsementController {

    private final EndorsementService endorsementService;

    @PreAuthorize("hasAnyRole('UNDERWRITER', 'SYSTEM_ADMINISTRATOR')")
    @PostMapping("/{policyId}/endorsements")
    public Mono<ResponseEntity<EndorsementResponse>> requestEndorsement(
            @PathVariable UUID policyId,
            @Valid @RequestBody RequestEndorsementRequest request) {
        return endorsementService.requestEndorsement(policyId, request)
                .map(ResponseEntity::ok);
    }

    @PreAuthorize("hasAnyRole('UNDERWRITER', 'SYSTEM_ADMINISTRATOR')")
    @PostMapping("/{policyId}/endorsements/{endorsementId}/approve")
    public Mono<ResponseEntity<EndorsementResponse>> approveEndorsement(
            @PathVariable UUID policyId,
            @PathVariable UUID endorsementId,
            @RequestParam UUID approvedByUserId) {
        return endorsementService.approveEndorsement(endorsementId, approvedByUserId)
                .map(ResponseEntity::ok);
    }

    @PreAuthorize("hasAnyRole('UNDERWRITER', 'SYSTEM_ADMINISTRATOR')")
    @PostMapping("/{policyId}/endorsements/{endorsementId}/reject")
    public Mono<ResponseEntity<EndorsementResponse>> rejectEndorsement(
            @PathVariable UUID policyId,
            @PathVariable UUID endorsementId,
            @RequestParam String decisionReason,
            @RequestParam UUID rejectedByUserId) {
        return endorsementService.rejectEndorsement(endorsementId, decisionReason, rejectedByUserId)
                .map(ResponseEntity::ok);
    }

    @PreAuthorize("hasAnyRole('UNDERWRITER', 'SYSTEM_ADMINISTRATOR')")
    @PostMapping("/{policyId}/endorsements/{endorsementId}/issue")
    public Mono<ResponseEntity<EndorsementResponse>> issueEndorsement(
            @PathVariable UUID policyId,
            @PathVariable UUID endorsementId) {
        return endorsementService.issueEndorsement(endorsementId)
                .map(ResponseEntity::ok);
    }

    @PreAuthorize("hasAnyRole('UNDERWRITER', 'SYSTEM_ADMINISTRATOR')")
    @PostMapping("/{policyId}/endorsements/{endorsementId}/apply")
    public Mono<ResponseEntity<Void>> applyEndorsement(
            @PathVariable UUID policyId,
            @PathVariable UUID endorsementId) {
        return endorsementService.applyEndorsementToPolicy(endorsementId)
                .then(Mono.just(ResponseEntity.ok().build()));
    }

    @PreAuthorize("hasAnyRole('UNDERWRITER', 'SYSTEM_ADMINISTRATOR', 'POLICYHOLDER')")
    @GetMapping("/{policyId}/endorsements")
    public Flux<EndorsementResponse> getEndorsements(@PathVariable UUID policyId) {
        return endorsementService.getEndorsements(policyId);
    }
}
