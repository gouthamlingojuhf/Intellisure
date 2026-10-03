package com.intellisure.claimsservice.repository;

import com.intellisure.claimsservice.entity.Salvage;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface SalvageRepository extends ReactiveCrudRepository<Salvage, UUID> {
    Flux<Salvage> findAllByClaimId(UUID claimId);
    Flux<Salvage> findByClaimIdAndStatus(UUID claimId, String status);
}