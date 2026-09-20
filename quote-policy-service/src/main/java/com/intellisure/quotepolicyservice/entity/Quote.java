package com.intellisure.quotepolicyservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Persistable;
import org.springframework.data.annotation.Transient;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("quote")
public class Quote implements Persistable<UUID> {

    @Transient
    @Builder.Default
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return isNew;
    }

    @Override
    public UUID getId() {
        return quoteId;
    }

    @Id
    private UUID quoteId;

    private UUID customerId;

    private String businessName;

    private String businessType;

    private BigDecimal annualRevenue;

    private Integer employeeCount;

    private BigDecimal requestedCoverageAmount;

    private BigDecimal estimatedPremium;

    private String quoteStatus;

    private LocalDateTime validUntil;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

