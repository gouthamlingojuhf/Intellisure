package com.intellisure.recoveryservice.dto;

public record CompleteRecoveryCaseRequest(
        String completionSummary,
        String outcome
) {}