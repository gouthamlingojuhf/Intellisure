package com.intellisure.documentauditservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record UploadDocumentRequest(
        @NotNull UUID entityId,
        @NotBlank String entityType,
        @NotBlank String documentType,
        @NotBlank String fileName,
        @NotNull Long fileSize,
        @NotBlank String contentType,
        @NotBlank String storagePath
) {}
