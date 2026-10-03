package com.intellisure.riskunderwritingservice.entity;

import com.intellisure.riskunderwritingservice.enums.UnderwritingOutcome;
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
@Table("underwriting_decision")
public class UnderwritingDecision {

    @Id
    @Column("decision_id")
    private UUID decisionId;

    @Column("assessment_id")
    private UUID assessmentId;

    @Column("quote_id")
    private UUID quoteId;

    @Column("underwriter_id")
    private UUID underwriterId;

    @Column("outcome")
    private UnderwritingOutcome outcome;

    @Column("decision_rationale")
    private String decisionRationale;

    @Column("authority_level")
    private String authorityLevel;

    @Column("within_authority")
    private Boolean withinAuthority;

    @Column("approved_limit")
    private BigDecimal approvedLimit;

    @Column("approved_deductible")
    private BigDecimal approvedDeductible;

    @Column("indicated_premium")
    private BigDecimal indicatedPremium;

    @Column("conditions")
    private String conditions;

    @Column("subjectivities_outstanding")
    private Boolean subjectivitiesOutstanding;

    @Column("rule_version_reference")
    private String ruleVersionReference;

    @Column("decided_at")
    private LocalDateTime decidedAt;

    @Column("created_at")
    private LocalDateTime createdAt;
}