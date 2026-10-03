package com.intellisure.recoveryservice.dto;

import com.intellisure.recoveryservice.entity.RecoverySupportStatus;
import com.intellisure.recoveryservice.entity.SupportPriority;
import com.intellisure.recoveryservice.entity.SupportType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record RecoverySupportRequestResponse(
        UUID supportRequestId,
        UUID recoveryCaseId,
        SupportType supportType,
        String description,
        SupportPriority priority,
        com.intellisure.recoveryservice.entity.RecoverySupportStatus status,
        LocalDate requiredByDate,
        String location,
        UUID vendorAssignmentId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}