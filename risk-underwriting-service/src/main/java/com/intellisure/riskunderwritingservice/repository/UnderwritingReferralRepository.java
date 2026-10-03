package com.intellisure.riskunderwritingservice.repository;

import com.intellisure.riskunderwritingservice.entity.UnderwritingReferral;
import com.intellisure.riskunderwritingservice.enums.ReferralStatus;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.UUID;

public interface UnderwritingReferralRepository
        extends ReactiveCrudRepository<
        UnderwritingReferral,
        UUID
        > {

    Flux<UnderwritingReferral> findAllByAssessmentId(
            UUID assessmentId
    );

    Flux<UnderwritingReferral>
    findAllByAssessmentIdAndStatus(
            UUID assessmentId,
            ReferralStatus status
    );

    Flux<UnderwritingReferral>
    findAllByAssessmentIdAndStatusIn(
            UUID assessmentId,
            Collection<ReferralStatus> statuses
    );

    Flux<UnderwritingReferral> findAllByStatus(
            ReferralStatus status
    );

    Flux<UnderwritingReferral> findAllByReferredTo(
            UUID referredTo
    );
}