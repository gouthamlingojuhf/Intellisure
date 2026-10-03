package com.intellisure.quotepolicyservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeclineQuoteRequest(

        @NotBlank(message = "Decline reason is required")
        @Size(
                max = 5000,
                message = "Decline reason must not exceed 5000 characters"
        )
        String reason
) {
}