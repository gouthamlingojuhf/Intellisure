package com.intellisure.quotepolicyservice.dto;

import com.intellisure.quotepolicyservice.enums.RenewalStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record RenewalTransactionResponse(
        UUID renewalId,
        UUID policyId,
        String renewalNumber,
        RenewalStatus status,
        LocalDate proposedStartDate,
        LocalDate proposedEndDate,
        BigDecimal proposedTotalPremium,
        List<RenewalCoverageResponse> proposedCoverages,
        List<RenewalSubjectivityResponse> subjectivities,
        UUID decidedByUserId,
        LocalDateTime decidedAt,
        String decisionReason,
        UUID boundByUserId,
        LocalDateTime boundAt,
        LocalDateTime issuedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}