package com.intellisure.documentauditservice.dto;

import com.intellisure.documentauditservice.entity.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record UploadDocumentRequest(
        @NotNull UUID entityId,
        @NotBlank String entityType,
        @NotNull DocumentType documentType,
        @NotBlank String fileName,
        @NotNull Long fileSize,
        @NotBlank String contentType,
        @NotBlank String storagePath,
        String sha256Hash,
        Integer version
) {}