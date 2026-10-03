package com.intellisure.documentauditservice.repository;

import com.intellisure.documentauditservice.entity.AuditEvent;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface AuditEventRepository extends R2dbcRepository<AuditEvent, UUID> {
    Flux<AuditEvent> findByEntityIdAndEntityType(UUID entityId, String entityType);
}
