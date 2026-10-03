package com.intellisure.quotepolicyservice.dto;

import com.intellisure.quotepolicyservice.enums.EndorsementType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record RequestEndorsementRequest(
        @NotNull EndorsementType endorsementType,
        @NotBlank String description,
        @NotNull LocalDate effectiveFrom,
        LocalDate effectiveTo,
        @NotNull @PositiveOrZero BigDecimal premiumDelta,
        @NotNull List<EndorsementCoverageRequest> coverages
) {}