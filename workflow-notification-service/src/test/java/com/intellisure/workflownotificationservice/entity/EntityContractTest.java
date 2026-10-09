package com.intellisure.workflownotificationservice.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EntityContractTest {
    @Test
    void notificationEntitySupportsPersistenceContractAndAccessors() {
        UUID id = UUID.randomUUID();
        Notification entity = Notification.builder().notificationId(id).userId(id).type(NotificationType.GENERAL_ALERT)
                .title("title").message("message").referenceType("QUOTE").referenceId(id).read(false)
                .channel(NotificationChannel.IN_APP).createdAt(LocalDateTime.now()).isNew(true).build();
        assertEquals(id, entity.getId());
        assertTrue(entity.isNew());
        entity.setNotificationId(id); entity.setUserId(id); entity.setType(NotificationType.TASK_ASSIGNED);
        entity.setTitle("new"); entity.setMessage("new message"); entity.setReferenceType("TASK"); entity.setReferenceId(id);
        entity.setRead(true); entity.setChannel(NotificationChannel.EMAIL); entity.setReadAt(LocalDateTime.now());
        entity.setCreatedAt(LocalDateTime.now()); entity.setNew(false);
        assertEquals(NotificationType.TASK_ASSIGNED, entity.getType());
        assertFalse(entity.isNew());
    }

    @Test
    void workflowEntitySupportsPersistenceContractAndAccessors() {
        UUID id = UUID.randomUUID();
        Workflow entity = Workflow.builder().workflowId(id).workflowType("TYPE").referenceId(id)
                .referenceType("REFERENCE").status(WorkflowStatus.INITIATED).currentStep("START")
                .initiatedBy("actor").startedAt(LocalDateTime.now()).createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now()).isNew(true).build();
        assertEquals(id, entity.getId());
        assertTrue(entity.isNew());
        entity.setWorkflowId(id); entity.setWorkflowType("NEW"); entity.setReferenceId(id); entity.setReferenceType("NEW_REF");
        entity.setStatus(WorkflowStatus.COMPLETED); entity.setCurrentStep("DONE"); entity.setInitiatedBy("new-actor");
        entity.setStartedAt(LocalDateTime.now()); entity.setCompletedAt(LocalDateTime.now()); entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now()); entity.setNew(false);
        assertEquals(WorkflowStatus.COMPLETED, entity.getStatus());
        assertFalse(entity.isNew());
    }

    @Test
    void workflowTaskEntitySupportsCustomBuilderAndAccessors() {
        UUID id = UUID.randomUUID();
        WorkflowTask entity = WorkflowTask.builder().taskId(id).workflowId(id).taskType("REVIEW")
                .assigneeUserId(id).status(WorkflowTaskStatus.ASSIGNED).dueAt(LocalDateTime.now())
                .outcome("DONE").completionNote("note").completedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).isNew(true).build();
        assertEquals(id, entity.getId());
        assertTrue(entity.isNew());
        entity.setTaskId(id); entity.setWorkflowId(id); entity.setTaskType("NEW"); entity.setAssigneeUserId(id);
        entity.setStatus(WorkflowTaskStatus.COMPLETED); entity.setDueAt(LocalDateTime.now()); entity.setOutcome("OK");
        entity.setCompletionNote("done"); entity.setCompletedAt(LocalDateTime.now()); entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now()); entity.setNew(false);
        assertEquals(WorkflowTaskStatus.COMPLETED, entity.getStatus());
        assertFalse(entity.isNew());
    }
}
