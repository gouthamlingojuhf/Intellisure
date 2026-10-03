package com.intellisure.claimsservice.service;

import com.intellisure.claimsservice.entity.CoverageDecision;
import com.intellisure.claimsservice.entity.Claim;
import com.intellisure.claimsservice.exception.ResourceNotFoundException;
import com.intellisure.claimsservice.exception.BusinessException;
import com.intellisure.claimsservice.repository.CoverageDecisionRepository;
import com.intellisure.claimsservice.repository.ClaimRepository;
import com.intellisure.claimsservice.repository.ClaimFinancialsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CoverageDecisionService {

    private final CoverageDecisionRepository decisionRepository;
    private final ClaimRepository claimRepository;
    private final ClaimFinancialsRepository financialsRepository;
    private final R2dbcEntityTemplate entityTemplate;

    public Flux<CoverageDecision> getDecisions(UUID claimId) {
        return decisionRepository.findAllByClaimId(claimId);
    }

    public Mono<CoverageDecision> getDecision(UUID decisionId) {
        return decisionRepository.findById(decisionId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Coverage decision not found: " + decisionId)));
    }

    public Mono<CoverageDecision> createDecision(UUID claimId, String coverageCode, String decision,
                                                 String decisionReason, UUID decidedBy) {
        return claimRepository.findById(claimId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Claim not found: " + claimId)))
                .flatMap(claim -> decisionRepository.findByClaimIdAndCoverageCode(claimId, coverageCode)
                        .switchIfEmpty(Mono.defer(() -> {
                            CoverageDecision newDecision = new CoverageDecision();
                            newDecision.setDecisionId(UUID.randomUUID());
                            newDecision.setClaimId(claimId);
                            newDecision.setCoverageCode(coverageCode);
                            newDecision.setDecision(decision);
                            newDecision.setDecisionReason(decisionReason);
                            newDecision.setDecidedBy(decidedBy);
                            newDecision.setDecidedAt(LocalDateTime.now());
                            newDecision.setCreatedAt(LocalDateTime.now());
                            newDecision.setUpdatedAt(LocalDateTime.now());
                            return entityTemplate.insert(newDecision).thenReturn(newDecision);
                        })))
                .flatMap(existing -> {
                    existing.setDecision(decision);
                    existing.setDecisionReason(decisionReason);
                    existing.setDecidedBy(decidedBy);
                    existing.setDecidedAt(LocalDateTime.now());
                    existing.setUpdatedAt(LocalDateTime.now());
                    return entityTemplate.update(existing).thenReturn(existing);
                });
    }

    public Flux<CoverageDecision> getDecisionsByClaim(UUID claimId) {
        return decisionRepository.findAllByClaimId(claimId);
    }
}