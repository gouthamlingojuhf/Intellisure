package com.intellisure.quotepolicyservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record InitiateRenewalRequest(
        @NotNull LocalDate proposedStartDate,
        @NotNull LocalDate proposedEndDate,
        @NotNull @PositiveOrZero BigDecimal proposedTotalPremium,
        List<RenewalCoverageRequest> proposedCoverages,
        List<RenewalSubjectivityRequest> subjectivities
) {}