package com.intellisure.claimsservice.controller;

import com.intellisure.claimsservice.dto.BusinessIncomeResponse;
import com.intellisure.claimsservice.dto.CreateBusinessIncomeRequest;
import com.intellisure.claimsservice.dto.UpdateBusinessIncomeRequest;
import com.intellisure.claimsservice.dto.RecordBusinessIncomeLossRequest;
import com.intellisure.claimsservice.dto.CalculateBusinessIncomeLossRequest;
import com.intellisure.claimsservice.service.BusinessIncomeService;
import com.intellisure.claimsservice.mapper.BusinessIncomeMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/claims/{claimId}/business-income")
@RequiredArgsConstructor
public class BusinessIncomeController {

    private final BusinessIncomeService businessIncomeService;
    private final com.intellisure.claimsservice.mapper.BusinessIncomeMapper businessIncomeMapper;

    @PostMapping
    public Mono<ResponseEntity<BusinessIncomeResponse>> createBusinessIncome(
            @PathVariable UUID claimId,
            @Valid @RequestBody CreateBusinessIncomeRequest request) {
        return businessIncomeService.createBusinessIncome(claimId, request.coverageLimit(),
                        request.waitingPeriodDays(), request.restorationPeriodDays(),
                        request.periodStart(), request.periodEnd(), request.createdBy())
                .map(businessIncomeMapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @GetMapping
    public Mono<ResponseEntity<BusinessIncomeResponse>> getBusinessIncome(@PathVariable UUID claimId) {
        return businessIncomeService.getBusinessIncome(claimId)
                .map(com.intellisure.claimsservice.mapper.BusinessIncomeMapper.INSTANCE::toResponse)
                .map(ResponseEntity::ok);
    }

    @PatchMapping
    public Mono<ResponseEntity<BusinessIncomeResponse>> updateBusinessIncome(
            @PathVariable UUID claimId,
            @Valid @RequestBody UpdateBusinessIncomeRequest request) {
        return businessIncomeService.updateBusinessIncome(claimId, request.coverageLimit(),
                        request.waitingPeriodDays(), request.restorationPeriodDays(),
                        request.periodStart(), request.periodEnd(), request.updatedBy())
                .map(com.intellisure.claimsservice.mapper.BusinessIncomeMapper.INSTANCE::toResponse)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/loss")
    public Mono<ResponseEntity<BusinessIncomeResponse>> recordLoss(
            @PathVariable UUID claimId,
            @Valid @RequestBody RecordBusinessIncomeLossRequest request) {
        return businessIncomeService.recordLoss(claimId, request.estimatedLoss(), request.updatedBy())
                .map(com.intellisure.claimsservice.mapper.BusinessIncomeMapper.INSTANCE::toResponse)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/actual-loss")
    public Mono<ResponseEntity<BusinessIncomeResponse>> recordActualLoss(
            @PathVariable UUID claimId,
            @Valid @RequestBody RecordBusinessIncomeLossRequest request) {
        return businessIncomeService.recordActualLoss(claimId, request.estimatedLoss(), request.updatedBy())
                .map(com.intellisure.claimsservice.mapper.BusinessIncomeMapper.INSTANCE::toResponse)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/confirm-coverage")
    public Mono<ResponseEntity<BusinessIncomeResponse>> confirmCoverage(
            @PathVariable UUID claimId,
            @RequestParam UUID updatedBy) {
        return businessIncomeService.confirmCoverage(claimId, updatedBy)
                .map(com.intellisure.claimsservice.mapper.BusinessIncomeMapper.INSTANCE::toResponse)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/calculate-loss")
    public Mono<ResponseEntity<BusinessIncomeResponse>> calculateLoss(
            @PathVariable UUID claimId,
            @Valid @RequestBody CalculateBusinessIncomeLossRequest request) {
        return businessIncomeService.calculateLoss(claimId, request.grossRevenue(), request.daysAffected(),
                        request.updatedBy())
                .map(com.intellisure.claimsservice.mapper.BusinessIncomeMapper.INSTANCE::toResponse)
                .map(ResponseEntity::ok);
    }
}