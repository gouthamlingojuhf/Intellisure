package com.intellisure.claimsservice.dto;

import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateBusinessIncomeRequest(
        @PositiveOrZero BigDecimal coverageLimit,
        Integer waitingPeriodDays,
        Integer restorationPeriodDays,
        LocalDate periodStart,
        LocalDate periodEnd,
        UUID updatedBy
) {}