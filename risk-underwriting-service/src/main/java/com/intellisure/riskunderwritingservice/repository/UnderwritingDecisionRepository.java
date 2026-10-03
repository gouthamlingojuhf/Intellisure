package com.intellisure.riskunderwritingservice.repository;

import com.intellisure.riskunderwritingservice.entity.UnderwritingDecision;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UnderwritingDecisionRepository
        extends ReactiveCrudRepository<
        UnderwritingDecision,
        UUID
        > {

    Flux<UnderwritingDecision>
    findAllByAssessmentIdOrderByDecidedAtDesc(
            UUID assessmentId
    );

    Mono<UnderwritingDecision>
    findFirstByAssessmentIdOrderByDecidedAtDesc(
            UUID assessmentId
    );

    Mono<UnderwritingDecision>
    findFirstByQuoteIdOrderByDecidedAtDesc(
            UUID quoteId
    );
}