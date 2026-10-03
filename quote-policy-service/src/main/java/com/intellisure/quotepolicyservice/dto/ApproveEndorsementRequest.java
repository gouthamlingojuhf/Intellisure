package com.intellisure.quotepolicyservice.dto;

import com.intellisure.quotepolicyservice.enums.EndorsementStatus;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record ApproveEndorsementRequest(
        @NotBlank EndorsementStatus status,
        String decisionReason,
        UUID approvedByUserId
) {}