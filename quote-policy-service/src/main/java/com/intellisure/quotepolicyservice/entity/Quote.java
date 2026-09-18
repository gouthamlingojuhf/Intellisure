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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("quote")
public class Quote {

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

