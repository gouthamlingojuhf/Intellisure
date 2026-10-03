package com.intellisure.claimsservice.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record CreateCoverageDecisionRequest(
        @NotBlank String coverageCode,
        @NotBlank String decision,
        String decisionReason,
        UUID decidedBy
) {}