package com.intellisure.vendorpartnerservice.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record RecordVendorPerformanceRequest(
        @NotNull UUID assignmentId,
        @NotNull BigDecimal qualityScore,
        @NotNull BigDecimal timelinessScore,
        @NotNull BigDecimal communicationScore,
        @NotNull BigDecimal outcomeScore,
        String note
) {}