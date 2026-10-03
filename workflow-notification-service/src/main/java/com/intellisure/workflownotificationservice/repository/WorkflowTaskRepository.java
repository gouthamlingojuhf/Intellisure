package com.intellisure.workflownotificationservice.repository;

import com.intellisure.workflownotificationservice.entity.WorkflowTask;
import com.intellisure.workflownotificationservice.entity.WorkflowTaskStatus;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

public interface WorkflowTaskRepository extends R2dbcRepository<WorkflowTask, UUID> {
    Flux<WorkflowTask> findByWorkflowId(UUID workflowId);
    Flux<WorkflowTask> findByAssigneeUserId(UUID assigneeUserId);
    Flux<WorkflowTask> findByAssigneeUserIdAndStatus(UUID assigneeUserId, WorkflowTaskStatus status);
    Flux<WorkflowTask> findByStatus(WorkflowTaskStatus status);
    Flux<WorkflowTask> findByDueAtBefore(LocalDateTime dueAt);
    Flux<WorkflowTask> findByAssigneeUserIdAndDueAtBefore(UUID assigneeUserId, LocalDateTime dueAt);
    Mono<WorkflowTask> findByTaskIdAndWorkflowId(UUID taskId, UUID workflowId);
}