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
@Table("quote_coverage")
public class QuoteCoverage {

    @Id
    private UUID quoteCoverageId;

    private UUID quoteId;

    private String coverageType;

    private BigDecimal coverageLimit;

    private BigDecimal deductible;

    private LocalDateTime createdAt;
}