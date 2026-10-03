package com.intellisure.workflownotificationservice.service;

import com.intellisure.workflownotificationservice.entity.Notification;
import com.intellisure.workflownotificationservice.entity.NotificationChannel;
import com.intellisure.workflownotificationservice.entity.NotificationType;
import com.intellisure.workflownotificationservice.entity.WorkflowTask;
import com.intellisure.workflownotificationservice.entity.WorkflowTaskStatus;
import com.intellisure.workflownotificationservice.repository.NotificationRepository;
import com.intellisure.workflownotificationservice.repository.WorkflowTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlaMonitoringService {

    private final WorkflowTaskRepository taskRepository;
    private final NotificationRepository notificationRepository;

    @Async("slaExecutor")
    @Scheduled(fixedRate = 300000)
    public void checkOverdueTasks() {
        log.debug("Checking for overdue tasks...");
        LocalDateTime now = LocalDateTime.now();
        
        Flux<WorkflowTask> overdueTasks = taskRepository.findByDueAtBefore(now);
        
        overdueTasks
            .filter(task -> task.getStatus() == WorkflowTaskStatus.ASSIGNED || 
                           task.getStatus() == WorkflowTaskStatus.IN_PROGRESS ||
                           task.getStatus() == WorkflowTaskStatus.PENDING)
            .flatMap(this::escalateTask)
            .subscribe(
                result -> log.debug("Escalated task: {}", result),
                error -> log.error("Error escalating task: {}", error.getMessage())
            );
    }

    @Async("slaExecutor")
    @Scheduled(fixedRate = 600000)
    public void checkSlaWarnings() {
        log.debug("Checking for SLA warnings...");
        LocalDateTime warningThreshold = LocalDateTime.now().plus(1, ChronoUnit.HOURS);
        
        Flux<WorkflowTask> upcomingTasks = taskRepository.findByDueAtBefore(warningThreshold);
        
        upcomingTasks
            .filter(task -> task.getStatus() == WorkflowTaskStatus.ASSIGNED || 
                           task.getStatus() == WorkflowTaskStatus.IN_PROGRESS ||
                           task.getStatus() == WorkflowTaskStatus.PENDING)
            .flatMap(this::sendSlaWarning)
            .subscribe(
                result -> log.debug("Sent SLA warning for task: {}", result),
                error -> log.error("Error sending SLA warning: {}", error.getMessage())
            );
    }

    private Mono<Void> escalateTask(WorkflowTask task) {
        if (task.getAssigneeUserId() == null) {
            return Mono.empty();
        }

        task.setStatus(WorkflowTaskStatus.ESCALATED);
        task.setUpdatedAt(LocalDateTime.now());
        
        return taskRepository.save(task)
            .flatMap(this::createEscalationNotification)
            .then();
    }

    private Mono<Void> sendSlaWarning(WorkflowTask task) {
        if (task.getAssigneeUserId() == null) {
            return Mono.empty();
        }

        return createSlaWarningNotification(task)
            .then();
    }

    private Mono<Notification> createEscalationNotification(WorkflowTask task) {
        Notification notification = Notification.builder()
            .notificationId(UUID.randomUUID())
            .userId(task.getAssigneeUserId())
            .type(NotificationType.TASK_OVERDUE)
            .title("Task Overdue: " + task.getTaskType())
            .message("Task " + task.getTaskId() + " is overdue. Due at: " + task.getDueAt())
            .referenceType("WORKFLOW_TASK")
            .referenceId(task.getTaskId())
            .read(false)
            .channel(NotificationChannel.IN_APP)
            .createdAt(LocalDateTime.now())
            .isNew(true)
            .build();

        return notificationRepository.save(notification);
    }

    private Mono<Notification> createSlaWarningNotification(WorkflowTask task) {
        Notification notification = Notification.builder()
            .notificationId(UUID.randomUUID())
            .userId(task.getAssigneeUserId())
            .type(NotificationType.SLA_WARNING)
            .title("SLA Warning: " + task.getTaskType())
            .message("Task " + task.getTaskId() + " is approaching SLA deadline. Due at: " + task.getDueAt())
            .referenceType("WORKFLOW_TASK")
            .referenceId(task.getTaskId())
            .read(false)
            .channel(NotificationChannel.IN_APP)
            .createdAt(LocalDateTime.now())
            .isNew(true)
            .build();

        return notificationRepository.save(notification);
    }

    public CompletableFuture<Void> triggerSlaCheckAsync() {
        return CompletableFuture.runAsync(() -> {
            checkOverdueTasks();
            checkSlaWarnings();
        });
    }
}