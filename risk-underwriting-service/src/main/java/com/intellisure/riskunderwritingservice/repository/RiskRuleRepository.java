package com.intellisure.riskunderwritingservice.repository;

import com.intellisure.riskunderwritingservice.entity.RiskRule;
import com.intellisure.riskunderwritingservice.enums.RuleStatus;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

public interface RiskRuleRepository
        extends ReactiveCrudRepository<RiskRule, UUID> {

    Mono<RiskRule> findByRuleCodeAndVersion(
            String ruleCode,
            Integer version
    );

    Flux<RiskRule> findAllByStatusOrderByPriorityAsc(
            RuleStatus status
    );

    Flux<RiskRule>
    findAllByStatusAndEffectiveFromLessThanEqual(
            RuleStatus status,
            LocalDate effectiveDate
    );

    Mono<Boolean> existsByRuleCodeAndVersion(
            String ruleCode,
            Integer version
    );
}