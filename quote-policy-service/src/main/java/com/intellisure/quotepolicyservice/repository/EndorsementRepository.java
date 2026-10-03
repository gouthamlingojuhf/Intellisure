package com.intellisure.quotepolicyservice.repository;

import com.intellisure.quotepolicyservice.entity.Endorsement;
import com.intellisure.quotepolicyservice.enums.EndorsementStatus;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface EndorsementRepository extends ReactiveCrudRepository<Endorsement, UUID> {

    Mono<Endorsement> findByEndorsementNumber(String endorsementNumber);

    Flux<Endorsement> findAllByPolicyId(UUID policyId);

    Flux<Endorsement> findAllByPolicyIdAndStatus(UUID policyId, EndorsementStatus status);

    Mono<Boolean> existsByEndorsementNumber(String endorsementNumber);
}