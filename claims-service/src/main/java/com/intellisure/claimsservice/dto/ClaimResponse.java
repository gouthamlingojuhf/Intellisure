package com.intellisure.claimsservice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ClaimResponse(
        UUID claimId,
        UUID policyId,
        UUID customerId,
        String policyNumber,
        String claimNumber,
        String status,
        LocalDate incidentDate,
        LocalDate reportedDate,
        String description,
        String incidentDescription,
        BigDecimal estimatedLoss,
        BigDecimal estimatedCoveredLoss,
        BigDecimal payoutAmount,
        BigDecimal payableAmount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String incidentType,
        String incidentLocation,
        String coverageDecision,
        String coverageDecisionReason,
        UUID assignedAdjusterId,
        Boolean coverageConfirmed,
        String closureReason
) {
    public ClaimResponse(
            UUID claimId,
            UUID policyId,
            UUID customerId,
            String claimNumber,
            String status,
            LocalDate incidentDate,
            LocalDate reportedDate,
            String description,
            BigDecimal estimatedLoss,
            BigDecimal payoutAmount,
            BigDecimal payableAmount,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String incidentType,
            String incidentLocation,
            String coverageDecision,
            UUID assignedAdjusterId,
            Boolean coverageConfirmed,
            String closureReason
    ) {
        this(
                claimId,
                policyId,
                customerId,
                null,
                claimNumber,
                status,
                incidentDate,
                reportedDate,
                description,
                null,
                estimatedLoss,
                null,
                payoutAmount,
                payableAmount,
                createdAt,
                updatedAt,
                incidentType,
                incidentLocation,
                coverageDecision,
                null,
                assignedAdjusterId,
                coverageConfirmed,
                closureReason
        );
    }

    public ClaimResponse(
            UUID claimId,
            UUID policyId,
            UUID customerId,
            String claimNumber,
            String status,
            LocalDate incidentDate,
            LocalDate reportedDate,
            String description,
            BigDecimal estimatedLoss,
            BigDecimal payoutAmount,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this(
                claimId,
                policyId,
                customerId,
                null,
                claimNumber,
                status,
                incidentDate,
                reportedDate,
                description,
                null,
                estimatedLoss,
                null,
                payoutAmount,
                payoutAmount,
                createdAt,
                updatedAt,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}
