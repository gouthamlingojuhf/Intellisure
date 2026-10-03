package com.intellisure.workflownotificationservice.controller;

import com.intellisure.workflownotificationservice.dto.CompleteWorkflowTaskRequest;
import com.intellisure.workflownotificationservice.dto.TaskFilterRequest;
import com.intellisure.workflownotificationservice.dto.WorkflowTaskListResponse;
import com.intellisure.workflownotificationservice.service.WorkflowOrchestrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/workflows")
@RequiredArgsConstructor
public class WorkflowTaskController {

    private final WorkflowOrchestrationService workflowService;

    @GetMapping("/{workflowId}/tasks")
    public Mono<WorkflowTaskListResponse> getTasks(
            @PathVariable UUID workflowId,
            @RequestParam(required = false) UUID assigneeUserId,
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        TaskFilterRequest filter = new TaskFilterRequest(
            workflowId, assigneeUserId, taskType, status, fromDate, toDate, page, size);
        return workflowService.getTasks(filter);
    }

    @PostMapping("/tasks/{taskId}/complete")
    public Mono<com.intellisure.workflownotificationservice.dto.WorkflowTaskResponse> completeTask(
            @PathVariable UUID workflowId,
            @PathVariable UUID taskId,
            @Valid @RequestBody CompleteWorkflowTaskRequest request) {
        
        CompleteWorkflowTaskRequest updatedRequest = new CompleteWorkflowTaskRequest(
            taskId, workflowId, request.outcome(), request.completionNote());
        return workflowService.completeTask(updatedRequest);
    }

    @PostMapping("/tasks/{taskId}/assign")
    public Mono<Void> assignTask(
            @PathVariable UUID taskId,
            @RequestParam UUID assigneeUserId) {
        return workflowService.assignTask(taskId, assigneeUserId);
    }

    @GetMapping("/tasks")
    public Mono<WorkflowTaskListResponse> getAllTasks(
            @RequestParam(required = false) UUID assigneeUserId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        TaskFilterRequest filter = new TaskFilterRequest(
            null, assigneeUserId, null, status, null, null, page, size);
        return workflowService.getTasks(filter);
    }
}