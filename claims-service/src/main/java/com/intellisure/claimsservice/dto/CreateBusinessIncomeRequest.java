package com.intellisure.claimsservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateBusinessIncomeRequest(
        @NotNull @PositiveOrZero BigDecimal coverageLimit,
        Integer waitingPeriodDays,
        Integer restorationPeriodDays,
        @NotNull LocalDate periodStart,
        @NotNull LocalDate periodEnd,
        UUID createdBy
) {}