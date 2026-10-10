package com.intellisure.documentauditservice.service;

import com.intellisure.documentauditservice.dto.CreateAuditEventRequest;
import com.intellisure.documentauditservice.entity.AuditActor;
import com.intellisure.documentauditservice.entity.AuditEvent;
import com.intellisure.documentauditservice.repository.AuditEventRepository;
import com.intellisure.documentauditservice.security.SecurityActorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditEventService {
    private final AuditEventRepository auditEventRepository;
    private final SecurityActorService securityActorService;

    public Mono<AuditEvent> createAuditEvent(CreateAuditEventRequest request) {
        return securityActorService.currentUserId()
                .flatMap(actorUserId -> {
                    AuditEvent auditEvent = AuditEvent.builder()
                            .auditEventId(UUID.randomUUID())
                            .serviceName(request.serviceName())
                            .entityId(request.entityId())
                            .entityType(request.entityType())
                            .action(request.action())
                            .userId(actorUserId)
                            .actor(request.actor() != null ? request.actor() : AuditActor.USER)
                            .reason(request.reason())
                            .ruleVersion(request.ruleVersion())
                            .modelVersion(request.modelVersion())
                            .correlationId(request.correlationId())
                            .stateBefore(request.stateBefore())
                            .stateAfter(request.stateAfter())
                            .eventDetails(request.eventDetails())
                            .ipAddress(request.ipAddress())
                            .createdAt(LocalDateTime.now())
                            .isNew(true)
                            .build();

                    return auditEventRepository.save(auditEvent);
                });
    }

    public Flux<AuditEvent> getAuditEvents(UUID entityId, String entityType) {
        if (entityId != null && entityType != null) {
            return auditEventRepository.findByEntityIdAndEntityType(entityId, entityType);
        }
        return auditEventRepository.findAll();
    }

    public Mono<AuditEvent> getAuditEvent(UUID auditEventId) {
        return auditEventRepository.findById(auditEventId);
    }
}
