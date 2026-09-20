package com.intellisure.quotepolicyservice.repository;

import com.intellisure.quotepolicyservice.entity.PolicyCoverage;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface PolicyCoverageRepository extends R2dbcRepository<PolicyCoverage, UUID> {
    Flux<PolicyCoverage> findByPolicyId(UUID policyId);
}
