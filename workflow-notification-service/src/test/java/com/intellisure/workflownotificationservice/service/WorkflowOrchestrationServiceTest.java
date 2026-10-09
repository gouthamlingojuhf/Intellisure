package com.intellisure.workflownotificationservice.service;

import com.intellisure.workflownotificationservice.dto.CompleteWorkflowTaskRequest;
import com.intellisure.workflownotificationservice.dto.CreateWorkflowRequest;
import com.intellisure.workflownotificationservice.dto.CreateWorkflowTaskRequest;
import com.intellisure.workflownotificationservice.dto.TaskFilterRequest;
import com.intellisure.workflownotificationservice.entity.*;
import com.intellisure.workflownotificationservice.repository.WorkflowRepository;
import com.intellisure.workflownotificationservice.repository.WorkflowTaskRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkflowOrchestrationServiceTest")
class WorkflowOrchestrationServiceTest {

    @Mock
    private WorkflowRepository workflowRepository;

    @Mock
    private WorkflowTaskRepository taskRepository;

    @Mock
    private AsyncNotificationDispatchService asyncDispatchService;

    @Mock
    private SlaMonitoringService slaMonitoringService;

    @InjectMocks
    private WorkflowOrchestrationService service;

    @Test
    @DisplayName("createWorkflow generates initial tasks for UNDERWRITING_REFERRAL")
    void createWorkflowForUnderwritingReferral() {
        UUID referenceId = UUID.randomUUID();
        CreateWorkflowRequest request = new CreateWorkflowRequest(
                "UNDERWRITING_REFERRAL", referenceId, "RISK_ASSESSMENT", "REFERRAL_INITIATED", "underwriter-1");

        when(workflowRepository.save(any(Workflow.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(taskRepository.save(any(WorkflowTask.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(service.createWorkflow(request))
                .assertNext(response -> {
                    assertEquals("UNDERWRITING_REFERRAL", response.workflowType());
                    assertEquals(referenceId, response.referenceId());
                    assertEquals("INITIATED", response.status());
                })
                .verifyComplete();

        verify(workflowRepository).save(any(Workflow.class));
        verify(taskRepository, atLeast(2)).save(any(WorkflowTask.class));
    }

    @Test
    @DisplayName("createWorkflow generates initial tasks for RECOVERY_COORDINATION")
    void createWorkflowForRecoveryCoordination() {
        UUID referenceId = UUID.randomUUID();
        CreateWorkflowRequest request = new CreateWorkflowRequest(
                "RECOVERY_COORDINATION", referenceId, "RECOVERY_CASE", "PLANNING", "adjuster-1");

        when(workflowRepository.save(any(Workflow.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(taskRepository.save(any(WorkflowTask.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(service.createWorkflow(request))
                .assertNext(response -> {
                    assertEquals("RECOVERY_COORDINATION", response.workflowType());
                    assertEquals("INITIATED", response.status());
                })
                .verifyComplete();

        verify(taskRepository, atLeast(3)).save(any(WorkflowTask.class));
    }

    @Test
    @DisplayName("createTask persists standalone task and sends notification if assignee provided")
    void createStandaloneTask() {
        UUID workflowId = UUID.randomUUID();
        UUID assignee = UUID.randomUUID();
        CreateWorkflowTaskRequest request = new CreateWorkflowTaskRequest(
                workflowId, "CLAIM_ASSESSMENT", assignee, LocalDateTime.now().plusDays(3), "Assess property loss");

        when(taskRepository.save(any(WorkflowTask.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(asyncDispatchService.createInAppNotification(eq(assignee), any(), any(), any(), any(), any()))
                .thenReturn(Mono.empty());

        StepVerifier.create(service.createTask(request))
                .assertNext(response -> {
                    assertEquals("CLAIM_ASSESSMENT", response.taskType());
                    assertEquals(assignee, response.assigneeUserId());
                    assertEquals("ASSIGNED", response.status());
                })
                .verifyComplete();

        verify(taskRepository).save(any(WorkflowTask.class));
        verify(asyncDispatchService).createInAppNotification(eq(assignee), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("completeTask marks task completed and triggers workflow status check")
    void completeTaskSuccessfully() {
        UUID taskId = UUID.randomUUID();
        UUID workflowId = UUID.randomUUID();
        WorkflowTask task = WorkflowTask.builder()
                .taskId(taskId)
                .workflowId(workflowId)
                .taskType("INVESTIGATION")
                .status(WorkflowTaskStatus.IN_PROGRESS)
                .isNew(false)
                .build();

        Workflow workflow = Workflow.builder()
                .workflowId(workflowId)
                .status(WorkflowStatus.IN_PROGRESS)
                .isNew(false)
                .build();

        when(taskRepository.findByTaskIdAndWorkflowId(taskId, workflowId)).thenReturn(Mono.just(task));
        when(taskRepository.save(any(WorkflowTask.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(workflowRepository.findById(workflowId)).thenReturn(Mono.just(workflow));
        when(taskRepository.findByWorkflowId(workflowId)).thenReturn(Flux.just(task));
        when(workflowRepository.save(any(Workflow.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        CompleteWorkflowTaskRequest request = new CompleteWorkflowTaskRequest(
                taskId, workflowId, "APPROVED", "Investigation completed satisfactorily");

        StepVerifier.create(service.completeTask(request))
                .assertNext(response -> {
                    assertEquals(taskId, response.taskId());
                    assertEquals("COMPLETED", response.status());
                    assertEquals("APPROVED", response.outcome());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("escalateTask marks status ESCALATED and triggers notification")
    void escalateTaskSuccessfully() {
        UUID taskId = UUID.randomUUID();
        UUID assignee = UUID.randomUUID();
        WorkflowTask task = WorkflowTask.builder()
                .taskId(taskId)
                .workflowId(UUID.randomUUID())
                .taskType("SLA_REVIEW")
                .assigneeUserId(assignee)
                .status(WorkflowTaskStatus.IN_PROGRESS)
                .isNew(false)
                .build();

        when(taskRepository.findById(taskId)).thenReturn(Mono.just(task));
        when(taskRepository.save(any(WorkflowTask.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(asyncDispatchService.createInAppNotification(eq(assignee), any(), any(), any(), any(), any()))
                .thenReturn(Mono.empty());

        StepVerifier.create(service.escalateTask(taskId, "SLA approaching breach"))
                .assertNext(response -> {
                    assertEquals(taskId, response.taskId());
                    assertEquals("ESCALATED", response.status());
                    assertEquals("ESCALATED", response.outcome());
                })
                .verifyComplete();

        verify(asyncDispatchService).createInAppNotification(eq(assignee), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("assignTask assigns user and updates status to ASSIGNED")
    void assignTaskSuccessfully() {
        UUID taskId = UUID.randomUUID();
        UUID assignee = UUID.randomUUID();
        WorkflowTask task = WorkflowTask.builder()
                .taskId(taskId)
                .workflowId(UUID.randomUUID())
                .taskType("DOCUMENT_REVIEW")
                .status(WorkflowTaskStatus.PENDING)
                .isNew(false)
                .build();

        when(taskRepository.findById(taskId)).thenReturn(Mono.just(task));
        when(taskRepository.save(any(WorkflowTask.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(asyncDispatchService.createInAppNotification(eq(assignee), any(), any(), any(), any(), any()))
                .thenReturn(Mono.empty());

        StepVerifier.create(service.assignTask(taskId, assignee))
                .verifyComplete();

        assertEquals(assignee, task.getAssigneeUserId());
        assertEquals(WorkflowTaskStatus.ASSIGNED, task.getStatus());
    }

    @Test
    void createWorkflowCoversEverySupportedWorkflowTypeAndDefault() {
        when(workflowRepository.save(any(Workflow.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(taskRepository.save(any(WorkflowTask.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        List<String> types = List.of("QUOTE_UNDERWRITING", "UNDERWRITING_REFERRAL", "CLAIM_PROCESSING",
                "CLAIM_INVESTIGATION", "CLAIM_ASSESSMENT", "POLICY_ISSUANCE", "POLICY_SERVICING",
                "RECOVERY_COORDINATION", "VENDOR_ASSIGNMENT", "DOCUMENT_EVIDENCE_REVIEW", "RENEWAL_PROCESSING",
                "UNSUPPORTED_TYPE");
        for (String type : types) {
            StepVerifier.create(service.createWorkflow(new CreateWorkflowRequest(
                            type, UUID.randomUUID(), "REFERENCE", "actor", null)))
                    .assertNext(response -> assertEquals("CREATED", response.currentStep()))
                    .verifyComplete();
        }
        verify(taskRepository, atLeast(types.size())).save(any(WorkflowTask.class));
    }

    @Test
    void getWorkflowAndWorkflowQueriesUseEachRepositoryFilter() {
        UUID id = UUID.randomUUID();
        Workflow workflow = workflow(id, WorkflowStatus.IN_PROGRESS);
        when(workflowRepository.findById(id)).thenReturn(Mono.just(workflow));
        when(workflowRepository.findByWorkflowType("CLAIM_PROCESSING")).thenReturn(Flux.just(workflow));
        when(workflowRepository.findByStatus(WorkflowStatus.COMPLETED)).thenReturn(Flux.just(workflow));
        when(workflowRepository.findByInitiatedBy("actor")).thenReturn(Flux.just(workflow));
        when(workflowRepository.findByStartedAtBetween(any(), any())).thenReturn(Flux.just(workflow));
        when(workflowRepository.findAll()).thenReturn(Flux.just(workflow));

        StepVerifier.create(service.getWorkflow(id)).assertNext(result -> assertEquals(id, result.workflowId())).verifyComplete();
        StepVerifier.create(service.getWorkflows("CLAIM_PROCESSING", null, null, null, null, 0, 10)).assertNext(r -> assertEquals(1, r.items().size())).verifyComplete();
        StepVerifier.create(service.getWorkflows(null, WorkflowStatus.COMPLETED, null, null, null, 0, 10)).assertNext(r -> assertEquals(1, r.items().size())).verifyComplete();
        StepVerifier.create(service.getWorkflows(null, null, "actor", null, null, 0, 10)).assertNext(r -> assertEquals(1, r.items().size())).verifyComplete();
        StepVerifier.create(service.getWorkflows(null, null, null, LocalDateTime.now().minusDays(1), LocalDateTime.now(), 0, 10)).assertNext(r -> assertEquals(1, r.items().size())).verifyComplete();
        StepVerifier.create(service.getWorkflows(null, null, null, null, null, 0, 10)).assertNext(r -> assertEquals(1, r.items().size())).verifyComplete();
        verify(workflowRepository).findAll();
    }

    @Test
    void createAndEscalateUnassignedTaskDoNotNotify() {
        UUID workflowId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        when(taskRepository.save(any(WorkflowTask.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        StepVerifier.create(service.createTask(new CreateWorkflowTaskRequest(workflowId, "REVIEW", null, null, null)))
                .assertNext(result -> assertEquals("PENDING", result.status())).verifyComplete();

        WorkflowTask task = task(taskId, workflowId, null, WorkflowTaskStatus.IN_PROGRESS);
        when(taskRepository.findById(taskId)).thenReturn(Mono.just(task));
        StepVerifier.create(service.escalateTask(taskId, "late"))
                .assertNext(result -> assertEquals("ESCALATED", result.status())).verifyComplete();
        verify(asyncDispatchService, never()).createInAppNotification(any(), any(), any(), any(), any(), any());
    }

    @Test
    void completeTaskSupportsIdOnlyLookupAndUpdatesRejectedOrInProgressWorkflow() {
        UUID taskId = UUID.randomUUID();
        UUID workflowId = UUID.randomUUID();
        WorkflowTask task = task(taskId, workflowId, null, WorkflowTaskStatus.IN_PROGRESS);
        WorkflowTask sibling = task(UUID.randomUUID(), workflowId, null, WorkflowTaskStatus.REJECTED);
        Workflow workflow = workflow(workflowId, WorkflowStatus.IN_PROGRESS);
        when(taskRepository.findById(taskId)).thenReturn(Mono.just(task));
        when(taskRepository.save(any(WorkflowTask.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(workflowRepository.findById(workflowId)).thenReturn(Mono.just(workflow));
        when(taskRepository.findByWorkflowId(workflowId)).thenReturn(Flux.just(sibling));
        when(workflowRepository.save(any(Workflow.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(service.completeTask(new CompleteWorkflowTaskRequest(taskId, null, "DONE", null)))
                .assertNext(result -> assertEquals("COMPLETED", result.status())).verifyComplete();
        assertEquals(WorkflowStatus.REJECTED, workflow.getStatus());
    }

    @Test
    void workflowStatusBecomesInProgressWhenTasksRemain() {
        UUID taskId = UUID.randomUUID();
        UUID workflowId = UUID.randomUUID();
        WorkflowTask task = task(taskId, workflowId, null, WorkflowTaskStatus.IN_PROGRESS);
        Workflow workflow = workflow(workflowId, WorkflowStatus.INITIATED);
        when(taskRepository.findById(taskId)).thenReturn(Mono.just(task));
        when(taskRepository.save(any(WorkflowTask.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(workflowRepository.findById(workflowId)).thenReturn(Mono.just(workflow));
        when(taskRepository.findByWorkflowId(workflowId)).thenReturn(Flux.just(task,
                task(UUID.randomUUID(), workflowId, null, WorkflowTaskStatus.PENDING)));
        when(workflowRepository.save(any(Workflow.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        StepVerifier.create(service.completeTask(new CompleteWorkflowTaskRequest(taskId, null, "DONE", "note")))
                .assertNext(result -> assertEquals("COMPLETED", result.status())).verifyComplete();
        assertEquals(WorkflowStatus.IN_PROGRESS, workflow.getStatus());
    }

    @Test
    void taskQueriesUseAllFilterPaths() {
        UUID workflowId = UUID.randomUUID();
        UUID assignee = UUID.randomUUID();
        WorkflowTask task = task(UUID.randomUUID(), workflowId, assignee, WorkflowTaskStatus.ASSIGNED);
        when(taskRepository.findByWorkflowId(workflowId)).thenReturn(Flux.just(task));
        when(taskRepository.findByAssigneeUserIdAndStatus(assignee, WorkflowTaskStatus.ASSIGNED)).thenReturn(Flux.just(task));
        when(taskRepository.findByAssigneeUserId(assignee)).thenReturn(Flux.just(task));
        when(taskRepository.findByStatus(WorkflowTaskStatus.ASSIGNED)).thenReturn(Flux.just(task));
        when(taskRepository.findByDueAtBefore(any())).thenReturn(Flux.just(task));
        when(taskRepository.findAll()).thenReturn(Flux.just(task));

        StepVerifier.create(service.getTasks(new TaskFilterRequest(workflowId, null, null, null, null, null, 0, 10))).assertNext(r -> assertEquals(1, r.items().size())).verifyComplete();
        StepVerifier.create(service.getTasks(new TaskFilterRequest(null, assignee, null, "ASSIGNED", null, null, 0, 10))).assertNext(r -> assertEquals(1, r.items().size())).verifyComplete();
        StepVerifier.create(service.getTasks(new TaskFilterRequest(null, assignee, null, null, null, null, 0, 10))).assertNext(r -> assertEquals(1, r.items().size())).verifyComplete();
        StepVerifier.create(service.getTasks(new TaskFilterRequest(null, null, null, "ASSIGNED", null, null, 0, 10))).assertNext(r -> assertEquals(1, r.items().size())).verifyComplete();
        StepVerifier.create(service.getTasks(new TaskFilterRequest(null, null, null, null, LocalDate.now(), null, 0, 10))).assertNext(r -> assertEquals(1, r.items().size())).verifyComplete();
        StepVerifier.create(service.getTasks(new TaskFilterRequest(null, null, null, null, null, null, 0, 10))).assertNext(r -> assertEquals(1, r.items().size())).verifyComplete();
    }

    @Test
    void missingTaskOperationsCompleteEmptyOrErrorAsDefined() {
        UUID id = UUID.randomUUID();
        when(taskRepository.findById(id)).thenReturn(Mono.empty());
        StepVerifier.create(service.escalateTask(id, "reason"))
                .expectError(IllegalArgumentException.class).verify();
        StepVerifier.create(service.assignTask(id, UUID.randomUUID())).verifyComplete();
    }

    private Workflow workflow(UUID id, WorkflowStatus status) {
        return Workflow.builder().workflowId(id).workflowType("TYPE").referenceId(UUID.randomUUID())
                .referenceType("REFERENCE").status(status).currentStep("STEP").initiatedBy("actor")
                .startedAt(LocalDateTime.now()).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .isNew(false).build();
    }

    private WorkflowTask task(UUID id, UUID workflowId, UUID assignee, WorkflowTaskStatus status) {
        return WorkflowTask.builder().taskId(id).workflowId(workflowId).taskType("TASK")
                .assigneeUserId(assignee).status(status).dueAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).isNew(false).build();
    }
}
