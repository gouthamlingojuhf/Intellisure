package com.intellisure.riskunderwritingservice.repository;

import com.intellisure.riskunderwritingservice.entity.RiskRecommendation;
import com.intellisure.riskunderwritingservice.enums.RecommendationPriority;
import com.intellisure.riskunderwritingservice.enums.RecommendationStatus;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface RiskRecommendationRepository
        extends ReactiveCrudRepository<RiskRecommendation, UUID> {

    Flux<RiskRecommendation> findAllByAssessmentId(
            UUID assessmentId
    );

    Flux<RiskRecommendation> findAllByAssessmentIdAndStatus(
            UUID assessmentId,
            RecommendationStatus status
    );

    Flux<RiskRecommendation> findAllByAssessmentIdAndPriority(
            UUID assessmentId,
            RecommendationPriority priority
    );

    Flux<RiskRecommendation>
    findAllByAssessmentIdAndRequiredBeforeBindTrue(
            UUID assessmentId
    );

    Flux<RiskRecommendation>
    findAllByAssessmentIdAndRequiredBeforeBindTrueAndStatus(
            UUID assessmentId,
            RecommendationStatus status
    );
}