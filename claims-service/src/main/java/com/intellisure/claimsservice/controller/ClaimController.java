package com.intellisure.claimsservice.controller;

import com.intellisure.claimsservice.dto.ClaimResponse;
import com.intellisure.claimsservice.dto.FileClaimRequest;
import com.intellisure.claimsservice.service.ClaimService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;

    @PostMapping
    public Mono<ClaimResponse> fileClaim(
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @Valid @RequestBody FileClaimRequest request) {
        
        if (userId == null) {
            userId = UUID.randomUUID();
        }
        
        return claimService.fileClaim(request, userId);
    }

    @PostMapping("/fnol")
    public Mono<ClaimResponse> fnol(@RequestHeader(value = "X-User-Id", required = false) UUID userId,
                                    @Valid @RequestBody FileClaimRequest request) {
        return fileClaim(userId, request);
    }

    @GetMapping
    public Flux<ClaimResponse> claims(@RequestParam(required = false) UUID customerId) {
        return claimService.getClaims(customerId);
    }

    @GetMapping("/{claimId}")
    public Mono<ClaimResponse> get(@PathVariable UUID claimId) {
        return claimService.getClaim(claimId);
    }

    @PatchMapping("/{claimId}/status")
    public Mono<ClaimResponse> status(@PathVariable UUID claimId, @RequestParam String value) {
        return claimService.updateStatus(claimId, value);
    }
}
