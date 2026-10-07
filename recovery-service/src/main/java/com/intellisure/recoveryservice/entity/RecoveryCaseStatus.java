package com.intellisure.recoveryservice.entity;

public enum RecoveryCaseStatus {
    INITIATED,
    ASSESSING_IMPACT,
    PLANNING,
    IN_PROGRESS,
    BUSINESS_PARTIALLY_RESTORED,
    BUSINESS_RESTORED,
    ON_HOLD,
    COMPLETED,
    CANCELLED,
    REOPENED
}