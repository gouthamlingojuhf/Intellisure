package com.intellisure.recoveryservice.dto;

import com.intellisure.recoveryservice.entity.RecoverySeverity;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record RecoveryEstimationRequest(
        @NotNull UUID claimId,
        @NotNull UUID customerId,
        @NotNull BigDecimal claimPayoutAmount,
        @NotNull Double faultPercentage,
        @NotNull RecoverySeverity severity,
        BigDecimal thirdPartyPolicyLimit,
        BigDecimal estimatedSalvageValue
) {}