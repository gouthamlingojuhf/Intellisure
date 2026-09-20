package com.intellisure.documentauditservice.service;

import com.intellisure.documentauditservice.dto.DocumentResponse;
import com.intellisure.documentauditservice.dto.UploadDocumentRequest;
import com.intellisure.documentauditservice.entity.Document;
import com.intellisure.documentauditservice.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;

    public Mono<DocumentResponse> logDocumentMetadata(UploadDocumentRequest request, UUID uploadedBy) {
        LocalDateTime now = LocalDateTime.now();
        
        Document document = Document.builder()
                .documentId(UUID.randomUUID())
                .entityId(request.entityId())
                .entityType(request.entityType())
                .documentType(request.documentType())
                .fileName(request.fileName())
                .fileSize(request.fileSize())
                .contentType(request.contentType())
                .storagePath(request.storagePath())
                .uploadedBy(uploadedBy)
                .createdAt(now)
                .isNew(true)
                .build();

        return documentRepository.save(document)
                .map(this::mapToResponse);
    }

    private DocumentResponse mapToResponse(Document document) {
        return new DocumentResponse(
                document.getDocumentId(),
                document.getEntityId(),
                document.getEntityType(),
                document.getDocumentType(),
                document.getFileName(),
                document.getFileSize(),
                document.getContentType(),
                document.getStoragePath(),
                document.getUploadedBy(),
                document.getCreatedAt()
        );
    }
}
