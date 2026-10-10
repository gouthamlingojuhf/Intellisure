package com.intellisure.documentauditservice.repository;

import com.intellisure.documentauditservice.entity.Document;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface DocumentRepository extends R2dbcRepository<Document, UUID> {
    Flux<Document> findByEntityIdAndEntityType(UUID entityId, String entityType);
    Flux<Document> findByUploadedBy(UUID uploadedBy);
}
