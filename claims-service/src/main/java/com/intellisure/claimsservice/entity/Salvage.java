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
@Table("salvage")
public class Salvage {

    @Id
    @Column("salvage_id")
    private UUID salvageId;

    @Column("claim_id")
    private UUID claimId;

    @Column("description")
    private String description;

    @Column("estimated_value")
    private BigDecimal estimatedValue;

    @Column("actual_value")
    private BigDecimal actualValue;

    @Column("status")
    private String status;

    @Column("buyer")
    private String buyer;

    @Column("sale_date")
    private LocalDate saleDate;

    @Column("sale_amount")
    private BigDecimal saleAmount;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}