package com.intellisure.claimsservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ClaimFinancialsResponse(
        UUID financialId,
        UUID claimId,
        BigDecimal reserveAmount,
        BigDecimal totalIncurred,
        BigDecimal paidAmount,
        BigDecimal outstandingReserve,
        UUID lastUpdatedBy,
        LocalDateTime lastUpdatedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}