package com.intellisure.documentauditservice.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentEntityContractTest {
    @Test
    void documentBuilderAndAccessorsPreserveMetadata() {
        UUID documentId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();
        Document document = Document.builder().documentId(documentId).entityId(entityId)
                .entityType("CLAIM").documentType(DocumentType.PHOTOGRAPH).fileName("damage.jpg")
                .fileSize(10L).contentType("image/jpeg").storagePath("claims/damage.jpg")
                .sha256Hash("hash").version(1).uploadedBy(UUID.randomUUID())
                .createdAt(LocalDateTime.now()).isNew(false).build();

        assertThat(document.getId()).isEqualTo(documentId);
        assertThat(document.getEntityId()).isEqualTo(entityId);
        assertThat(document.getDocumentType()).isEqualTo(DocumentType.PHOTOGRAPH);
        assertThat(document.isNew()).isFalse();
        document.setFileName("updated.jpg");
        document.setVersion(2);
        document.setNew(true);
        assertThat(document.getFileName()).isEqualTo("updated.jpg");
        assertThat(document.isNew()).isTrue();
    }

    @Test
    void auditEventBuilderAndAccessorsPreserveActorMetadata() {
        UUID eventId = UUID.randomUUID();
        AuditEvent event = AuditEvent.builder().auditEventId(eventId).serviceName("claims-service")
                .entityId(UUID.randomUUID()).entityType("CLAIM").action("FNOL_FILED")
                .userId(UUID.randomUUID()).actor(AuditActor.SYSTEM).reason("workflow")
                .ruleVersion("r1").modelVersion("m1").correlationId(UUID.randomUUID())
                .stateBefore("OPEN").stateAfter("UNDER_REVIEW").eventDetails("details")
                .ipAddress("127.0.0.1").createdAt(LocalDateTime.now()).isNew(false).build();

        assertThat(event.getId()).isEqualTo(eventId);
        assertThat(event.getActor()).isEqualTo(AuditActor.SYSTEM);
        assertThat(event.getStateAfter()).isEqualTo("UNDER_REVIEW");
        assertThat(event.isNew()).isFalse();
        event.setAction("UPDATED");
        event.setNew(true);
        assertThat(event.getAction()).isEqualTo("UPDATED");
        assertThat(event.isNew()).isTrue();
    }
}
