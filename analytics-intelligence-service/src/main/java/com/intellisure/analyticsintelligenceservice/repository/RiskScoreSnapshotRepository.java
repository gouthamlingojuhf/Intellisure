package com.intellisure.analyticsintelligenceservice.repository;

import com.intellisure.analyticsintelligenceservice.entity.RiskScoreSnapshot;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RiskScoreSnapshotRepository extends R2dbcRepository<RiskScoreSnapshot, UUID> {
    Flux<RiskScoreSnapshot> findByCustomerIdOrderByGeneratedAtDesc(UUID customerId);
}
