package com.intellisure.claimsservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RecordSalvageSaleRequest(
        @NotBlank String buyer,
        @NotNull LocalDate saleDate,
        @NotNull @Positive BigDecimal saleAmount,
        UUID updatedBy
) {}