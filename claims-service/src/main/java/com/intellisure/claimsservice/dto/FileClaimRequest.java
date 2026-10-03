package com.intellisure.claimsservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record FileClaimRequest(
        @NotNull UUID policyId,
        @NotNull LocalDate incidentDate,
        @NotBlank String description,
        BigDecimal estimatedLoss
) {}
