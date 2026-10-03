package com.intellisure.quotepolicyservice.entity;

import com.intellisure.quotepolicyservice.enums.UnderwritingDecisionType;
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
@Table("underwriting_decision")
public class UnderwritingDecision {

    @Id
    @Column("underwriting_decision_id")
    private UUID underwritingDecisionId;

    @Column("source_decision_id")
    private UUID sourceDecisionId;

    @Column("quote_id")
    private UUID quoteId;

    @Column("underwriter_id")
    private UUID underwriterId;

    @Column("decision")
    private UnderwritingDecisionType decision;

    @Column("decision_reason")
    private String decisionReason;

    @Column("authority_level")
    private String authorityLevel;

    @Column("conditions")
    private String conditions;

    @Column("decided_at")
    private LocalDateTime decidedAt;

    @Column("created_at")
    private LocalDateTime createdAt;
}