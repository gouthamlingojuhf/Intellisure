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
@Table("subrogation")
public class Subrogation {

    @Id
    @Column("subrogation_id")
    private UUID subrogationId;

    @Column("claim_id")
    private UUID claimId;

    @Column("third_party_name")
    private String thirdPartyName;

    @Column("third_party_insurance")
    private String thirdPartyInsurance;

    @Column("amount_claimed")
    private BigDecimal amountClaimed;

    @Column("amount_recovered")
    private BigDecimal amountRecovered;

    @Column("status")
    private String status;

    @Column("notes")
    private String notes;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}