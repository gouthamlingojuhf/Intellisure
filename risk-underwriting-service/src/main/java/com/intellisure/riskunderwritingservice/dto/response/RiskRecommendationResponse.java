package com.intellisure.riskunderwritingservice.dto.response;

import com.intellisure.riskunderwritingservice.enums.RecommendationPriority;
import com.intellisure.riskunderwritingservice.enums.RecommendationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record RiskRecommendationResponse(

        UUID recommendationId,

        UUID assessmentId,

        String recommendationType,

        String description,

        RecommendationPriority priority,

        RecommendationStatus status,

        Boolean requiredBeforeBind,

        LocalDate targetDate,

        LocalDateTime completedAt,

        LocalDateTime verifiedAt,

        UUID verifiedBy,

        UUID createdBy,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}