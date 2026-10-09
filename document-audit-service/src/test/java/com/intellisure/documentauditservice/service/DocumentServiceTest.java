package com.intellisure.documentauditservice.service;

import com.intellisure.documentauditservice.dto.UploadDocumentRequest;
import com.intellisure.documentauditservice.entity.Document;
import com.intellisure.documentauditservice.entity.DocumentType;
import com.intellisure.documentauditservice.repository.DocumentRepository;
import com.intellisure.documentauditservice.security.DocumentSecurityService;
import org.junit.jupiter.api.BeforeEach;
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
    @Mock DocumentSecurityService documentSecurityService;
    @InjectMocks DocumentService service;

    @BeforeEach
    void allowEntityReadsForServiceTests() {
        lenient().when(documentSecurityService.assertEntityAccess(any(), anyString())).thenReturn(Mono.empty());
    }

    @Test
    void loggingDocumentPreservesEvidenceMetadataAndUploader() {
        UUID entity = UUID.randomUUID(), uploader = UUID.randomUUID();
        when(documentSecurityService.currentUserId()).thenReturn(Mono.just(uploader));
        Document saved = Document.builder().documentId(UUID.randomUUID()).entityId(entity)
                .entityType("CLAIM").documentType(DocumentType.PHOTOGRAPH).fileName("damage.jpg").fileSize(1024L)
                .contentType("image/jpeg").storagePath("claims/damage.jpg").sha256Hash("abc123").version(1)
                .uploadedBy(uploader).build();
        when(repository.save(any(Document.class))).thenReturn(Mono.just(saved));
        StepVerifier.create(service.logDocumentMetadata(new UploadDocumentRequest(entity, "CLAIM",
                        DocumentType.PHOTOGRAPH, "damage.jpg", 1024L, "image/jpeg", "claims/damage.jpg", null, null)))
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

    @Test
    void getDocumentReturnsMappedResponse() {
        UUID docId = UUID.randomUUID();
        Document saved = Document.builder().documentId(docId).entityId(UUID.randomUUID())
                .entityType("QUOTE").documentType(DocumentType.QUOTE_LETTER).fileName("quote.pdf").fileSize(2048L)
                .contentType("application/pdf").storagePath("quotes/quote.pdf").sha256Hash("hash123").version(1)
                .uploadedBy(UUID.randomUUID()).build();
        when(repository.findById(docId)).thenReturn(Mono.just(saved));

        StepVerifier.create(service.getDocument(docId))
                .assertNext(res -> {
                    org.junit.jupiter.api.Assertions.assertEquals(docId, res.documentId());
                    org.junit.jupiter.api.Assertions.assertEquals("quote.pdf", res.fileName());
                })
                .verifyComplete();
    }

    @Test
    void getDocumentsByEntityFiltersCorrectly() {
        UUID entityId = UUID.randomUUID();
        Document doc1 = Document.builder().documentId(UUID.randomUUID()).entityId(entityId)
                .entityType("POLICY").documentType(DocumentType.POLICY_DECLARATIONS).fileName("dec.pdf")
                .fileSize(4096L).contentType("application/pdf").storagePath("policies/dec.pdf").build();

        when(repository.findByEntityIdAndEntityType(entityId, "POLICY"))
                .thenReturn(reactor.core.publisher.Flux.just(doc1));

        StepVerifier.create(service.getDocumentsByEntity(entityId, "POLICY"))
                .assertNext(res -> org.junit.jupiter.api.Assertions.assertEquals("dec.pdf", res.fileName()))
                .verifyComplete();
    }
}
