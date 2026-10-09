package com.intellisure.workflownotificationservice.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DtoContractTest {
    @Test
    void recordDtosExposeStableApiShapes() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        assertEquals(id, new CompleteWorkflowTaskRequest(id, id, "DONE", "note").taskId());
        assertEquals(id, new CreateNotificationRequest(id, "GENERAL_ALERT", "t", "m", "QUOTE", id, "IN_APP").userId());
        assertEquals("TYPE", new CreateWorkflowRequest("TYPE", id, "REF", "actor", "STEP").workflowType());
        assertEquals(id, new CreateWorkflowTaskRequest(id, "TASK", id, now, "note").workflowId());
        assertEquals(id, new MarkNotificationReadRequest(id).notificationId());
        assertEquals("title", new NotificationResponse(id, id, "TYPE", "title", "message", "REF", id, false, "IN_APP", now, now).title());
        assertEquals("EMAIL", new SendNotificationRequest(id, "title", "message", "EMAIL").channel());
        assertEquals(LocalDate.of(2026, 1, 1), new TaskFilterRequest(id, id, "TASK", "PENDING", LocalDate.of(2026, 1, 1), null, 0, 20).fromDate());
        WorkflowResponse workflow = new WorkflowResponse(id, "TYPE", id, "REF", "INITIATED", "STEP", "actor", now, null, now, now);
        WorkflowTaskResponse task = new WorkflowTaskResponse(id, id, "TASK", id, "PENDING", now, null, null, null, now, now);
        assertEquals(workflow, new WorkflowListResponse(List.of(workflow), 0, 20, 1L).items().get(0));
        assertEquals(task, new WorkflowTaskListResponse(List.of(task), 0, 20, 1L).items().get(0));
        assertEquals(1, new NotificationListResponse(List.of(new NotificationResponse(id, id, "TYPE", "t", "m", null, null, false, "IN_APP", null, now)), 0, 20, 1L).items().size());
    }
}
