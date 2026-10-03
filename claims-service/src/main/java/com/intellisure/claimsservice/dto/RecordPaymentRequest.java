package com.intellisure.claimsservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public record RecordPaymentRequest(
        @NotNull @Positive BigDecimal amount,
        UUID updatedBy
) {}