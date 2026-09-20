package com.intellisure.vendorpartnerservice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record AssignmentResponse(UUID assignmentId, UUID vendorId, UUID claimId, String serviceRequested,
                                 String status, LocalDate assignedDate, LocalDate completedDate,
                                 BigDecimal cost, LocalDateTime createdAt, LocalDateTime updatedAt) {}
