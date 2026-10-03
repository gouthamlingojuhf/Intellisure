package com.intellisure.analyticsintelligenceservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record RiskScoreResponse(
        UUID snapshotId,
        UUID customerId,
        BigDecimal riskScore,
        String riskBand,
        String keyFactors,
        String modelVersion,
        LocalDateTime generatedAt
) {}
