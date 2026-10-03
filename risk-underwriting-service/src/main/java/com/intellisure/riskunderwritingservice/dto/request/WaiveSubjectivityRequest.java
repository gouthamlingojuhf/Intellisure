package com.intellisure.riskunderwritingservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record WaiveSubjectivityRequest(

        @NotNull(message = "Waived-by user ID is required")
        UUID waivedBy,

        @NotBlank(message = "Waiver reason is required")
        @Size(
                max = 5000,
                message = "Waiver reason must not exceed 5000 characters"
        )
        String waiverReason
) {
}