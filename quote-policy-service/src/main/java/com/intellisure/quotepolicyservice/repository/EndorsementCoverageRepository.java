package com.intellisure.quotepolicyservice.repository;

import com.intellisure.quotepolicyservice.entity.EndorsementCoverage;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface EndorsementCoverageRepository extends ReactiveCrudRepository<EndorsementCoverage, UUID> {

    Flux<EndorsementCoverage> findAllByEndorsementId(UUID endorsementId);

    Mono<Void> deleteAllByEndorsementId(UUID endorsementId);
}