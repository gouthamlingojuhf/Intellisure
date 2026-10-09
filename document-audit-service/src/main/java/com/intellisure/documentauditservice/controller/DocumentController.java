package com.intellisure.documentauditservice.controller;

import com.intellisure.documentauditservice.dto.DocumentResponse;
import com.intellisure.documentauditservice.dto.UploadDocumentRequest;
import com.intellisure.documentauditservice.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    public Mono<DocumentResponse> logDocument(
            @Valid @RequestBody UploadDocumentRequest request) {
        return documentService.logDocumentMetadata(request);
    }

    @GetMapping("/{documentId}")
    public Mono<DocumentResponse> getDocument(@PathVariable UUID documentId) {
        return documentService.getDocument(documentId);
    }

    @GetMapping
    public Flux<DocumentResponse> listDocuments(
            @RequestParam UUID entityId,
            @RequestParam String entityType) {
        return documentService.getDocumentsByEntity(entityId, entityType);
    }
}
