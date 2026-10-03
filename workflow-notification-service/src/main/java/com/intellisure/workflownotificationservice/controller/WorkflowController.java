package com.intellisure.workflownotificationservice.controller;

import com.intellisure.workflownotificationservice.dto.CreateWorkflowRequest;
import com.intellisure.workflownotificationservice.dto.TaskFilterRequest;
import com.intellisure.workflownotificationservice.dto.WorkflowListResponse;
import com.intellisure.workflownotificationservice.dto.WorkflowResponse;
import com.intellisure.workflownotificationservice.dto.WorkflowTaskListResponse;
import com.intellisure.workflownotificationservice.service.WorkflowOrchestrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowOrchestrationService workflowService;

    @PostMapping
    public Mono<WorkflowResponse> createWorkflow(@Valid @RequestBody CreateWorkflowRequest request) {
        return workflowService.createWorkflow(request);
    }

    @GetMapping("/{workflowId}")
    public Mono<WorkflowResponse> getWorkflow(@PathVariable UUID workflowId) {
        return workflowService.getWorkflow(workflowId);
    }

    @GetMapping
    public Mono<WorkflowListResponse> getWorkflows(
            @RequestParam(required = false) String workflowType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String initiatedBy,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return workflowService.getWorkflows(workflowType, 
            status != null ? com.intellisure.workflownotificationservice.entity.WorkflowStatus.valueOf(status) : null,
            initiatedBy, startDate, endDate, page, size);
    }
}