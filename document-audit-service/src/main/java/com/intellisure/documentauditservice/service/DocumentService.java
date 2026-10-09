package com.intellisure.documentauditservice.service;

import com.intellisure.documentauditservice.dto.DocumentResponse;
import com.intellisure.documentauditservice.dto.UploadDocumentRequest;
import com.intellisure.documentauditservice.entity.Document;
import com.intellisure.documentauditservice.entity.DocumentType;
import com.intellisure.documentauditservice.exception.DocumentNotFoundException;
import com.intellisure.documentauditservice.security.DocumentSecurityService;
import com.intellisure.documentauditservice.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentSecurityService documentSecurityService;

    public Mono<DocumentResponse> logDocumentMetadata(UploadDocumentRequest request) {
        return documentSecurityService.assertEntityAccess(request.entityId(), request.entityType())
                .then(documentSecurityService.currentUserId())
                .flatMap(uploadedBy -> {
                    LocalDateTime now = LocalDateTime.now();
                    String sha256Hash = request.sha256Hash() != null ? request.sha256Hash() : computeSha256(request);
                    Integer version = request.version() != null ? request.version() : 1;

                    Document document = Document.builder()
                            .documentId(UUID.randomUUID())
                            .entityId(request.entityId())
                            .entityType(request.entityType())
                            .documentType(request.documentType())
                            .fileName(request.fileName())
                            .fileSize(request.fileSize())
                            .contentType(request.contentType())
                            .storagePath(request.storagePath())
                            .sha256Hash(sha256Hash)
                            .version(version)
                            .uploadedBy(uploadedBy)
                            .createdAt(now)
                            .isNew(true)
                            .build();

                    return documentRepository.save(document).map(this::mapToResponse);
                });
    }

    public Mono<DocumentResponse> getDocument(UUID documentId) {
        return documentRepository.findById(documentId)
                .switchIfEmpty(Mono.error(new DocumentNotFoundException("Document not found: " + documentId)))
                .flatMap(document -> documentSecurityService.assertEntityAccess(document.getEntityId(), document.getEntityType())
                        .thenReturn(mapToResponse(document)));
    }

    public Flux<DocumentResponse> getDocumentsByEntity(UUID entityId, String entityType) {
        return documentSecurityService.assertEntityAccess(entityId, entityType)
                .thenMany(documentRepository.findByEntityIdAndEntityType(entityId, entityType))
                .map(this::mapToResponse);
    }

    private String computeSha256(UploadDocumentRequest request) {
        try {
            String input = request.entityId() + "|" + request.entityType() + "|" +
                    request.documentType() + "|" + request.fileName() + "|" +
                    request.fileSize() + "|" + request.contentType() + "|" +
                    request.storagePath() + "|" + LocalDateTime.now();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
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
                document.getSha256Hash(),
                document.getVersion(),
                document.getUploadedBy(),
                document.getCreatedAt()
        );
    }
}
