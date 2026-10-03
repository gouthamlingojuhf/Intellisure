package com.intellisure.quotepolicyservice.repository;

import com.intellisure.quotepolicyservice.entity.UnderwritingDecision;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UnderwritingDecisionRepository
        extends ReactiveCrudRepository<UnderwritingDecision, UUID> {

    Flux<UnderwritingDecision> findAllByQuoteId(
            UUID quoteId
    );

    Mono<UnderwritingDecision>
    findFirstByQuoteIdOrderByDecidedAtDesc(
            UUID quoteId
    );

    Mono<UnderwritingDecision> findBySourceDecisionId(
            UUID sourceDecisionId
    );
}