package com.intellisure.workflownotificationservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("workflow_task")
public class WorkflowTask implements Persistable<UUID> {

    @Id
    @Column("task_id")
    private UUID taskId;

    @Column("workflow_id")
    private UUID workflowId;

    @Column("task_type")
    private String taskType;

    @Column("assignee_user_id")
    private UUID assigneeUserId;

    private WorkflowTaskStatus status;

    @Column("due_at")
    private LocalDateTime dueAt;

    private String outcome;

    @Column("completion_note")
    private String completionNote;

    @Column("completed_at")
    private LocalDateTime completedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Transient
    private boolean isNew = true;

    public static WorkflowTask builder() {
        return new WorkflowTask();
    }

    public WorkflowTask taskId(UUID taskId) { this.taskId = taskId; return this; }
    public WorkflowTask workflowId(UUID workflowId) { this.workflowId = workflowId; return this; }
    public WorkflowTask taskType(String taskType) { this.taskType = taskType; return this; }
    public WorkflowTask assigneeUserId(UUID assigneeUserId) { this.assigneeUserId = assigneeUserId; return this; }
    public WorkflowTask status(WorkflowTaskStatus status) { this.status = status; return this; }
    public WorkflowTask dueAt(LocalDateTime dueAt) { this.dueAt = dueAt; return this; }
    public WorkflowTask outcome(String outcome) { this.outcome = outcome; return this; }
    public WorkflowTask completionNote(String completionNote) { this.completionNote = completionNote; return this; }
    public WorkflowTask completedAt(LocalDateTime completedAt) { this.completedAt = completedAt; return this; }
    public WorkflowTask createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
    public WorkflowTask updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
    public WorkflowTask isNew(boolean isNew) { this.isNew = isNew; return this; }
    public WorkflowTask build() { return this; }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @Override
    public UUID getId() {
        return taskId;
    }

    public UUID getTaskId() { return taskId; }
    public void setTaskId(UUID taskId) { this.taskId = taskId; }
    public UUID getWorkflowId() { return workflowId; }
    public void setWorkflowId(UUID workflowId) { this.workflowId = workflowId; }
    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }
    public UUID getAssigneeUserId() { return assigneeUserId; }
    public void setAssigneeUserId(UUID assigneeUserId) { this.assigneeUserId = assigneeUserId; }
    public WorkflowTaskStatus getStatus() { return status; }
    public void setStatus(WorkflowTaskStatus status) { this.status = status; }
    public LocalDateTime getDueAt() { return dueAt; }
    public void setDueAt(LocalDateTime dueAt) { this.dueAt = dueAt; }
    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }
    public String getCompletionNote() { return completionNote; }
    public void setCompletionNote(String completionNote) { this.completionNote = completionNote; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setNew(boolean isNew) { this.isNew = isNew; }
}