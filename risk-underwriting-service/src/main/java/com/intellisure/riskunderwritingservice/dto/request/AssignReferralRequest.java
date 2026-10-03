package com.intellisure.riskunderwritingservice.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignReferralRequest(

        @NotNull(message = "Referred-to user ID is required")
        UUID referredTo
) {
}