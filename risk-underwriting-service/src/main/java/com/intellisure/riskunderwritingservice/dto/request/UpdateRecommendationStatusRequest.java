package com.intellisure.riskunderwritingservice.dto.request;

import com.intellisure.riskunderwritingservice.enums.RecommendationStatus;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateRecommendationStatusRequest(

        @NotNull(message = "Recommendation status is required")
        RecommendationStatus status,

        UUID verifiedBy
) {
}
