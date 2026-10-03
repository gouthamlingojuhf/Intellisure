package com.intellisure.claimsservice.controller;

import com.intellisure.claimsservice.dto.SubrogationResponse;
import com.intellisure.claimsservice.dto.CreateSubrogationRequest;
import com.intellisure.claimsservice.dto.UpdateSubrogationRequest;
import com.intellisure.claimsservice.dto.RecordSubrogationRecoveryRequest;
import com.intellisure.claimsservice.service.SubrogationService;
import com.intellisure.claimsservice.mapper.SubrogationMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/claims/{claimId}/subrogations")
@RequiredArgsConstructor
public class SubrogationController {

    private final SubrogationService subrogationService;
    private final SubrogationMapper subrogationMapper;

    @PostMapping
    public Mono<ResponseEntity<SubrogationResponse>> createSubrogation(
            @PathVariable UUID claimId,
            @Valid @RequestBody CreateSubrogationRequest request) {
        return subrogationService.createSubrogation(claimId, request.thirdPartyName(),
                        request.thirdPartyInsurance(), request.amountClaimed(), request.notes(),
                        request.createdBy())
                .map(SubrogationMapper.INSTANCE::toResponse)
                .map(ResponseEntity::ok);
    }

    @GetMapping
    public Flux<SubrogationResponse> getSubrogations(@PathVariable UUID claimId) {
        return subrogationService.getSubrogations(claimId)
                .map(SubrogationMapper.INSTANCE::toResponse);
    }

    @GetMapping("/{subrogationId}")
    public Mono<ResponseEntity<SubrogationResponse>> getSubrogation(@PathVariable UUID claimId, @PathVariable UUID subrogationId) {
        return subrogationService.getSubrogation(subrogationId)
                .map(SubrogationMapper.INSTANCE::toResponse)
                .map(ResponseEntity::ok);
    }

    @PatchMapping("/{subrogationId}")
    public Mono<ResponseEntity<SubrogationResponse>> updateSubrogation(
            @PathVariable UUID claimId, @PathVariable UUID subrogationId,
            @Valid @RequestBody UpdateSubrogationRequest request) {
        return subrogationService.updateSubrogation(subrogationId, request.thirdPartyName(),
                        request.thirdPartyInsurance(), request.amountClaimed(), request.status(),
                        request.notes(), request.updatedBy())
                .map(SubrogationMapper.INSTANCE::toResponse)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/{subrogationId}/recovery")
    public Mono<ResponseEntity<SubrogationResponse>> recordRecovery(
            @PathVariable UUID claimId, @PathVariable UUID subrogationId,
            @Valid @RequestBody RecordSubrogationRecoveryRequest request) {
        return subrogationService.recordRecovery(subrogationId, request.amountRecovered(), request.updatedBy())
                .map(SubrogationMapper.INSTANCE::toResponse)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/{subrogationId}/close")
    public Mono<ResponseEntity<SubrogationResponse>> closeSubrogation(
            @PathVariable UUID claimId, @PathVariable UUID subrogationId,
            @RequestParam UUID closedBy) {
        return subrogationService.closeSubrogation(subrogationId, closedBy)
                .map(SubrogationMapper.INSTANCE::toResponse)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/open")
    public Flux<SubrogationResponse> getOpenSubrogations(@PathVariable UUID claimId) {
        return subrogationService.getOpenSubrogations(claimId)
                .map(SubrogationMapper.INSTANCE::toResponse);
    }

    @GetMapping("/closed")
    public Flux<SubrogationResponse> getClosedSubrogations(@PathVariable UUID claimId) {
        return subrogationService.getClosedSubrogations(claimId)
                .map(SubrogationMapper.INSTANCE::toResponse);
    }
}