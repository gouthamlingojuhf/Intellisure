package com.intellisure.quotepolicyservice.repository;

import com.intellisure.quotepolicyservice.entity.PolicyCoverage;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

public interface PolicyCoverageRepository
        extends ReactiveCrudRepository<PolicyCoverage, UUID> {

    Flux<PolicyCoverage> findAllByPolicyId(UUID policyId);

    Mono<PolicyCoverage> findByPolicyIdAndCoverageCode(
            UUID policyId,
            String coverageCode
    );

    Mono<Boolean> existsByPolicyIdAndCoverageCode(
            UUID policyId,
            String coverageCode
    );

    Flux<PolicyCoverage>
    findAllByPolicyIdAndEffectiveFromLessThanEqualAndEffectiveToGreaterThanEqual(
            UUID policyId,
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    );
}