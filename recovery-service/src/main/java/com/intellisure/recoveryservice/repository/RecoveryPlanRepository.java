package com.intellisure.recoveryservice.repository;

import com.intellisure.recoveryservice.entity.RecoveryPlan;
import com.intellisure.recoveryservice.entity.RecoveryPlanStatus;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RecoveryPlanRepository extends R2dbcRepository<RecoveryPlan, UUID> {
    Mono<RecoveryPlan> findByRecoveryCaseId(UUID recoveryCaseId);
    Flux<RecoveryPlan> findByStatus(RecoveryPlanStatus status);
}