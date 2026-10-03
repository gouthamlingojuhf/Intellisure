package com.intellisure.riskunderwritingservice.entity;

import com.intellisure.riskunderwritingservice.enums.RiskAssessmentStatus;
import com.intellisure.riskunderwritingservice.enums.RiskBand;
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
@Table("risk_assessment")
public class RiskAssessment {

    @Id
    @Column("assessment_id")
    private UUID assessmentId;

    @Column("assessment_number")
    private String assessmentNumber;

    @Column("quote_id")
    private UUID quoteId;

    @Column("policy_id")
    private UUID policyId;

    @Column("customer_id")
    private UUID customerId;

    @Column("assessment_type")
    private String assessmentType;

    @Column("status")
    private RiskAssessmentStatus status;

    @Column("assessment_date")
    private LocalDate assessmentDate;

    @Column("location")
    private String location;

    @Column("business_operations")
    private String businessOperations;

    @Column("annual_revenue")
    private BigDecimal annualRevenue;

    @Column("annual_payroll")
    private BigDecimal annualPayroll;

    @Column("employee_count")
    private Integer employeeCount;

    @Column("asset_value")
    private BigDecimal assetValue;

    @Column("prior_claim_count")
    private Integer priorClaimCount;

    @Column("prior_loss_amount")
    private BigDecimal priorLossAmount;

    @Column("risk_score")
    private BigDecimal riskScore;

    @Column("risk_band")
    private RiskBand riskBand;

    @Column("summary")
    private String summary;

    @Column("created_by")
    private UUID createdBy;

    @Column("assigned_underwriter_id")
    private UUID assignedUnderwriterId;

    @Column("assigned_risk_engineer_id")
    private UUID assignedRiskEngineerId;

    @Column("submitted_at")
    private LocalDateTime submittedAt;

    @Column("completed_at")
    private LocalDateTime completedAt;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}