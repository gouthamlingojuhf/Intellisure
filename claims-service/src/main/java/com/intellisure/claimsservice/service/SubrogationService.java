package com.intellisure.claimsservice.service;

import com.intellisure.claimsservice.entity.Subrogation;
import com.intellisure.claimsservice.entity.Claim;
import com.intellisure.claimsservice.exception.ResourceNotFoundException;
import com.intellisure.claimsservice.exception.BusinessException;
import com.intellisure.claimsservice.repository.SubrogationRepository;
import com.intellisure.claimsservice.repository.ClaimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubrogationService {

    private final SubrogationRepository subrogationRepository;
    private final ClaimRepository claimRepository;
    private final R2dbcEntityTemplate entityTemplate;

    public Flux<Subrogation> getSubrogations(UUID claimId) {
        return subrogationRepository.findAllByClaimId(claimId);
    }

    public Mono<Subrogation> getSubrogation(UUID subrogationId) {
        return subrogationRepository.findById(subrogationId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Subrogation not found: " + subrogationId)));
    }

    public Mono<Subrogation> createSubrogation(UUID claimId, String thirdPartyName, String thirdPartyInsurance,
                                               java.math.BigDecimal amountClaimed, String notes, UUID createdBy) {
        return claimRepository.findById(claimId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Claim not found: " + claimId)))
                .flatMap(claim -> {
                    Subrogation subrogation = new Subrogation();
                    subrogation.setSubrogationId(UUID.randomUUID());
                    subrogation.setClaimId(claimId);
                    subrogation.setThirdPartyName(thirdPartyName);
                    subrogation.setThirdPartyInsurance(thirdPartyInsurance);
                    subrogation.setAmountClaimed(amountClaimed);
                    subrogation.setAmountRecovered(BigDecimal.ZERO);
                    subrogation.setStatus("OPEN");
                    subrogation.setNotes(notes);
                    subrogation.setCreatedAt(LocalDateTime.now());
                    subrogation.setUpdatedAt(LocalDateTime.now());
                    return entityTemplate.insert(subrogation).thenReturn(subrogation);
                });
    }

    public Mono<Subrogation> updateSubrogation(UUID subrogationId, String thirdPartyName,
                                               String thirdPartyInsurance, java.math.BigDecimal amountClaimed,
                                               String status, String notes, UUID updatedBy) {
        return subrogationRepository.findById(subrogationId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Subrogation not found: " + subrogationId)))
                .flatMap(subrogation -> {
                    if (thirdPartyName != null) subrogation.setThirdPartyName(thirdPartyName);
                    if (thirdPartyInsurance != null) subrogation.setThirdPartyInsurance(thirdPartyInsurance);
                    if (amountClaimed != null) subrogation.setAmountClaimed(amountClaimed);
                    if (status != null) subrogation.setStatus(status);
                    if (notes != null) subrogation.setNotes(notes);
                    subrogation.setUpdatedAt(LocalDateTime.now());
                    return entityTemplate.update(subrogation).thenReturn(subrogation);
                });
    }

    public Mono<Subrogation> recordRecovery(UUID subrogationId, java.math.BigDecimal amountRecovered, UUID updatedBy) {
        return subrogationRepository.findById(subrogationId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Subrogation not found: " + subrogationId)))
                .flatMap(subrogation -> {
                    subrogation.setAmountRecovered(amountRecovered);
                    if (subrogation.getAmountClaimed().compareTo(subrogation.getAmountRecovered()) <= 0) {
                        subrogation.setStatus("CLOSED");
                    } else {
                        subrogation.setStatus("PARTIAL");
                    }
                    subrogation.setUpdatedAt(LocalDateTime.now());
                    return entityTemplate.update(subrogation).thenReturn(subrogation);
                });
    }

    public Flux<Subrogation> getOpenSubrogations(UUID claimId) {
        return subrogationRepository.findByClaimIdAndStatus(claimId, "OPEN");
    }

    public Flux<Subrogation> getClosedSubrogations(UUID claimId) {
        return subrogationRepository.findByClaimIdAndStatus(claimId, "CLOSED");
    }

    public Mono<Subrogation> closeSubrogation(UUID subrogationId, UUID closedBy) {
        return subrogationRepository.findById(subrogationId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Subrogation not found: " + subrogationId)))
                .flatMap(subrogation -> {
                    subrogation.setStatus("CLOSED");
                    subrogation.setUpdatedAt(LocalDateTime.now());
                    return entityTemplate.update(subrogation).thenReturn(subrogation);
                });
    }
}