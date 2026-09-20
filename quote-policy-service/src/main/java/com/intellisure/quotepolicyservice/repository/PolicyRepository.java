package com.intellisure.quotepolicyservice.repository;

import com.intellisure.quotepolicyservice.entity.Policy;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PolicyRepository extends R2dbcRepository<Policy, UUID> {
    Flux<Policy> findByCustomerId(UUID customerId);
    Mono<Policy> findByPolicyNumber(String policyNumber);
}
