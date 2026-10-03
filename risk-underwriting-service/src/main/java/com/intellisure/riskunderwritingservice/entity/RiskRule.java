package com.intellisure.riskunderwritingservice.entity;

import com.intellisure.riskunderwritingservice.enums.RuleActionType;
import com.intellisure.riskunderwritingservice.enums.RuleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("risk_rule")
public class RiskRule {

    @Id
    @Column("rule_id")
    private UUID ruleId;

    @Column("rule_code")
    private String ruleCode;

    @Column("name")
    private String name;

    @Column("description")
    private String description;

    @Column("condition_expression")
    private String conditionExpression;

    @Column("action_type")
    private RuleActionType actionType;

    @Column("action_value")
    private String actionValue;

    @Column("priority")
    private Integer priority;

    @Column("status")
    private RuleStatus status;

    @Column("effective_from")
    private LocalDate effectiveFrom;

    @Column("effective_to")
    private LocalDate effectiveTo;

    @Column("version")
    private Integer version;

    @Column("created_by")
    private UUID createdBy;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}