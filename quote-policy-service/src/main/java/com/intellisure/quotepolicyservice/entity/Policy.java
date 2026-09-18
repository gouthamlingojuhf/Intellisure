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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("policy")
public class Policy {

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
