package com.intellisure.riskunderwritingservice.entity;

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
@Table("risk_assessment")
public class RiskAssessment implements Persistable<UUID> {

    @Id
    private UUID assessmentId;

    private UUID quoteId;
    private UUID policyId;
    private UUID customerId;

    private String assessmentType;
    private String status;
    private LocalDate assessmentDate;

    private String location;
    private String businessOperations;
    private BigDecimal riskScore;
    private String summary;

    private UUID createdBy;
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
        return assessmentId;
    }
}
