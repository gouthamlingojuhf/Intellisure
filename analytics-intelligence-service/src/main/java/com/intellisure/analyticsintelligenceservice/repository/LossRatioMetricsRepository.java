package com.intellisure.analyticsintelligenceservice.repository;

import com.intellisure.analyticsintelligenceservice.entity.LossRatioMetrics;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

public interface LossRatioMetricsRepository extends R2dbcRepository<LossRatioMetrics, UUID> {
    Mono<LossRatioMetrics> findFirstByOrderByCalculatedAtDesc();
    Mono<LossRatioMetrics> findFirstByPeriodStartAndPeriodEnd(LocalDateTime periodStart, LocalDateTime periodEnd);
}