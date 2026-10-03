package com.intellisure.recoveryservice.repository;

import com.intellisure.recoveryservice.entity.RecoveryCase;
import com.intellisure.recoveryservice.entity.RecoveryCaseStatus;
import com.intellisure.recoveryservice.entity.RecoverySeverity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RecoveryCaseRepository extends R2dbcRepository<RecoveryCase, UUID> {
    Mono<RecoveryCase> findByClaimId(UUID claimId);
    Flux<RecoveryCase> findByCustomerId(UUID customerId);
    Flux<RecoveryCase> findByStatus(RecoveryCaseStatus status);
    Flux<RecoveryCase> findBySeverity(RecoverySeverity severity);
    Flux<RecoveryCase> findByOwnerId(UUID ownerId);
}