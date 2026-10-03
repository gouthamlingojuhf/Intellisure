package com.intellisure.quotepolicyservice.dto.request;

import com.intellisure.quotepolicyservice.enums.UnderwritingDecisionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RecordUnderwritingDecisionRequest(

        @NotNull(message = "Underwriting decision is required")
        UnderwritingDecisionType decision,

        @NotBlank(message = "Decision reason is required")
        @Size(
                max = 5000,
                message = "Decision reason must not exceed 5000 characters"
        )
        String decisionReason,

        @Size(
                max = 100,
                message = "Authority level must not exceed 100 characters"
        )
        String authorityLevel,

        @Size(
                max = 5000,
                message = "Conditions must not exceed 5000 characters"
        )
        String conditions
) {
}