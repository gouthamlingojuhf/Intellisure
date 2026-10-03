package com.intellisure.claimsservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(
        UUID paymentId,
        UUID claimId,
        String paymentReference,
        BigDecimal amount,
        LocalDateTime paymentDate,
        String paymentType,
        String paymentMethod,
        String status,
        String referenceNumber,
        String notes,
        UUID createdBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}