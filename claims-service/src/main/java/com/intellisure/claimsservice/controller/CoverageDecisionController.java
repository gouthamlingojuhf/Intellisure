package com.intellisure.claimsservice.controller;

import com.intellisure.claimsservice.dto.CoverageDecisionResponse;
import com.intellisure.claimsservice.dto.CreateCoverageDecisionRequest;
import com.intellisure.claimsservice.service.CoverageDecisionService;
import com.intellisure.claimsservice.mapper.CoverageDecisionMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/claims/{claimId}/coverage-decisions")
@RequiredArgsConstructor
public class CoverageDecisionController {

    private final CoverageDecisionService decisionService;
    private final CoverageDecisionMapper decisionMapper;

    @PostMapping
    public Mono<ResponseEntity<CoverageDecisionResponse>> createDecision(
            @PathVariable UUID claimId,
            @Valid @RequestBody CreateCoverageDecisionRequest request) {
        return decisionService.createDecision(claimId, request.coverageCode(), request.decision(),
                        request.decisionReason(), request.decidedBy())
                .map(CoverageDecisionMapper.INSTANCE::toResponse)
                .map(ResponseEntity::ok);
    }

    @GetMapping
    public Flux<CoverageDecisionResponse> getDecisions(@PathVariable UUID claimId) {
        return decisionService.getDecisionsByClaim(claimId)
                .map(CoverageDecisionMapper.INSTANCE::toResponse);
    }

    @GetMapping("/{decisionId}")
    public Mono<ResponseEntity<CoverageDecisionResponse>> getDecision(@PathVariable UUID claimId, @PathVariable UUID decisionId) {
        return decisionService.getDecision(decisionId)
                .map(CoverageDecisionMapper.INSTANCE::toResponse)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/coverage/{coverageCode}")
    public Mono<ResponseEntity<CoverageDecisionResponse>> getDecisionByCoverage(
            @PathVariable UUID claimId, @PathVariable String coverageCode) {
        return decisionService.getDecisionsByClaim(claimId)
                .filter(d -> d.getCoverageCode().equals(coverageCode))
                .next()
                .map(CoverageDecisionMapper.INSTANCE::toResponse)
                .map(ResponseEntity::ok);
    }
}