package com.intellisure.quotepolicyservice.dto.response;

import com.intellisure.quotepolicyservice.enums.UnderwritingDecisionType;

import java.time.LocalDateTime;
import java.util.UUID;

public record UnderwritingDecisionResponse(

        UUID underwritingDecisionId,

        UUID quoteId,

        UUID underwriterId,

        UnderwritingDecisionType decision,

        String decisionReason,

        String authorityLevel,

        String conditions,

        LocalDateTime decidedAt,

        LocalDateTime createdAt
) {
}