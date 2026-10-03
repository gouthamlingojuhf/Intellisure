package com.intellisure.workflownotificationservice.service;

import com.intellisure.workflownotificationservice.dto.CompleteWorkflowTaskRequest;
import com.intellisure.workflownotificationservice.dto.CreateWorkflowRequest;
import com.intellisure.workflownotificationservice.dto.TaskFilterRequest;
import com.intellisure.workflownotificationservice.dto.WorkflowListResponse;
import com.intellisure.workflownotificationservice.dto.WorkflowResponse;
import com.intellisure.workflownotificationservice.dto.WorkflowTaskListResponse;
import com.intellisure.workflownotificationservice.dto.WorkflowTaskResponse;
import com.intellisure.workflownotificationservice.entity.Workflow;
import com.intellisure.workflownotificationservice.entity.WorkflowStatus;
import com.intellisure.workflownotificationservice.entity.WorkflowTask;
import com.intellisure.workflownotificationservice.entity.WorkflowTaskStatus;
import com.intellisure.workflownotificationservice.repository.WorkflowRepository;
import com.intellisure.workflownotificationservice.repository.WorkflowTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowOrchestrationService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowTaskRepository taskRepository;
    private final AsyncNotificationDispatchService asyncDispatchService;
    private final SlaMonitoringService slaMonitoringService;

    public Mono<WorkflowResponse> createWorkflow(CreateWorkflowRequest request) {
        Workflow workflow = Workflow.builder()
            .workflowId(UUID.randomUUID())
            .workflowType(request.workflowType())
            .referenceId(request.referenceId())
            .referenceType(request.referenceType())
            .status(WorkflowStatus.INITIATED)
            .currentStep(request.currentStep() != null ? request.currentStep() : "CREATED")
            .initiatedBy(request.initiatedBy())
            .startedAt(LocalDateTime.now())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .isNew(true)
            .build();

        return workflowRepository.save(workflow)
            .flatMap(saved -> createInitialTasks(saved, request))
            .thenReturn(workflow)
            .map(this::mapToResponse);
    }

    private Mono<Void> createInitialTasks(Workflow workflow, CreateWorkflowRequest request) {
        List<WorkflowTask> tasks = createTasksForWorkflowType(workflow, request.workflowType());
        
        return Flux.fromIterable(tasks)
            .flatMap(taskRepository::save)
            .then();
    }

    private List<WorkflowTask> createTasksForWorkflowType(Workflow workflow, String workflowType) {
        return switch (workflowType) {
            case "QUOTE_UNDERWRITING" -> List.of(
                createTask(workflow, "QUOTE_REVIEW", "Underwriter review of quote submission"),
                createTask(workflow, "RISK_ASSESSMENT", "Risk engineer assessment"),
                createTask(workflow, "QUOTE_APPROVAL", "Underwriter approval decision")
            );
            case "CLAIM_PROCESSING" -> List.of(
                createTask(workflow, "FNOL_REVIEW", "Initial FNOL review and triage"),
                createTask(workflow, "ADJUSTER_ASSIGNMENT", "Assign claim to adjuster"),
                createTask(workflow, "INVESTIGATION", "Claim investigation and assessment"),
                createTask(workflow, "SETTLEMENT_APPROVAL", "Settlement approval")
            );
            case "POLICY_ISSUANCE" -> List.of(
                createTask(workflow, "QUOTE_ACCEPTANCE", "Customer acceptance of quote"),
                createTask(workflow, "BIND_COVERAGE", "Bind coverage"),
                createTask(workflow, "ISSUE_POLICY", "Issue policy documents")
            );
            case "VENDOR_ASSIGNMENT" -> List.of(
                createTask(workflow, "VENDOR_MATCHING", "Match eligible vendor"),
                createTask(workflow, "DISPATCH_WORK_ORDER", "Dispatch work order to vendor"),
                createTask(workflow, "VENDOR_COMPLETION", "Vendor completes work")
            );
            case "RENEWAL_PROCESSING" -> List.of(
                createTask(workflow, "RENEWAL_INITIATION", "Initiate renewal process"),
                createTask(workflow, "RE_QUOTE", "Generate renewal quote"),
                createTask(workflow, "CUSTOMER_NEGOTIATION", "Customer negotiation"),
                createTask(workflow, "RENEWAL_BINDING", "Bind renewal")
            );
            default -> List.of(
                createTask(workflow, "GENERAL_TASK", "General workflow task")
            );
        };
    }

    private WorkflowTask createTask(Workflow workflow, String taskType, String description) {
        return WorkflowTask.builder()
            .taskId(UUID.randomUUID())
            .workflowId(workflow.getWorkflowId())
            .taskType(taskType)
            .status(WorkflowTaskStatus.PENDING)
            .dueAt(LocalDateTime.now().plusDays(7))
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .isNew(true)
            .build();
    }

    public Mono<WorkflowResponse> getWorkflow(UUID workflowId) {
        return workflowRepository.findById(workflowId)
            .map(this::mapToResponse);
    }

    public Mono<WorkflowListResponse> getWorkflows(String workflowType, WorkflowStatus status, String initiatedBy, 
                                                     LocalDateTime startDate, LocalDateTime endDate, 
                                                     Integer page, Integer size) {
        Flux<Workflow> workflows;
        
        if (workflowType != null) {
            workflows = workflowRepository.findByWorkflowType(workflowType);
        } else if (status != null) {
            workflows = workflowRepository.findByStatus(status);
        } else if (initiatedBy != null) {
            workflows = workflowRepository.findByInitiatedBy(initiatedBy);
        } else if (startDate != null && endDate != null) {
            workflows = workflowRepository.findByStartedAtBetween(startDate, endDate);
        } else {
            workflows = workflowRepository.findAll();
        }
        
        return workflows
            .map(this::mapToResponse)
            .collectList()
            .map(list -> new WorkflowListResponse(list, page, size, (long) list.size()));
    }

    public Mono<WorkflowTaskResponse> completeTask(CompleteWorkflowTaskRequest request) {
        return taskRepository.findByTaskIdAndWorkflowId(request.taskId(), request.workflowId())
            .flatMap(task -> {
                task.setStatus(WorkflowTaskStatus.COMPLETED);
                task.setOutcome(request.outcome());
                task.setCompletionNote(request.completionNote());
                task.setCompletedAt(LocalDateTime.now());
                task.setUpdatedAt(LocalDateTime.now());
                task.setNew(false);
                
                return taskRepository.save(task)
                    .flatMap(saved -> 
                        checkAndUpdateWorkflowStatus(request.workflowId())
                            .thenReturn(saved)
                    );
            })
            .map(this::mapTaskToResponse);
    }

    private Mono<Workflow> checkAndUpdateWorkflowStatus(UUID workflowId) {
        return workflowRepository.findById(workflowId)
            .flatMap(workflow -> {
                return taskRepository.findByWorkflowId(workflowId)
                    .collectList()
                    .flatMap(tasks -> {
                        boolean allCompleted = tasks.stream()
                            .allMatch(t -> t.getStatus() == WorkflowTaskStatus.COMPLETED);
                        boolean anyRejected = tasks.stream()
                            .anyMatch(t -> t.getStatus() == WorkflowTaskStatus.REJECTED);
                        
                        if (allCompleted) {
                            workflow.setStatus(WorkflowStatus.COMPLETED);
                            workflow.setCompletedAt(LocalDateTime.now());
                        } else if (anyRejected) {
                            workflow.setStatus(WorkflowStatus.REJECTED);
                        } else {
                            workflow.setStatus(WorkflowStatus.IN_PROGRESS);
                        }
                        workflow.setUpdatedAt(LocalDateTime.now());
                        workflow.setNew(false);
                        return workflowRepository.save(workflow);
                    });
            });
    }

    public Mono<WorkflowTaskListResponse> getTasks(TaskFilterRequest filter) {
        Flux<WorkflowTask> tasks;
        
        if (filter.workflowId() != null) {
            tasks = taskRepository.findByWorkflowId(filter.workflowId());
        } else if (filter.assigneeUserId() != null && filter.status() != null) {
            tasks = taskRepository.findByAssigneeUserIdAndStatus(filter.assigneeUserId(), 
                WorkflowTaskStatus.valueOf(filter.status()));
        } else if (filter.assigneeUserId() != null) {
            tasks = taskRepository.findByAssigneeUserId(filter.assigneeUserId());
        } else if (filter.status() != null) {
            tasks = taskRepository.findByStatus(WorkflowTaskStatus.valueOf(filter.status()));
        } else if (filter.fromDate() != null) {
            tasks = taskRepository.findByDueAtBefore(filter.fromDate().atStartOfDay());
        } else {
            tasks = taskRepository.findAll();
        }
        
        return tasks
            .map(this::mapTaskToResponse)
            .collectList()
            .map(list -> new WorkflowTaskListResponse(list, filter.page(), filter.size(), (long) list.size()));
    }

    public Mono<Void> assignTask(UUID taskId, UUID assigneeUserId) {
        return taskRepository.findById(taskId)
            .flatMap(task -> {
                task.setAssigneeUserId(assigneeUserId);
                task.setStatus(WorkflowTaskStatus.ASSIGNED);
                task.setUpdatedAt(LocalDateTime.now());
                task.setNew(false);
                
                return taskRepository.save(task)
                    .flatMap(savedTask -> asyncDispatchService.createInAppNotification(
                        assigneeUserId,
                        com.intellisure.workflownotificationservice.entity.NotificationType.TASK_ASSIGNED,
                        "New Task Assigned: " + savedTask.getTaskType(),
                        "You have been assigned a new task: " + savedTask.getTaskType(),
                        "WORKFLOW_TASK",
                        taskId
                    ))
                    .then();
            });
    }

    private WorkflowResponse mapToResponse(Workflow workflow) {
        return new WorkflowResponse(
            workflow.getWorkflowId(),
            workflow.getWorkflowType(),
            workflow.getReferenceId(),
            workflow.getReferenceType(),
            workflow.getStatus().name(),
            workflow.getCurrentStep(),
            workflow.getInitiatedBy(),
            workflow.getStartedAt(),
            workflow.getCompletedAt(),
            workflow.getCreatedAt(),
            workflow.getUpdatedAt()
        );
    }

    private WorkflowTaskResponse mapTaskToResponse(WorkflowTask task) {
        return new WorkflowTaskResponse(
            task.getTaskId(),
            task.getWorkflowId(),
            task.getTaskType(),
            task.getAssigneeUserId(),
            task.getStatus().name(),
            task.getDueAt(),
            task.getOutcome(),
            task.getCompletionNote(),
            task.getCompletedAt(),
            task.getCreatedAt(),
            task.getUpdatedAt()
        );
    }
}