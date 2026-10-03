package com.intellisure.claimsservice.repository;

import com.intellisure.claimsservice.entity.Subrogation;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface SubrogationRepository extends ReactiveCrudRepository<Subrogation, UUID> {
    Flux<Subrogation> findAllByClaimId(UUID claimId);
    Flux<Subrogation> findByClaimIdAndStatus(UUID claimId, String status);
}