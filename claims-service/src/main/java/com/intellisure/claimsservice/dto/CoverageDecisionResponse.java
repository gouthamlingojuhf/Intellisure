package com.intellisure.claimsservice.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CoverageDecisionResponse(
        UUID decisionId,
        UUID claimId,
        String coverageCode,
        String decision,
        String decisionReason,
        UUID decidedBy,
        LocalDateTime decidedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}