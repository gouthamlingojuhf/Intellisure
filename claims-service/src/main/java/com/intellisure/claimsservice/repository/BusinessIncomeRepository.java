package com.intellisure.claimsservice.repository;

import com.intellisure.claimsservice.entity.BusinessIncome;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface BusinessIncomeRepository extends ReactiveCrudRepository<BusinessIncome, UUID> {
    Mono<BusinessIncome> findByClaimId(UUID claimId);
}