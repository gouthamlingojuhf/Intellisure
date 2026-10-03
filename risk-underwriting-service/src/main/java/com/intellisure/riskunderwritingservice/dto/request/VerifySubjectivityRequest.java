package com.intellisure.riskunderwritingservice.dto.request;

import com.intellisure.riskunderwritingservice.enums.SubjectivityStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record VerifySubjectivityRequest(

        @NotNull(message = "Verification status is required")
        SubjectivityStatus status,

        @NotNull(message = "Verified-by user ID is required")
        UUID verifiedBy,

        @NotBlank(message = "Verification note is required")
        @Size(
                max = 5000,
                message = "Verification note must not exceed 5000 characters"
        )
        String verificationNote
) {
}