package com.intellisure.quotepolicyservice.dto.response;

import com.intellisure.quotepolicyservice.enums.PolicyStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PolicyResponse(

        UUID policyId,

        String policyNumber,

        UUID quoteId,

        UUID customerId,

        String productCode,

        PolicyStatus status,

        LocalDate startDate,

        LocalDate endDate,

        BigDecimal totalPremium,

        UUID issuedByUserId,

        LocalDateTime boundAt,

        LocalDateTime issuedAt,

        LocalDateTime expiredAt,

        LocalDateTime createdAt,

        LocalDateTime updatedAt,

        List<PolicyCoverageResponse> coverages
) {
}