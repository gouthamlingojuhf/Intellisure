package com.intellisure.claimsservice.repository;

import com.intellisure.claimsservice.entity.Claim;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ClaimRepository extends R2dbcRepository<Claim, UUID> {
    Flux<Claim> findByCustomerId(UUID customerId);
    Flux<Claim> findByPolicyId(UUID policyId);
    Flux<Claim> findByStatus(String status);
    Mono<Claim> findByClaimNumber(String claimNumber);
}
