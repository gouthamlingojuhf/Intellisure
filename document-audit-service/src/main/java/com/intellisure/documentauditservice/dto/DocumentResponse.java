package com.intellisure.documentauditservice.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentResponse(
        UUID documentId,
        UUID entityId,
        String entityType,
        String documentType,
        String fileName,
        Long fileSize,
        String contentType,
        String storagePath,
        UUID uploadedBy,
        LocalDateTime createdAt
) {}
