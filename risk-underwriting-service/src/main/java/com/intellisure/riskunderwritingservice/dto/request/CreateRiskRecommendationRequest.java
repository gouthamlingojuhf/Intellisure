package com.intellisure.riskunderwritingservice.dto.request;

import com.intellisure.riskunderwritingservice.enums.RecommendationPriority;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record CreateRiskRecommendationRequest(

        @NotBlank(message = "Recommendation type is required")
        @Size(max = 100)
        String recommendationType,

        @NotBlank(message = "Recommendation description is required")
        @Size(max = 5000)
        String description,

        @NotNull(message = "Recommendation priority is required")
        RecommendationPriority priority,

        @NotNull(message = "Required-before-bind value is required")
        Boolean requiredBeforeBind,

        @FutureOrPresent(
                message = "Target date cannot be in the past"
        )
        LocalDate targetDate,

        @NotNull(message = "Created-by user ID is required")
        UUID createdBy
) {
}