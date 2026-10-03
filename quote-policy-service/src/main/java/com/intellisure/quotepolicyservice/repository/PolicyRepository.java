package com.intellisure.quotepolicyservice.repository;

import com.intellisure.quotepolicyservice.entity.Policy;
import com.intellisure.quotepolicyservice.enums.PolicyStatus;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;
public interface PolicyRepository
        extends ReactiveCrudRepository<Policy, UUID> {

    Mono<Policy> findByPolicyNumber(String policyNumber);

    Mono<Policy> findByQuoteId(UUID quoteId);

    Flux<Policy> findAllByCustomerId(UUID customerId);

    Flux<Policy> findAllByStatus(
            PolicyStatus status
    );

    Flux<Policy> findAllByStatusAndStartDateLessThanEqual(
            PolicyStatus status,
            LocalDate date
    );

    Flux<Policy> findAllByStatusAndEndDateBefore(
            PolicyStatus status,
            LocalDate date
    );

    Mono<Boolean> existsByQuoteId(UUID quoteId);

    Mono<Boolean> existsByPolicyNumber(String policyNumber);
}