package com.intellisure.quotepolicyservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Persistable;
import org.springframework.data.annotation.Transient;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("policy")
public class Policy implements Persistable<UUID> {

    @Transient
    @Builder.Default
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return isNew;
    }

    @Override
    public UUID getId() {
        return policyId;
    }

    @Id
    private UUID policyId;

    private UUID quoteId;

    private UUID customerId;

    private String policyNumber;

    private String policyStatus;

    private LocalDate effectiveDate;

    private LocalDate expiryDate;

    private BigDecimal totalPremium;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
