package com.intellisure.riskunderwritingservice.dto.response;

import com.intellisure.riskunderwritingservice.enums.ReferralStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record UnderwritingReferralResponse(

        UUID referralId,

        UUID assessmentId,

        UUID quoteId,

        String referralReason,

        String requiredAuthorityLevel,

        UUID referredBy,

        UUID referredTo,

        ReferralStatus status,

        String resolutionNote,

        LocalDateTime createdAt,

        LocalDateTime resolvedAt,

        LocalDateTime updatedAt
) {
}