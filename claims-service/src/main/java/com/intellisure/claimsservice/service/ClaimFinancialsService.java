package com.intellisure.claimsservice.service;

import com.intellisure.claimsservice.entity.ClaimFinancials;
import com.intellisure.claimsservice.exception.ResourceNotFoundException;
import com.intellisure.claimsservice.exception.BusinessException;
import com.intellisure.claimsservice.repository.ClaimFinancialsRepository;
import com.intellisure.claimsservice.repository.ClaimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClaimFinancialsService {

    private final ClaimFinancialsRepository financialsRepository;
    private final ClaimRepository claimRepository;
    private final R2dbcEntityTemplate entityTemplate;

    public Mono<ClaimFinancials> getFinancials(UUID claimId) {
        return financialsRepository.findByClaimId(claimId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Financials not found for claim: " + claimId)));
    }

    public Mono<ClaimFinancials> initializeFinancials(UUID claimId, BigDecimal initialReserve, UUID updatedBy) {
        return claimRepository.findById(claimId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Claim not found: " + claimId)))
                .flatMap(claim -> financialsRepository.findByClaimId(claimId)
                        .switchIfEmpty(Mono.defer(() -> {
                            ClaimFinancials financials = ClaimFinancials.builder()
                                    .financialId(UUID.randomUUID())
                                    .claimId(claimId)
                                    .reserveAmount(initialReserve != null ? initialReserve : BigDecimal.ZERO)
                                    .totalIncurred(BigDecimal.ZERO)
                                    .paidAmount(BigDecimal.ZERO)
                                    .outstandingReserve(initialReserve != null ? initialReserve : BigDecimal.ZERO)
                                    .lastUpdatedBy(updatedBy)
                                    .lastUpdatedAt(LocalDateTime.now())
                                    .createdAt(LocalDateTime.now())
                                    .updatedAt(LocalDateTime.now())
                                    .build();
                            return entityTemplate.insert(financials).thenReturn(financials);
                        })));
    }

    public Mono<ClaimFinancials> updateReserve(UUID claimId, BigDecimal newReserve, String reason, UUID updatedBy) {
        return financialsRepository.findByClaimId(claimId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Financials not found for claim: " + claimId)))
                .flatMap(financials -> {
                    financials.setReserveAmount(newReserve);
                    financials.setOutstandingReserve(newReserve.subtract(financials.getPaidAmount()));
                    financials.setTotalIncurred(financials.getTotalIncurred().add(newReserve.subtract(financials.getReserveAmount())));
                    financials.setLastUpdatedBy(updatedBy);
                    financials.setLastUpdatedAt(LocalDateTime.now());
                    financials.setUpdatedAt(LocalDateTime.now());
                    return entityTemplate.update(financials).thenReturn(financials);
                });
    }

    public Mono<ClaimFinancials> recordPayment(UUID claimId, BigDecimal amount, UUID updatedBy) {
        return financialsRepository.findByClaimId(claimId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Financials not found for claim: " + claimId)))
                .flatMap(financials -> {
                    BigDecimal newPaid = financials.getPaidAmount().add(amount);
                    if (newPaid.compareTo(financials.getTotalIncurred()) > 0) {
                        return Mono.error(new BusinessException("Payment amount exceeds total incurred"));
                    }
                    financials.setPaidAmount(newPaid);
                    financials.setOutstandingReserve(financials.getReserveAmount().subtract(newPaid));
                    financials.setLastUpdatedBy(updatedBy);
                    financials.setLastUpdatedAt(LocalDateTime.now());
                    financials.setUpdatedAt(LocalDateTime.now());
                    return entityTemplate.update(financials).thenReturn(financials);
                });
    }

    public Mono<ClaimFinancials> updateIncurred(UUID claimId, BigDecimal newIncurred, UUID updatedBy) {
        return financialsRepository.findByClaimId(claimId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Financials not found for claim: " + claimId)))
                .flatMap(financials -> {
                    financials.setTotalIncurred(newIncurred);
                    financials.setOutstandingReserve(financials.getReserveAmount().subtract(financials.getPaidAmount()));
                    financials.setLastUpdatedBy(updatedBy);
                    financials.setLastUpdatedAt(LocalDateTime.now());
                    financials.setUpdatedAt(LocalDateTime.now());
                    return entityTemplate.update(financials).thenReturn(financials);
                });
    }
}