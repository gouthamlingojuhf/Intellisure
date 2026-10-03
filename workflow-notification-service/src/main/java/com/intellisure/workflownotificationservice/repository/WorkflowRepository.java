package com.intellisure.workflownotificationservice.repository;

import com.intellisure.workflownotificationservice.entity.Workflow;
import com.intellisure.workflownotificationservice.entity.WorkflowStatus;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

public interface WorkflowRepository extends R2dbcRepository<Workflow, UUID> {
    Mono<Workflow> findByReferenceIdAndReferenceType(UUID referenceId, String referenceType);
    Flux<Workflow> findByStatus(WorkflowStatus status);
    Flux<Workflow> findByWorkflowType(String workflowType);
    Flux<Workflow> findByInitiatedBy(String initiatedBy);
    Flux<Workflow> findByStartedAtBetween(LocalDateTime start, LocalDateTime end);
    Mono<Long> countByStatus(WorkflowStatus status);
}