package com.intellisure.riskunderwritingservice.dto.request;

import com.intellisure.riskunderwritingservice.enums.ReferralStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResolveReferralRequest(

        @NotNull(message = "Referral resolution status is required")
        ReferralStatus status,

        @NotBlank(message = "Resolution note is required")
        @Size(
                max = 5000,
                message = "Resolution note must not exceed 5000 characters"
        )
        String resolutionNote
) {
}