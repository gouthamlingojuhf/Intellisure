package com.intellisure.documentauditservice.controller;

import com.intellisure.documentauditservice.dto.CreateAuditEventRequest;
import com.intellisure.documentauditservice.entity.AuditEvent;
import com.intellisure.documentauditservice.repository.AuditEventRepository;
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
    @Mock AuditEventRepository repository;
    @InjectMocks AuditController controller;

    @Test
    void creatingAuditEventPersistsLifecycleMetadata() {
        UUID entityId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        AuditEvent saved = AuditEvent.builder().auditEventId(UUID.randomUUID())
                .serviceName("claims-service").entityId(entityId).entityType("CLAIM")
                .action("STATUS_UPDATED").userId(userId).eventDetails("UNDER_REVIEW")
                .ipAddress("127.0.0.1").build();
        when(repository.save(any(AuditEvent.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(controller.create(new CreateAuditEventRequest(
                        "claims-service", entityId, "CLAIM", "STATUS_UPDATED",
                        userId, "UNDER_REVIEW", "127.0.0.1")))
                .assertNext(event -> {
                    assertEquals(entityId, event.getEntityId());
                    assertEquals("STATUS_UPDATED", event.getAction());
                }).verifyComplete();

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(repository).save(captor.capture());
        assertEquals("claims-service", captor.getValue().getServiceName());
        assertEquals("CLAIM", captor.getValue().getEntityType());
        assertEquals(userId, captor.getValue().getUserId());
    }

    @Test
    void listingAuditEventsFiltersByEntityIdentity() {
        UUID entityId = UUID.randomUUID();
        AuditEvent event = AuditEvent.builder().auditEventId(UUID.randomUUID())
                .entityId(entityId).entityType("CLAIM").action("FNOL_FILED").build();
        when(repository.findByEntityIdAndEntityType(entityId, "CLAIM"))
                .thenReturn(Flux.just(event));

        StepVerifier.create(controller.list(entityId, "CLAIM"))
                .assertNext(result -> assertEquals("FNOL_FILED", result.getAction()))
                .verifyComplete();
        verify(repository).findByEntityIdAndEntityType(entityId, "CLAIM");
    }
}
