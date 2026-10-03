package com.intellisure.workflownotificationservice.entity;

public enum WorkflowStatus {
    INITIATED,
    IN_PROGRESS,
    PENDING_APPROVAL,
    PENDING_REVIEW,
    COMPLETED,
    REJECTED,
    EXPIRED,
    CANCELLED
}