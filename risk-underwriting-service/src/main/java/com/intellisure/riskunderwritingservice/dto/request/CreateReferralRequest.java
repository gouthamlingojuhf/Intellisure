package com.intellisure.riskunderwritingservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateReferralRequest(

        @NotBlank(message = "Referral reason is required")
        @Size(
                max = 5000,
                message = "Referral reason must not exceed 5000 characters"
        )
        String referralReason,

        @NotBlank(message = "Required authority level is required")
        @Size(
                max = 100,
                message = "Authority level must not exceed 100 characters"
        )
        String requiredAuthorityLevel,

        @NotNull(message = "Referred-by user ID is required")
        UUID referredBy
) {
}