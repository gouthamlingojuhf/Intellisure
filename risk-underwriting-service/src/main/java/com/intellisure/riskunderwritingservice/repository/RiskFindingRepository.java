package com.intellisure.riskunderwritingservice.repository;

import com.intellisure.riskunderwritingservice.entity.RiskFinding;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface RiskFindingRepository extends R2dbcRepository<RiskFinding, UUID> {
    Flux<RiskFinding> findByAssessmentId(UUID assessmentId);
}
