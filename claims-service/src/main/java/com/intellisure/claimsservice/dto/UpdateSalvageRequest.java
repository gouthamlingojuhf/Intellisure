package com.intellisure.claimsservice.dto;

import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateSalvageRequest(
        String description,
        BigDecimal estimatedValue,
        String status,
        String buyer,
        LocalDate saleDate,
        BigDecimal saleAmount,
        UUID updatedBy
) {}