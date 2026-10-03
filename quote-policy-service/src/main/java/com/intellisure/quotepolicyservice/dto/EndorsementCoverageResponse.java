package com.intellisure.quotepolicyservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record EndorsementCoverageResponse(
        UUID endorsementCoverageId,
        UUID endorsementId,
        String coverageCode,
        String coverageName,
        BigDecimal limitAmount,
        BigDecimal deductibleAmount,
        BigDecimal coveragePremium,
        String conditions,
        String exclusions,
        Integer waitingPeriodDays,
        String operation,
        LocalDateTime createdAt
) {}