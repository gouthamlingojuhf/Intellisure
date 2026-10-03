package com.intellisure.analyticsintelligenceservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ExecutiveDashboardSummaryResponse(
        UUID summaryId,
        BigDecimal totalWrittenPremium,
        BigDecimal totalEarnedPremium,
        BigDecimal totalIncurredLosses,
        BigDecimal lossRatioPercentage,
        BigDecimal claimsFrequency,
        BigDecimal netSubrogationYield,
        Integer activePolicyCount,
        Integer totalClaimsFiled,
        Integer openClaimsCount,
        Integer closedClaimsCount,
        LocalDateTime calculatedAt,
        LocalDateTime periodStart,
        LocalDateTime periodEnd
) {}