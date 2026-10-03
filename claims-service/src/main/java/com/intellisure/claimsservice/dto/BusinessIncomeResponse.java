package com.intellisure.claimsservice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record BusinessIncomeResponse(
        UUID businessIncomeId,
        UUID claimId,
        BigDecimal coverageLimit,
        Integer waitingPeriodDays,
        Integer restorationPeriodDays,
        LocalDate periodOfIndemnityStart,
        LocalDate periodOfIndemnityEnd,
        BigDecimal estimatedLoss,
        BigDecimal actualLoss,
        Boolean coverageConfirmed,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}