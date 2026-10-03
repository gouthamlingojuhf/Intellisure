package com.intellisure.quotepolicyservice.repository;

import com.intellisure.quotepolicyservice.entity.QuoteCoverage;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface QuoteCoverageRepository
        extends ReactiveCrudRepository<QuoteCoverage, UUID> {

    Flux<QuoteCoverage> findAllByQuoteId(UUID quoteId);

    Mono<QuoteCoverage> findByQuoteIdAndCoverageCode(
            UUID quoteId,
            String coverageCode
    );

    Mono<Boolean> existsByQuoteIdAndCoverageCode(
            UUID quoteId,
            String coverageCode
    );

    Mono<Void> deleteAllByQuoteId(UUID quoteId);
}