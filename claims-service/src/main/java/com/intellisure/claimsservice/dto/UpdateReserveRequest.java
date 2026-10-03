package com.intellisure.claimsservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.UUID;

public record UpdateReserveRequest(
        @NotNull @PositiveOrZero BigDecimal reserveAmount,
        @NotBlank String reason,
        UUID updatedBy
) {}