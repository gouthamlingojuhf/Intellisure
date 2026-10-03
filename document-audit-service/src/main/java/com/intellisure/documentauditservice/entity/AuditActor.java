package com.intellisure.documentauditservice.entity;

public enum AuditActor {
    USER,
    SYSTEM,
    SCHEDULER,
    EXTERNAL,
    INTEGRATION,
    BATCH_JOB,
    RULE_ENGINE,
    ML_MODEL
}