package com.intellisure.claimsservice.dto;

import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.UUID;

public record UpdateSubrogationRequest(
        String thirdPartyName,
        String thirdPartyInsurance,
        BigDecimal amountClaimed,
        String status,
        String notes,
        UUID updatedBy
) {}