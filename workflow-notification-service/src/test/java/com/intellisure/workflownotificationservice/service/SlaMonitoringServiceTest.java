package com.intellisure.workflownotificationservice.service;

import com.intellisure.workflownotificationservice.entity.Notification;
import com.intellisure.workflownotificationservice.entity.WorkflowTask;
import com.intellisure.workflownotificationservice.entity.WorkflowTaskStatus;
import com.intellisure.workflownotificationservice.repository.NotificationRepository;
import com.intellisure.workflownotificationservice.repository.WorkflowTaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlaMonitoringServiceTest {

    @Mock WorkflowTaskRepository taskRepository;
    @Mock NotificationRepository notificationRepository;
    @InjectMocks SlaMonitoringService service;

    @Test
    void overdueCheckEscalatesOpenTasksAndSkipsClosedOrUnassignedTasks() {
        UUID assignee = UUID.randomUUID();
        WorkflowTask assigned = task(assignee, WorkflowTaskStatus.ASSIGNED);
        WorkflowTask pendingWithoutAssignee = task(null, WorkflowTaskStatus.PENDING);
        WorkflowTask completed = task(assignee, WorkflowTaskStatus.COMPLETED);
        when(taskRepository.findByDueAtBefore(any())).thenReturn(Flux.just(assigned, pendingWithoutAssignee, completed));
        when(taskRepository.save(any(WorkflowTask.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        service.checkOverdueTasks();

        assertEquals(WorkflowTaskStatus.ESCALATED, assigned.getStatus());
        verify(taskRepository, times(1)).save(assigned);
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void warningCheckNotifiesOpenAssignedTasksOnly() {
        UUID assignee = UUID.randomUUID();
        WorkflowTask inProgress = task(assignee, WorkflowTaskStatus.IN_PROGRESS);
        WorkflowTask pendingWithoutAssignee = task(null, WorkflowTaskStatus.PENDING);
        WorkflowTask rejected = task(assignee, WorkflowTaskStatus.REJECTED);
        when(taskRepository.findByDueAtBefore(any())).thenReturn(Flux.just(inProgress, pendingWithoutAssignee, rejected));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        service.checkSlaWarnings();

        verify(notificationRepository, times(1)).save(argThat(notification ->
                notification.getType().name().equals("SLA_WARNING") && notification.getUserId().equals(assignee)));
    }

    @Test
    void triggerSlaCheckRunsBothScheduledChecks() throws Exception {
        when(taskRepository.findByDueAtBefore(any())).thenReturn(Flux.empty());
        service.triggerSlaCheckAsync().get();
        verify(taskRepository, times(2)).findByDueAtBefore(any());
    }

    private WorkflowTask task(UUID assignee, WorkflowTaskStatus status) {
        return WorkflowTask.builder().taskId(UUID.randomUUID()).workflowId(UUID.randomUUID())
                .taskType("REVIEW").assigneeUserId(assignee).status(status)
                .dueAt(LocalDateTime.now().minusHours(2)).createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now()).isNew(false).build();
    }
}
