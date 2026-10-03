package com.intellisure.quotepolicyservice.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignUnderwriterRequest(

        @NotNull(message = "Underwriter ID is required")
        UUID underwriterId
) {
}