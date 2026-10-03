package com.intellisure.documentauditservice.dto;

import com.intellisure.documentauditservice.entity.DocumentType;
import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentResponse(
        UUID documentId,
        UUID entityId,
        String entityType,
        DocumentType documentType,
        String fileName,
        Long fileSize,
        String contentType,
        String storagePath,
        String sha256Hash,
        Integer version,
        UUID uploadedBy,
        LocalDateTime createdAt
) {}