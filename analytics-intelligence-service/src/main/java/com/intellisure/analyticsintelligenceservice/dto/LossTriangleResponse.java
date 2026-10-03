package com.intellisure.analyticsintelligenceservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record LossTriangleResponse(
        UUID triangleId,
        Integer accidentYear,
        Integer developmentYear,
        BigDecimal cumulativeIncurredClaims,
        BigDecimal cumulativePaidClaims,
        BigDecimal caseReserves,
        BigDecimal ibnrReserves,
        LocalDateTime calculatedAt
) {}