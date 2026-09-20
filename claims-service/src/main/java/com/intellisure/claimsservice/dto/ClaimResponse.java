package com.intellisure.claimsservice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ClaimResponse(
        UUID claimId,
        UUID policyId,
        UUID customerId,
        String claimNumber,
        String status,
        LocalDate incidentDate,
        LocalDate reportedDate,
        String description,
        BigDecimal estimatedLoss,
        BigDecimal payoutAmount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
