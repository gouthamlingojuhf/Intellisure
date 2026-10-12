package com.intellisure.riskunderwritingservice.repository;

import com.intellisure.riskunderwritingservice.entity.RiskAssessment;
import com.intellisure.riskunderwritingservice.enums.RiskAssessmentStatus;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RiskAssessmentRepository
        extends ReactiveCrudRepository<RiskAssessment, UUID> {

    Mono<RiskAssessment> findByAssessmentNumber(
            String assessmentNumber
    );

    Mono<RiskAssessment> findByQuoteId(
            UUID quoteId
    );

    Flux<RiskAssessment> findAllByCustomerId(
            UUID customerId
    );

    Flux<RiskAssessment> findAllByStatus(
            RiskAssessmentStatus status
    );

    Flux<RiskAssessment> findAllByAssignedUnderwriterId(
            UUID underwriterId
    );

    Flux<RiskAssessment> findAllByAssignedRiskEngineerId(
            UUID riskEngineerId
    );

    Mono<Boolean> existsByQuoteId(
            UUID quoteId
    );

    Mono<Boolean> existsByAssessmentNumber(
            String assessmentNumber
    );

    @org.springframework.data.r2dbc.repository.Query("""
        SELECT COUNT(*)
        FROM risk_assessment
        WHERE assigned_risk_engineer_id = :engineerId
          AND status IN ('DRAFT', 'IN_PROGRESS', 'NEEDS_INFORMATION', 'RISK_ENGINEERING_REQUIRED', 'UNDER_REVIEW', 'REFERRED')
        """)
    Mono<Long> countActiveAssessmentsByRiskEngineerId(
            UUID engineerId
    );
}