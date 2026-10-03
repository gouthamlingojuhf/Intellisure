package com.intellisure.quotepolicyservice.entity;

import com.intellisure.quotepolicyservice.enums.*;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("endorsement")
@Getter
@Setter
public class Endorsement {

    @Id
    @Column("endorsement_id")
    private UUID endorsementId;

    @Column("policy_id")
    private UUID policyId;

    @Column("endorsement_number")
    private String endorsementNumber;

    @Column("endorsement_type")
    private EndorsementType endorsementType;

    @Column("description")
    private String description;

    @Column("premium_delta")
    private BigDecimal premiumDelta;

    @Column("status")
    private EndorsementStatus status;

    @Column("requested_by_user_id")
    private UUID requestedByUserId;

    @Column("approved_by_user_id")
    private UUID approvedByUserId;

    @Column("effective_from")
    private LocalDate effectiveFrom;

    @Column("effective_to")
    private LocalDate effectiveTo;

    @Column("requested_at")
    private LocalDateTime requestedAt;

    @Column("approved_at")
    private LocalDateTime approvedAt;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

}