package com.intellisure.quotepolicyservice.dto;

import com.intellisure.quotepolicyservice.enums.EndorsementType;
import com.intellisure.quotepolicyservice.enums.EndorsementStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record EndorsementResponse(
        UUID endorsementId,
        UUID policyId,
        String endorsementNumber,
        EndorsementType endorsementType,
        String description,
        BigDecimal premiumDelta,
        EndorsementStatus status,
        UUID requestedByUserId,
        UUID approvedByUserId,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        LocalDateTime requestedAt,
        LocalDateTime approvedAt,
        List<EndorsementCoverageResponse> coverages,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}