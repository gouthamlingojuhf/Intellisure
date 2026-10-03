package com.intellisure.quotepolicyservice.repository;

import com.intellisure.quotepolicyservice.entity.Subjectivity;
import com.intellisure.quotepolicyservice.enums.SubjectivityStatus;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface SubjectivityRepository extends ReactiveCrudRepository<Subjectivity, UUID> {

    Flux<Subjectivity> findAllByQuoteId(UUID quoteId);

    Flux<Subjectivity> findAllByQuoteIdAndStatus(UUID quoteId, SubjectivityStatus status);

    Mono<Long> countByQuoteIdAndStatus(UUID quoteId, SubjectivityStatus status);
}