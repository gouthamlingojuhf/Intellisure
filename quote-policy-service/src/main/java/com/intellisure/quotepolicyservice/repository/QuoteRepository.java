package com.intellisure.quotepolicyservice.repository;
import com.intellisure.quotepolicyservice.entity.Quote;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface QuoteRepository
        extends ReactiveCrudRepository<Quote, UUID> {

    Flux<Quote> findByCustomerId(UUID customerId);
}