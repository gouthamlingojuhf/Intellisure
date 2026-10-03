package com.intellisure.claimsservice.repository;

import com.intellisure.claimsservice.entity.CoverageDecision;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CoverageDecisionRepository extends ReactiveCrudRepository<CoverageDecision, UUID> {
    Flux<CoverageDecision> findAllByClaimId(UUID claimId);
    Mono<CoverageDecision> findByClaimIdAndCoverageCode(UUID claimId, String coverageCode);
}