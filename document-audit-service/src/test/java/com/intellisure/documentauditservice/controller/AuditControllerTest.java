package com.intellisure.documentauditservice.controller;

import com.intellisure.documentauditservice.dto.CreateAuditEventRequest;
import com.intellisure.documentauditservice.entity.AuditActor;
import com.intellisure.documentauditservice.entity.AuditEvent;
import com.intellisure.documentauditservice.service.AuditEventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditControllerTest {
    @Mock AuditEventService auditEventService;
    @InjectMocks AuditController controller;

    @Test
    void creatingAuditEventPersistsLifecycleMetadata() {
        UUID entityId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        AuditEvent saved = AuditEvent.builder().auditEventId(UUID.randomUUID())
                .serviceName("claims-service").entityId(entityId).entityType("CLAIM")
                .action("STATUS_UPDATED").userId(userId).eventDetails("UNDER_REVIEW")
                .ipAddress("127.0.0.1").build();
        when(auditEventService.createAuditEvent(any(CreateAuditEventRequest.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(controller.create(new CreateAuditEventRequest(
                        "claims-service", entityId, "CLAIM", "STATUS_UPDATED",
                        userId, AuditActor.USER, "Status update", "1.0", "1.0",
                        UUID.randomUUID(), "{\"status\":\"OPEN\"}", "{\"status\":\"UNDER_REVIEW\"}",
                        "UNDER_REVIEW", "127.0.0.1")))
                .assertNext(event -> {
                    assertEquals(entityId, event.getEntityId());
                    assertEquals("STATUS_UPDATED", event.getAction());
                }).verifyComplete();

        ArgumentCaptor<CreateAuditEventRequest> captor = ArgumentCaptor.forClass(CreateAuditEventRequest.class);
        verify(auditEventService).createAuditEvent(captor.capture());
        assertEquals("claims-service", captor.getValue().serviceName());
        assertEquals("CLAIM", captor.getValue().entityType());
        assertEquals(userId, captor.getValue().userId());
    }

    @Test
    void listingAuditEventsFiltersByEntityIdentity() {
        UUID entityId = UUID.randomUUID();
        AuditEvent event = AuditEvent.builder().auditEventId(UUID.randomUUID())
                .entityId(entityId).entityType("CLAIM").action("FNOL_FILED").build();
        when(auditEventService.getAuditEvents(entityId, "CLAIM"))
                .thenReturn(Flux.just(event));

        StepVerifier.create(controller.list(entityId, "CLAIM"))
                .assertNext(result -> assertEquals("FNOL_FILED", result.getAction()))
                .verifyComplete();
        verify(auditEventService).getAuditEvents(entityId, "CLAIM");
    }
}