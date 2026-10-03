package com.intellisure.claimsservice.service;

import com.intellisure.claimsservice.entity.Salvage;
import com.intellisure.claimsservice.entity.Claim;
import com.intellisure.claimsservice.exception.ResourceNotFoundException;
import com.intellisure.claimsservice.exception.BusinessException;
import com.intellisure.claimsservice.repository.SalvageRepository;
import com.intellisure.claimsservice.repository.ClaimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SalvageService {

    private final SalvageRepository salvageRepository;
    private final ClaimRepository claimRepository;
    private final R2dbcEntityTemplate entityTemplate;

    public Flux<Salvage> getSalvages(UUID claimId) {
        return salvageRepository.findAllByClaimId(claimId);
    }

    public Mono<Salvage> getSalvage(UUID salvageId) {
        return salvageRepository.findById(salvageId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Salvage not found: " + salvageId)));
    }

    public Mono<Salvage> createSalvage(UUID claimId, String description, java.math.BigDecimal estimatedValue,
                                     String status, UUID createdBy) {
        return claimRepository.findById(claimId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Claim not found: " + claimId)))
                .flatMap(claim -> {
                    Salvage salvage = new Salvage();
                    salvage.setSalvageId(UUID.randomUUID());
                    salvage.setClaimId(claimId);
                    salvage.setDescription(description);
                    salvage.setEstimatedValue(estimatedValue != null ? estimatedValue : BigDecimal.ZERO);
                    salvage.setActualValue(BigDecimal.ZERO);
                    salvage.setStatus(status != null ? status : "PENDING");
                    salvage.setCreatedAt(LocalDateTime.now());
                    salvage.setUpdatedAt(LocalDateTime.now());
                    return entityTemplate.insert(salvage).thenReturn(salvage);
                });
    }

    public Mono<Salvage> updateSalvage(UUID salvageId, String description, java.math.BigDecimal estimatedValue,
                                     String status, String buyer, LocalDate saleDate, java.math.BigDecimal saleAmount,
                                     UUID updatedBy) {
        return salvageRepository.findById(salvageId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Salvage not found: " + salvageId)))
                .flatMap(salvage -> {
                    if (description != null) salvage.setDescription(description);
                    if (estimatedValue != null) salvage.setEstimatedValue(estimatedValue);
                    if (status != null) salvage.setStatus(status);
                    if (buyer != null) salvage.setBuyer(buyer);
                    if (saleDate != null) salvage.setSaleDate(saleDate);
                    if (saleAmount != null) salvage.setSaleAmount(saleAmount);
                    salvage.setUpdatedAt(LocalDateTime.now());
                    return entityTemplate.update(salvage).thenReturn(salvage);
                });
    }

    public Mono<Salvage> recordSale(UUID salvageId, String buyer, LocalDate saleDate, java.math.BigDecimal saleAmount,
                                    UUID updatedBy) {
        return salvageRepository.findById(salvageId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Salvage not found: " + salvageId)))
                .flatMap(salvage -> {
                    salvage.setBuyer(buyer);
                    salvage.setSaleDate(saleDate);
                    salvage.setSaleAmount(saleAmount);
                    salvage.setActualValue(saleAmount);
                    salvage.setStatus("SOLD");
                    salvage.setUpdatedAt(LocalDateTime.now());
                    return entityTemplate.update(salvage).thenReturn(salvage);
                });
    }

    public Flux<Salvage> getPendingSalvages(UUID claimId) {
        return salvageRepository.findByClaimIdAndStatus(claimId, "PENDING");
    }

    public Flux<Salvage> getSoldSalvages(UUID claimId) {
        return salvageRepository.findByClaimIdAndStatus(claimId, "SOLD");
    }
}