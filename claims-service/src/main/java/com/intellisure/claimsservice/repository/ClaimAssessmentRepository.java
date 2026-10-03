package com.intellisure.claimsservice.repository;

import com.intellisure.claimsservice.entity.ClaimAssessment;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ClaimAssessmentRepository extends ReactiveCrudRepository<ClaimAssessment, UUID> {
    Flux<ClaimAssessment> findAllByClaimId(UUID claimId);
    Mono<ClaimAssessment> findByClaimIdAndStatus(UUID claimId, String status);
}