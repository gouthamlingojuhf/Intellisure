package com.intellisure.quotepolicyservice.dto;

import com.intellisure.quotepolicyservice.enums.AuditType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record InitiatePremiumAuditRequest(
        @NotNull AuditType auditType,
        @NotNull @PositiveOrZero BigDecimal estimatedExposure,
        @NotNull String exposureBasis
) {}