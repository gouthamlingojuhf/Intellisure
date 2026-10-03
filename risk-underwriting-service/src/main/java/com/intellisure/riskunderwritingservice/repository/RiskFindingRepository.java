package com.intellisure.riskunderwritingservice.repository;

import com.intellisure.riskunderwritingservice.entity.RiskFinding;
import com.intellisure.riskunderwritingservice.enums.FindingSeverity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface RiskFindingRepository
        extends ReactiveCrudRepository<RiskFinding, UUID> {

    Flux<RiskFinding> findAllByAssessmentId(
            UUID assessmentId
    );

    Flux<RiskFinding> findAllByAssessmentIdAndSeverity(
            UUID assessmentId,
            FindingSeverity severity
    );
}