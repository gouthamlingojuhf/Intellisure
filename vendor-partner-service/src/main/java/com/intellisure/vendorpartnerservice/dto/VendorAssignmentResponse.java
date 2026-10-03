package com.intellisure.vendorpartnerservice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record VendorAssignmentResponse(
        UUID assignmentId,
        UUID vendorId,
        String assignmentType,
        UUID claimId,
        UUID recoveryCaseId,
        String status,
        String taskDescription,
        LocalDate dueDate,
        String priority,
        LocalDateTime acceptedAt,
        LocalDateTime completedAt,
        List<UUID> evidenceDocumentIds,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}