package com.intellisure.quotepolicyservice.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record RenewalDecisionRequest(
        @NotBlank String status,
        String decisionReason,
        UUID decidedByUserId
) {}