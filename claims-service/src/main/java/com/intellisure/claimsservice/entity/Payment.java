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
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("payment")
public class Payment {

    @Id
    @Column("payment_id")
    private UUID paymentId;

    @Column("claim_id")
    private UUID claimId;

    @Column("payment_reference")
    private String paymentReference;

    @Column("amount")
    private BigDecimal amount;

    @Column("payment_date")
    private LocalDateTime paymentDate;

    @Column("payment_type")
    private String paymentType;

    @Column("payment_method")
    private String paymentMethod;

    @Column("status")
    private String status;

    @Column("reference_number")
    private String referenceNumber;

    @Column("notes")
    private String notes;

    @Column("created_by")
    private UUID createdBy;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}