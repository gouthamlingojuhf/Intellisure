package com.intellisure.claimsservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SubrogationResponse(
        UUID subrogationId,
        UUID claimId,
        String thirdPartyName,
        String thirdPartyInsurance,
        BigDecimal amountClaimed,
        BigDecimal amountRecovered,
        String status,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}