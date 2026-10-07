package com.intellisure.documentauditservice.controller;

import com.intellisure.documentauditservice.dto.CreateAuditEventRequest;
import com.intellisure.documentauditservice.entity.AuditEvent;
import com.intellisure.documentauditservice.service.AuditEventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/audit-events")
@RequiredArgsConstructor
public class AuditController {
    private final AuditEventService auditEventService;

    @PostMapping
    public Mono<AuditEvent> create(@Valid @RequestBody CreateAuditEventRequest request) {
        return auditEventService.createAuditEvent(request);
    }

    @GetMapping
    public Flux<AuditEvent> list(@RequestParam UUID entityId, @RequestParam String entityType) {
        return auditEventService.getAuditEvents(entityId, entityType);
    }

    @GetMapping("/{auditEventId}")
    public Mono<AuditEvent> getById(@PathVariable UUID auditEventId) {
        return auditEventService.getAuditEvent(auditEventId);
    }
}
