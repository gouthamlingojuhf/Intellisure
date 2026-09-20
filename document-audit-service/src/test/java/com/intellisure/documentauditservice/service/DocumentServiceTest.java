package com.intellisure.documentauditservice.service;

import com.intellisure.documentauditservice.dto.UploadDocumentRequest;
import com.intellisure.documentauditservice.entity.Document;
import com.intellisure.documentauditservice.repository.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {
    @Mock DocumentRepository repository;
    @InjectMocks DocumentService service;

    @Test
    void loggingDocumentPreservesEvidenceMetadataAndUploader() {
        UUID entity = UUID.randomUUID(), uploader = UUID.randomUUID();
        Document saved = Document.builder().documentId(UUID.randomUUID()).entityId(entity)
                .entityType("CLAIM").documentType("PHOTO").fileName("damage.jpg").fileSize(1024L)
                .contentType("image/jpeg").storagePath("claims/damage.jpg").uploadedBy(uploader).build();
        when(repository.save(any(Document.class))).thenReturn(Mono.just(saved));
        StepVerifier.create(service.logDocumentMetadata(new UploadDocumentRequest(entity, "CLAIM",
                        "PHOTO", "damage.jpg", 1024L, "image/jpeg", "claims/damage.jpg"), uploader))
                .assertNext(result -> {
                    org.junit.jupiter.api.Assertions.assertEquals(entity, result.entityId());
                    org.junit.jupiter.api.Assertions.assertEquals("damage.jpg", result.fileName());
                    org.junit.jupiter.api.Assertions.assertEquals(uploader, result.uploadedBy());
                }).verifyComplete();

        ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);
        verify(repository).save(captor.capture());
        Document persisted = captor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals(entity, persisted.getEntityId());
        org.junit.jupiter.api.Assertions.assertEquals(uploader, persisted.getUploadedBy());
        org.junit.jupiter.api.Assertions.assertTrue(persisted.isNew());
    }
}
