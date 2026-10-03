package com.intellisure.claimsservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("coverage_decision")
public class CoverageDecision {

    @Id
    @Column("decision_id")
    private UUID decisionId;

    @Column("claim_id")
    private UUID claimId;

    @Column("coverage_code")
    private String coverageCode;

    @Column("decision")
    private String decision;

    @Column("decision_reason")
    private String decisionReason;

    @Column("decided_by")
    private UUID decidedBy;

    @Column("decided_at")
    private LocalDateTime decidedAt;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}