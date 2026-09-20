package com.intellisure.workflownotificationservice.repository;

import com.intellisure.workflownotificationservice.entity.Workflow;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface WorkflowRepository extends R2dbcRepository<Workflow, UUID> {
    Mono<Workflow> findByReferenceIdAndReferenceType(UUID referenceId, String referenceType);
    Flux<Workflow> findByStatus(String status);
}
