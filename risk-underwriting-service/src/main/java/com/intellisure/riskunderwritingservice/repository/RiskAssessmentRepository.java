package com.intellisure.riskunderwritingservice.repository;

import com.intellisure.riskunderwritingservice.entity.RiskAssessment;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface RiskAssessmentRepository extends R2dbcRepository<RiskAssessment, UUID> {
    Flux<RiskAssessment> findByCustomerId(UUID customerId);
    Flux<RiskAssessment> findByQuoteId(UUID quoteId);
}
