package com.intellisure.claimsservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.UUID;

public record UpdateIncurredRequest(
        @NotNull @PositiveOrZero BigDecimal incurredAmount,
        UUID updatedBy
) {}