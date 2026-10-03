package com.intellisure.quotepolicyservice.repository;

import com.intellisure.quotepolicyservice.entity.QuoteVersion;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface QuoteVersionRepository extends ReactiveCrudRepository<QuoteVersion, UUID> {

    Flux<QuoteVersion> findAllByQuoteId(UUID quoteId);

    Mono<QuoteVersion> findByQuoteIdAndVersion(UUID quoteId, Long version);

    Mono<Boolean> existsByQuoteIdAndVersion(UUID quoteId, Long version);
}