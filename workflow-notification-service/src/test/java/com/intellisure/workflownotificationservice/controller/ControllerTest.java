package com.intellisure.workflownotificationservice.controller;

import com.intellisure.workflownotificationservice.dto.*;
import com.intellisure.workflownotificationservice.entity.NotificationType;
import com.intellisure.workflownotificationservice.entity.WorkflowStatus;
import com.intellisure.workflownotificationservice.service.NotificationService;
import com.intellisure.workflownotificationservice.service.WorkflowOrchestrationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ControllerTest {
    @Mock NotificationService notificationService;
    @Mock WorkflowOrchestrationService workflowService;
    @InjectMocks NotificationController notificationController;
    @InjectMocks WorkflowController workflowController;
    @InjectMocks WorkflowTaskController workflowTaskController;

    @Test
    void notificationControllerDelegatesAllEndpoints() {
        UUID user = UUID.randomUUID();
        UUID notification = UUID.randomUUID();
        CreateNotificationRequest create = new CreateNotificationRequest(user, "GENERAL_ALERT", "t", "m", null, null, null);
        MarkNotificationReadRequest read = new MarkNotificationReadRequest(notification);
        when(notificationService.createNotification(create)).thenReturn(Mono.empty());
        when(notificationService.getNotifications(user, false, 0, 20)).thenReturn(Mono.empty());
        when(notificationService.getUnreadCount(user)).thenReturn(Mono.just(0L));
        when(notificationService.getNotificationsByType(user, NotificationType.GENERAL_ALERT, 0, 20)).thenReturn(Mono.empty());
        when(notificationService.markRead(read)).thenReturn(Mono.empty());

        notificationController.createNotification(create);
        notificationController.getNotifications(user, false, 0, 20);
        notificationController.getUnreadCount(user);
        notificationController.getNotificationsByType(user, "general_alert", 0, 20);
        notificationController.markRead(notification, read);
        notificationController.markReadById(notification);
        verify(notificationService).createNotification(create);
        verify(notificationService, times(2)).markRead(any(MarkNotificationReadRequest.class));
    }

    @Test
    void workflowControllerDelegatesCreateGetAndAllQueryModes() {
        UUID workflowId = UUID.randomUUID();
        CreateWorkflowRequest create = new CreateWorkflowRequest("CLAIM_PROCESSING", workflowId, "CLAIM", "actor", null);
        when(workflowService.createWorkflow(create)).thenReturn(Mono.empty());
        when(workflowService.getWorkflow(workflowId)).thenReturn(Mono.empty());
        when(workflowService.getWorkflows(any(), any(), any(), any(), any(), any(), any())).thenReturn(Mono.empty());

        workflowController.createWorkflow(create);
        workflowController.getWorkflow(workflowId);
        workflowController.getWorkflows("CLAIM_PROCESSING", "IN_PROGRESS", "actor",
                LocalDateTime.now().minusDays(1), LocalDateTime.now(), 0, 20);
        verify(workflowService).getWorkflows(eq("CLAIM_PROCESSING"), eq(WorkflowStatus.IN_PROGRESS), eq("actor"), any(), any(), eq(0), eq(20));
    }

    @Test
    void taskControllerSetsPathWorkflowIdsAndBuildsFilters() {
        UUID workflowId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        UUID assignee = UUID.randomUUID();
        CreateWorkflowTaskRequest create = new CreateWorkflowTaskRequest(null, "REVIEW", assignee, null, "note");
        CompleteWorkflowTaskRequest complete = new CompleteWorkflowTaskRequest(null, null, "DONE", "complete");
        when(workflowService.createTask(any())).thenReturn(Mono.empty());
        when(workflowService.getTasks(any())).thenReturn(Mono.empty());
        when(workflowService.completeTask(any())).thenReturn(Mono.empty());
        when(workflowService.assignTask(taskId, assignee)).thenReturn(Mono.empty());
        when(workflowService.escalateTask(taskId, "late")).thenReturn(Mono.empty());

        workflowTaskController.createTaskForWorkflow(workflowId, create);
        workflowTaskController.createTask(create);
        workflowTaskController.getTasks(workflowId, assignee, "REVIEW", "ASSIGNED", LocalDate.now(), null, 0, 20);
        workflowTaskController.completeTask(taskId, complete);
        workflowTaskController.completeTaskWithWorkflow(workflowId, taskId, complete);
        workflowTaskController.assignTask(taskId, assignee);
        workflowTaskController.escalateTask(taskId, "late");
        workflowTaskController.getAllTasks(assignee, "ASSIGNED", 0, 20);

        ArgumentCaptor<CreateWorkflowTaskRequest> createCaptor = ArgumentCaptor.forClass(CreateWorkflowTaskRequest.class);
        verify(workflowService, times(2)).createTask(createCaptor.capture());
        assertEquals(workflowId, createCaptor.getAllValues().get(0).workflowId());
        ArgumentCaptor<CompleteWorkflowTaskRequest> completeCaptor = ArgumentCaptor.forClass(CompleteWorkflowTaskRequest.class);
        verify(workflowService, times(2)).completeTask(completeCaptor.capture());
        assertEquals(workflowId, completeCaptor.getAllValues().get(1).workflowId());
        verify(workflowService).assignTask(taskId, assignee);
        verify(workflowService).escalateTask(taskId, "late");
    }
}
