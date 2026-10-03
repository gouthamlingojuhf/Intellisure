package com.intellisure.vendorpartnerservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record VendorPerformanceResponse(
        UUID vendorId,
        UUID assignmentId,
        BigDecimal qualityScore,
        BigDecimal timelinessScore,
        BigDecimal communicationScore,
        BigDecimal outcomeScore,
        BigDecimal overallScore,
        String note,
        LocalDateTime recordedAt
) {}