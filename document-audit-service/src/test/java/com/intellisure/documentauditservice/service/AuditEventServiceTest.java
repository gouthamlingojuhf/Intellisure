package com.intellisure.documentauditservice.service;

import com.intellisure.documentauditservice.dto.CreateAuditEventRequest;
import com.intellisure.documentauditservice.entity.AuditActor;
import com.intellisure.documentauditservice.entity.AuditEvent;
import com.intellisure.documentauditservice.repository.AuditEventRepository;
import com.intellisure.documentauditservice.security.SecurityActorService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditEventServiceTest {

    @Mock AuditEventRepository repository;
    @Mock SecurityActorService securityActorService;
    @InjectMocks AuditEventService service;

    @Test
    void attributesCreatedAuditEventToAuthenticatedUserAndDefaultsActor() {
        UUID actorId = UUID.randomUUID();
        when(securityActorService.currentUserId()).thenReturn(Mono.just(actorId));
        when(repository.save(any(AuditEvent.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(service.createAuditEvent(request(null)))
                .assertNext(event -> {
                    assertThat(event.getUserId()).isEqualTo(actorId);
                    assertThat(event.getActor()).isEqualTo(AuditActor.USER);
                    assertThat(event.isNew()).isTrue();
                }).verifyComplete();

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getServiceName()).isEqualTo("claims-service");
    }

    @Test
    void preservesExplicitActorAndDelegatesReads() {
        UUID actorId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        when(securityActorService.currentUserId()).thenReturn(Mono.just(actorId));
        when(repository.save(any(AuditEvent.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        AuditEvent stored = AuditEvent.builder().auditEventId(eventId).entityId(entityId)
                .entityType("CLAIM").action("FNOL_FILED").build();
        when(repository.findByEntityIdAndEntityType(entityId, "CLAIM")).thenReturn(Flux.just(stored));
        when(repository.findById(eventId)).thenReturn(Mono.just(stored));

        StepVerifier.create(service.createAuditEvent(request(AuditActor.SYSTEM)))
                .assertNext(event -> assertThat(event.getActor()).isEqualTo(AuditActor.SYSTEM))
                .verifyComplete();
        StepVerifier.create(service.getAuditEvents(entityId, "CLAIM"))
                .assertNext(event -> assertThat(event.getAuditEventId()).isEqualTo(eventId)).verifyComplete();
        StepVerifier.create(service.getAuditEvent(eventId))
                .assertNext(event -> assertThat(event.getAction()).isEqualTo("FNOL_FILED")).verifyComplete();
    }

    private CreateAuditEventRequest request(AuditActor actor) {
        return new CreateAuditEventRequest(
                "claims-service", UUID.randomUUID(), "CLAIM", "STATUS_UPDATED", UUID.randomUUID(),
                actor, "reason", "rule-1", "model-1", UUID.randomUUID(), "before", "after",
                "details", "127.0.0.1");
    }
}
