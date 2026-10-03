package com.intellisure.claimsservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.UUID;

public record CalculateBusinessIncomeLossRequest(
        @NotNull @Positive BigDecimal grossRevenue,
        @NotNull @Positive Integer daysAffected,
        UUID updatedBy
) {}