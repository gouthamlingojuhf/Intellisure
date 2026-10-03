package com.intellisure.claimsservice.controller;

import com.intellisure.claimsservice.dto.SalvageResponse;
import com.intellisure.claimsservice.dto.CreateSalvageRequest;
import com.intellisure.claimsservice.dto.UpdateSalvageRequest;
import com.intellisure.claimsservice.dto.RecordSalvageSaleRequest;
import com.intellisure.claimsservice.service.SalvageService;
import com.intellisure.claimsservice.mapper.SalvageMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/claims/{claimId}/salvages")
@RequiredArgsConstructor
public class SalvageController {

    private final SalvageService salvageService;
    private final SalvageMapper salvageMapper;

    @PostMapping
    public Mono<ResponseEntity<SalvageResponse>> createSalvage(
            @PathVariable UUID claimId,
            @Valid @RequestBody CreateSalvageRequest request) {
        return salvageService.createSalvage(claimId, request.description(), request.estimatedValue(),
                        request.status(), request.createdBy())
                .map(SalvageMapper.INSTANCE::toResponse)
                .map(ResponseEntity::ok);
    }

    @GetMapping
    public Flux<SalvageResponse> getSalvages(@PathVariable UUID claimId) {
        return salvageService.getSalvages(claimId)
                .map(SalvageMapper.INSTANCE::toResponse);
    }

    @GetMapping("/{salvageId}")
    public Mono<ResponseEntity<SalvageResponse>> getSalvage(@PathVariable UUID claimId, @PathVariable UUID salvageId) {
        return salvageService.getSalvage(salvageId)
                .map(SalvageMapper.INSTANCE::toResponse)
                .map(ResponseEntity::ok);
    }

    @PatchMapping("/{salvageId}")
    public Mono<ResponseEntity<SalvageResponse>> updateSalvage(
            @PathVariable UUID claimId, @PathVariable UUID salvageId,
            @Valid @RequestBody UpdateSalvageRequest request) {
        return salvageService.updateSalvage(salvageId, request.description(), request.estimatedValue(),
                        request.status(), request.buyer(), request.saleDate(), request.saleAmount(), request.updatedBy())
                .map(SalvageMapper.INSTANCE::toResponse)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/{salvageId}/sale")
    public Mono<ResponseEntity<SalvageResponse>> recordSale(
            @PathVariable UUID claimId, @PathVariable UUID salvageId,
            @Valid @RequestBody RecordSalvageSaleRequest request) {
        return salvageService.recordSale(salvageId, request.buyer(), request.saleDate(),
                        request.saleAmount(), request.updatedBy())
                .map(SalvageMapper.INSTANCE::toResponse)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/pending")
    public Flux<SalvageResponse> getPendingSalvages(@PathVariable UUID claimId) {
        return salvageService.getPendingSalvages(claimId)
                .map(SalvageMapper.INSTANCE::toResponse);
    }

    @GetMapping("/sold")
    public Flux<SalvageResponse> getSoldSalvages(@PathVariable UUID claimId) {
        return salvageService.getSoldSalvages(claimId)
                .map(SalvageMapper.INSTANCE::toResponse);
    }
}