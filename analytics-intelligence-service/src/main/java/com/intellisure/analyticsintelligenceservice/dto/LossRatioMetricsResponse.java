package com.intellisure.analyticsintelligenceservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record LossRatioMetricsResponse(
        UUID metricsId,
        BigDecimal totalEarnedPremium,
        BigDecimal totalIncurredClaims,
        BigDecimal lossAdjustmentExpenses,
        BigDecimal lossRatioPercentage,
        Integer activePolicyCount,
        Integer totalClaimsFiled,
        LocalDateTime calculatedAt,
        LocalDateTime periodStart,
        LocalDateTime periodEnd
) {}