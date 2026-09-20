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
@Table("quote_coverage")
public class QuoteCoverage implements Persistable<UUID> {

    @Transient
    @Builder.Default
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return isNew;
    }

    @Override
    public UUID getId() {
        return quoteCoverageId;
    }

    @Id
    private UUID quoteCoverageId;

    private UUID quoteId;

    private String coverageType;

    private BigDecimal coverageLimit;

    private BigDecimal deductible;

    private LocalDateTime createdAt;
}