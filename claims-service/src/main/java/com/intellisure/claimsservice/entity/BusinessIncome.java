package com.intellisure.claimsservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("business_income")
public class BusinessIncome {

    @Id
    @Column("business_income_id")
    private UUID businessIncomeId;

    @Column("claim_id")
    private UUID claimId;

    @Column("coverage_limit")
    private BigDecimal coverageLimit;

    @Column("waiting_period_days")
    private Integer waitingPeriodDays;

    @Column("restoration_period_days")
    private Integer restorationPeriodDays;

    @Column("period_of_indemnity_start")
    private LocalDate periodOfIndemnityStart;

    @Column("period_of_indemnity_end")
    private LocalDate periodOfIndemnityEnd;

    @Column("estimated_loss")
    private BigDecimal estimatedLoss;

    @Column("actual_loss")
    private BigDecimal actualLoss;

    @Column("coverage_confirmed")
    private Boolean coverageConfirmed;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}