package com.intellisure.workflownotificationservice.service;

import com.intellisure.workflownotificationservice.dto.CompleteWorkflowTaskRequest;
import com.intellisure.workflownotificationservice.dto.CreateWorkflowRequest;
import com.intellisure.workflownotificationservice.dto.CreateWorkflowTaskRequest;
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
}
