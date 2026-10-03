package com.intellisure.claimsservice.service;

import com.intellisure.claimsservice.entity.BusinessIncome;
import com.intellisure.claimsservice.entity.Claim;
import com.intellisure.claimsservice.exception.ResourceNotFoundException;
import com.intellisure.claimsservice.exception.BusinessException;
import com.intellisure.claimsservice.repository.BusinessIncomeRepository;
import com.intellisure.claimsservice.repository.ClaimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BusinessIncomeService {

    private final BusinessIncomeRepository businessIncomeRepository;
    private final ClaimRepository claimRepository;
    private final R2dbcEntityTemplate entityTemplate;

    public Mono<BusinessIncome> getBusinessIncome(UUID claimId) {
        return businessIncomeRepository.findByClaimId(claimId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Business income not found for claim: " + claimId)));
    }

    public Mono<BusinessIncome> createBusinessIncome(UUID claimId, java.math.BigDecimal coverageLimit,
                                                     Integer waitingPeriodDays, Integer restorationPeriodDays,
                                                     LocalDate periodStart, LocalDate periodEnd,
                                                     UUID createdBy) {
        return claimRepository.findById(claimId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Claim not found: " + claimId)))
                .flatMap(claim -> businessIncomeRepository.findByClaimId(claimId)
                        .switchIfEmpty(Mono.defer(() -> {
                            BusinessIncome bi = BusinessIncome.builder()
                                    .businessIncomeId(UUID.randomUUID())
                                    .claimId(claimId)
                                    .coverageLimit(coverageLimit)
                                    .waitingPeriodDays(waitingPeriodDays)
                                    .restorationPeriodDays(restorationPeriodDays)
                                    .periodOfIndemnityStart(periodStart)
                                    .periodOfIndemnityEnd(periodEnd)
                                    .estimatedLoss(BigDecimal.ZERO)
                                    .actualLoss(BigDecimal.ZERO)
                                    .coverageConfirmed(false)
                                    .createdAt(LocalDateTime.now())
                                    .updatedAt(LocalDateTime.now())
                                    .build();
                            return entityTemplate.insert(bi).thenReturn(bi);
                        })));
    }

    public Mono<BusinessIncome> updateBusinessIncome(UUID claimId, java.math.BigDecimal coverageLimit,
                                                     Integer waitingPeriodDays, Integer restorationPeriodDays,
                                                     LocalDate periodStart, LocalDate periodEnd,
                                                     UUID updatedBy) {
        return businessIncomeRepository.findByClaimId(claimId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Business income not found for claim: " + claimId)))
                .flatMap(bi -> {
                    if (coverageLimit != null) bi.setCoverageLimit(coverageLimit);
                    if (waitingPeriodDays != null) bi.setWaitingPeriodDays(waitingPeriodDays);
                    if (restorationPeriodDays != null) bi.setRestorationPeriodDays(restorationPeriodDays);
                    if (periodStart != null) bi.setPeriodOfIndemnityStart(periodStart);
                    if (periodEnd != null) bi.setPeriodOfIndemnityEnd(periodEnd);
                    bi.setUpdatedAt(LocalDateTime.now());
                    return entityTemplate.update(bi).thenReturn(bi);
                });
    }

    public Mono<BusinessIncome> recordLoss(UUID claimId, java.math.BigDecimal estimatedLoss, UUID updatedBy) {
        return businessIncomeRepository.findByClaimId(claimId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Business income not found for claim: " + claimId)))
                .flatMap(bi -> {
                    bi.setEstimatedLoss(estimatedLoss);
                    bi.setUpdatedAt(LocalDateTime.now());
                    return entityTemplate.update(bi).thenReturn(bi);
                });
    }

    public Mono<BusinessIncome> recordActualLoss(UUID claimId, java.math.BigDecimal actualLoss, UUID updatedBy) {
        return businessIncomeRepository.findByClaimId(claimId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Business income not found for claim: " + claimId)))
                .flatMap(bi -> {
                    bi.setActualLoss(actualLoss);
                    bi.setUpdatedAt(LocalDateTime.now());
                    return entityTemplate.update(bi).thenReturn(bi);
                });
    }

    public Mono<BusinessIncome> confirmCoverage(UUID claimId, UUID updatedBy) {
        return businessIncomeRepository.findByClaimId(claimId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Business income not found for claim: " + claimId)))
                .flatMap(bi -> {
                    bi.setCoverageConfirmed(true);
                    bi.setUpdatedAt(LocalDateTime.now());
                    return entityTemplate.update(bi).thenReturn(bi);
                });
    }

    public Mono<BusinessIncome> calculateLoss(UUID claimId, java.math.BigDecimal grossRevenue, Integer daysAffected,
                                              UUID updatedBy) {
        return businessIncomeRepository.findByClaimId(claimId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Business income not found for claim: " + claimId)))
                .flatMap(bi -> {
                    if (bi.getWaitingPeriodDays() == null || bi.getRestorationPeriodDays() == null) {
                        return Mono.error(new BusinessException("Waiting period and restoration period must be set"));
                    }
                    // Simple calculation: (grossRevenue / 365) * daysAffected * (restorationPeriod / waitingPeriod)
                    BigDecimal dailyRevenue = grossRevenue.divide(new BigDecimal("365"), 2, BigDecimal.ROUND_HALF_UP);
                    BigDecimal loss = dailyRevenue.multiply(new BigDecimal(daysAffected));
                    bi.setEstimatedLoss(loss);
                    bi.setUpdatedAt(LocalDateTime.now());
                    return entityTemplate.update(bi).thenReturn(bi);
                });
    }
}