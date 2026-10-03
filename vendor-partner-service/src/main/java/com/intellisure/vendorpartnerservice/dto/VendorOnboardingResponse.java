package com.intellisure.vendorpartnerservice.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record VendorOnboardingResponse(
        UUID onboardingRequestId,
        UUID vendorId,
        String status,
        LocalDateTime submittedAt,
        LocalDateTime reviewedAt,
        UUID reviewerId,
        String rejectionReason
) {}