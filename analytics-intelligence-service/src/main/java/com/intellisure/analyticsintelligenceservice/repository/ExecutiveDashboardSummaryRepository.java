package com.intellisure.analyticsintelligenceservice.repository;

import com.intellisure.analyticsintelligenceservice.entity.ExecutiveDashboardSummary;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ExecutiveDashboardSummaryRepository extends R2dbcRepository<ExecutiveDashboardSummary, UUID> {
    Mono<ExecutiveDashboardSummary> findFirstByOrderByCalculatedAtDesc();
    Mono<ExecutiveDashboardSummary> findFirstByPeriodStartAndPeriodEnd(LocalDateTime periodStart, LocalDateTime periodEnd);
}