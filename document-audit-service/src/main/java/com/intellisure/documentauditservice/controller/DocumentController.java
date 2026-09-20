package com.intellisure.documentauditservice.controller;

import com.intellisure.documentauditservice.dto.DocumentResponse;
import com.intellisure.documentauditservice.dto.UploadDocumentRequest;
import com.intellisure.documentauditservice.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    public Mono<DocumentResponse> logDocument(
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @Valid @RequestBody UploadDocumentRequest request) {
        
        if (userId == null) {
            userId = UUID.randomUUID();
        }
        
        return documentService.logDocumentMetadata(request, userId);
    }

}
