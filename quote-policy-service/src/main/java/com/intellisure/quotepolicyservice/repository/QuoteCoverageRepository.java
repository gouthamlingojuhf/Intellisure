package com.intellisure.quotepolicyservice.repository;

import com.intellisure.quotepolicyservice.entity.QuoteCoverage;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface QuoteCoverageRepository
        extends ReactiveCrudRepository<QuoteCoverage, UUID> {

    Flux<QuoteCoverage> findByQuoteId(UUID quoteId);
}