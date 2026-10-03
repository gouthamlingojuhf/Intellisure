package com.intellisure.quotepolicyservice.repository;

import com.intellisure.quotepolicyservice.entity.PremiumAudit;
import com.intellisure.quotepolicyservice.enums.AuditStatus;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PremiumAuditRepository extends ReactiveCrudRepository<PremiumAudit, UUID> {

    Mono<PremiumAudit> findByAuditNumber(String auditNumber);

    Flux<PremiumAudit> findAllByPolicyId(UUID policyId);

    Flux<PremiumAudit> findAllByPolicyIdAndStatus(UUID policyId, AuditStatus status);

    Mono<Boolean> existsByAuditNumber(String auditNumber);
}