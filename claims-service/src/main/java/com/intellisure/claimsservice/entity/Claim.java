package com.intellisure.claimsservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("claim")
public class Claim implements Persistable<UUID> {

    @Id
    private UUID claimId;

    private UUID policyId;
    private UUID customerId;
    private String policyNumber;

    private String claimNumber;
    private String status;
    private String incidentType;
    private String incidentDescription;
    private LocalDate incidentDate;
    private LocalDate reportedDate;
    private String incidentLocation;
    private String priority;
    private UUID assignedAdjusterId;

    private String description;
    private BigDecimal estimatedLoss;
    private BigDecimal estimatedCoveredLoss;
    private Boolean coverageConfirmed;
    private String coverageDecision;
    private String coverageDecisionReason;
    private String closureReason;
    private BigDecimal payoutAmount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Transient
    @Builder.Default
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return isNew;
    }

    @Override
    public UUID getId() {
        return claimId;
    }
}