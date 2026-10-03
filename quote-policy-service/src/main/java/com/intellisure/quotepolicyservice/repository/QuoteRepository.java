package com.intellisure.quotepolicyservice.repository;

import com.intellisure.quotepolicyservice.entity.Quote;
import com.intellisure.quotepolicyservice.enums.QuoteStatus;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

public interface QuoteRepository
        extends ReactiveCrudRepository<Quote, UUID> {

    Mono<Quote> findByQuoteNumber(String quoteNumber);

    Flux<Quote> findAllByCustomerId(UUID customerId);

    Flux<Quote> findAllByCustomerIdAndStatus(
            UUID customerId,
            QuoteStatus status
    );

    Flux<Quote> findAllByAssignedUnderwriterId(
            UUID assignedUnderwriterId
    );

    Flux<Quote> findAllByStatus(QuoteStatus status);

    Mono<Boolean> existsByQuoteNumber(String quoteNumber);

    @Query("""
        SELECT COUNT(*)
        FROM quote
        WHERE assigned_underwriter_id = :underwriterId
          AND status IN (
              'IN_REVIEW',
              'NEEDS_INFORMATION',
              'RISK_ASSESSMENT'
          )
        """)
    Mono<Long> countActiveQuotesByUnderwriterId(
            UUID underwriterId
    );


    Flux<Quote> findAllByStatusAndQuoteExpiresAtLessThanEqual(
            QuoteStatus status,
            LocalDateTime expirationTime
    );

}