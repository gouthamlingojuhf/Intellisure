package com.intellisure.documentauditservice.controller;

import com.intellisure.documentauditservice.dto.CreateAuditEventRequest;
import com.intellisure.documentauditservice.entity.AuditEvent;
import com.intellisure.documentauditservice.repository.AuditEventRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/audit-events")
@RequiredArgsConstructor
public class AuditController {
    private final AuditEventRepository repository;

    @PostMapping
    public Mono<AuditEvent> create(@Valid @RequestBody CreateAuditEventRequest request) {
        return repository.save(AuditEvent.builder().auditEventId(UUID.randomUUID())
                .serviceName(request.serviceName()).entityId(request.entityId()).entityType(request.entityType())
                .action(request.action()).userId(request.userId()).eventDetails(request.eventDetails())
                .ipAddress(request.ipAddress()).createdAt(LocalDateTime.now()).isNew(true).build());
    }

    @GetMapping
    public Flux<AuditEvent> list(@RequestParam UUID entityId, @RequestParam String entityType) {
        return repository.findByEntityIdAndEntityType(entityId, entityType);
    }
}
