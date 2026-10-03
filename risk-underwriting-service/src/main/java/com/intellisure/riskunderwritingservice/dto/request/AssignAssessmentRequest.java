package com.intellisure.riskunderwritingservice.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignAssessmentRequest(

        @NotNull(message = "Underwriter ID is required")
        UUID underwriterId,

        UUID riskEngineerId
) {
}