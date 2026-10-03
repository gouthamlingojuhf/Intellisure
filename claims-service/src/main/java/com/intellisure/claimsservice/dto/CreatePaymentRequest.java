package com.intellisure.claimsservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public record CreatePaymentRequest(
        @NotBlank String paymentReference,
        @NotNull @Positive BigDecimal amount,
        @NotBlank String paymentType,
        @NotBlank String paymentMethod,
        String referenceNumber,
        String notes,
        UUID createdBy
) {}