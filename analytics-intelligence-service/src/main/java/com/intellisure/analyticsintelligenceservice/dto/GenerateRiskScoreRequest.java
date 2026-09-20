package com.intellisure.analyticsintelligenceservice.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record GenerateRiskScoreRequest(
        @NotNull UUID customerId
) {}
