package com.intellisure.riskunderwritingservice.repository;

import com.intellisure.riskunderwritingservice.entity.Subjectivity;
import com.intellisure.riskunderwritingservice.enums.SubjectivityStatus;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface SubjectivityRepository
        extends ReactiveCrudRepository<Subjectivity, UUID> {

    Flux<Subjectivity> findAllByAssessmentId(
            UUID assessmentId
    );

    Flux<Subjectivity> findAllByAssessmentIdAndStatus(
            UUID assessmentId,
            SubjectivityStatus status
    );

    Flux<Subjectivity>
    findAllByAssessmentIdAndRequiredBeforeBindTrue(
            UUID assessmentId
    );
}