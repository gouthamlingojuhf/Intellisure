package com.intellisure.claimsservice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record SalvageResponse(
        UUID salvageId,
        UUID claimId,
        String description,
        BigDecimal estimatedValue,
        BigDecimal actualValue,
        String status,
        String buyer,
        LocalDate saleDate,
        BigDecimal saleAmount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}