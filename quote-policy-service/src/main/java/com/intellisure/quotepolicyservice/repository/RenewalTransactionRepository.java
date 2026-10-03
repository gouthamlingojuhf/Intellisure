package com.intellisure.quotepolicyservice.repository;

import com.intellisure.quotepolicyservice.entity.RenewalTransaction;
import com.intellisure.quotepolicyservice.enums.RenewalStatus;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RenewalTransactionRepository extends ReactiveCrudRepository<RenewalTransaction, UUID> {

    Mono<RenewalTransaction> findByRenewalNumber(String renewalNumber);

    Flux<RenewalTransaction> findAllByPolicyId(UUID policyId);

    Flux<RenewalTransaction> findAllByPolicyIdAndStatus(UUID policyId, RenewalStatus status);

    Mono<Boolean> existsByRenewalNumber(String renewalNumber);
}