package com.intellisure.quotepolicyservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.UUID;

public record CompletePremiumAuditRequest(
        @NotNull @PositiveOrZero BigDecimal actualExposure,
        UUID auditedByUserId
) {}