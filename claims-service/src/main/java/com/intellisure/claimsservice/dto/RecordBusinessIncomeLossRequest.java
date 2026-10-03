package com.intellisure.claimsservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.UUID;

public record RecordBusinessIncomeLossRequest(
        @NotNull @PositiveOrZero BigDecimal estimatedLoss,
        UUID updatedBy
) {}