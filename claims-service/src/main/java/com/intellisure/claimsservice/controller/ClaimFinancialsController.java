package com.intellisure.claimsservice.controller;

import com.intellisure.claimsservice.dto.ClaimFinancialsResponse;
import com.intellisure.claimsservice.dto.InitiateFinancialsRequest;
import com.intellisure.claimsservice.dto.UpdateReserveRequest;
import com.intellisure.claimsservice.dto.RecordPaymentRequest;
import com.intellisure.claimsservice.dto.UpdateIncurredRequest;
import com.intellisure.claimsservice.service.ClaimFinancialsService;
import com.intellisure.claimsservice.mapper.ClaimFinancialsMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/claims/{claimId}/financials")
@RequiredArgsConstructor
public class ClaimFinancialsController {

    private final ClaimFinancialsService financialsService;
    private final ClaimFinancialsMapper financialsMapper;

    @PostMapping
    public Mono<ResponseEntity<ClaimFinancialsResponse>> initializeFinancials(
            @PathVariable UUID claimId,
            @Valid @RequestBody InitiateFinancialsRequest request) {
        return financialsService.initializeFinancials(claimId, request.reserveAmount(), request.updatedBy())
                .map(financialsMapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @GetMapping
    public Mono<ResponseEntity<ClaimFinancialsResponse>> getFinancials(@PathVariable UUID claimId) {
        return financialsService.getFinancials(claimId)
                .map(financialsMapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @PatchMapping("/reserve")
    public Mono<ResponseEntity<ClaimFinancialsResponse>> updateReserve(
            @PathVariable UUID claimId,
            @Valid @RequestBody UpdateReserveRequest request) {
        return financialsService.updateReserve(claimId, request.reserveAmount(), request.reason(), request.updatedBy())
                .map(financialsMapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/payment")
    public Mono<ResponseEntity<ClaimFinancialsResponse>> recordPayment(
            @PathVariable UUID claimId,
            @Valid @RequestBody RecordPaymentRequest request) {
        return financialsService.recordPayment(claimId, request.amount(), request.updatedBy())
                .map(financialsMapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @PatchMapping("/incurred")
    public Mono<ResponseEntity<ClaimFinancialsResponse>> updateIncurred(
            @PathVariable UUID claimId,
            @Valid @RequestBody UpdateIncurredRequest request) {
        return financialsService.updateIncurred(claimId, request.incurredAmount(), request.updatedBy())
                .map(financialsMapper::toResponse)
                .map(ResponseEntity::ok);
    }
}