package com.intellisure.documentauditservice.controller;

import com.intellisure.documentauditservice.dto.DocumentResponse;
import com.intellisure.documentauditservice.dto.UploadDocumentRequest;
import com.intellisure.documentauditservice.entity.DocumentType;
import com.intellisure.documentauditservice.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class DocumentControllerTest {
    @Mock DocumentService documentService;
    @InjectMocks DocumentController controller;

    @Test
    void delegatesDocumentCreate() {
        UploadDocumentRequest request = new UploadDocumentRequest(
                UUID.randomUUID(), "CLAIM", DocumentType.PHOTOGRAPH, "damage.jpg", 20L,
                "image/jpeg", "claims/damage.jpg", "hash", 1);
        DocumentResponse response = response(request.entityId());
        when(documentService.logDocumentMetadata(request)).thenReturn(Mono.just(response));

        StepVerifier.create(controller.logDocument(request))
                .assertNext(result -> assertThat(result.documentId()).isEqualTo(response.documentId()))
                .verifyComplete();
        verify(documentService).logDocumentMetadata(request);
    }

    @Test
    void delegatesDocumentReads() {
        UUID documentId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();
        DocumentResponse response = response(entityId);
        when(documentService.getDocument(documentId)).thenReturn(Mono.just(response));
        when(documentService.getDocumentsByEntity(entityId, "CLAIM")).thenReturn(Flux.just(response));

        StepVerifier.create(controller.getDocument(documentId))
                .assertNext(result -> assertThat(result.documentId()).isEqualTo(response.documentId()))
                .verifyComplete();
        StepVerifier.create(controller.listDocuments(entityId, "CLAIM"))
                .assertNext(result -> assertThat(result.entityType()).isEqualTo("CLAIM"))
                .verifyComplete();
        verify(documentService).getDocument(documentId);
        verify(documentService).getDocumentsByEntity(entityId, "CLAIM");
    }

    private DocumentResponse response(UUID entityId) {
        return new DocumentResponse(UUID.randomUUID(), entityId, "CLAIM", DocumentType.PHOTOGRAPH,
                "damage.jpg", 20L, "image/jpeg", "claims/damage.jpg", "hash", 1,
                UUID.randomUUID(), null);
    }
}
