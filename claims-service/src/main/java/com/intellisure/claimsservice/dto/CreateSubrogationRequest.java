package com.intellisure.claimsservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateSubrogationRequest(
        @NotBlank String thirdPartyName,
        String thirdPartyInsurance,
        @NotNull @PositiveOrZero BigDecimal amountClaimed,
        String notes,
        UUID createdBy
) {}