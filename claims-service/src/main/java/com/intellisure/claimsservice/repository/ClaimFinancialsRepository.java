package com.intellisure.claimsservice.repository;

import com.intellisure.claimsservice.entity.ClaimFinancials;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ClaimFinancialsRepository extends ReactiveCrudRepository<ClaimFinancials, UUID> {
    Mono<ClaimFinancials> findByClaimId(UUID claimId);
}