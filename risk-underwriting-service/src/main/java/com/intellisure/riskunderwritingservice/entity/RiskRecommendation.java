package com.intellisure.riskunderwritingservice.entity;

import com.intellisure.riskunderwritingservice.enums.RecommendationPriority;
import com.intellisure.riskunderwritingservice.enums.RecommendationStatus;
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
@Table("risk_recommendation")
public class RiskRecommendation {

    @Id
    @Column("recommendation_id")
    private UUID recommendationId;

    @Column("assessment_id")
    private UUID assessmentId;

    @Column("recommendation_type")
    private String recommendationType;

    @Column("description")
    private String description;

    @Column("priority")
    private RecommendationPriority priority;

    @Column("status")
    private RecommendationStatus status;

    @Column("required_before_bind")
    private Boolean requiredBeforeBind;

    @Column("target_date")
    private LocalDate targetDate;

    @Column("completed_at")
    private LocalDateTime completedAt;

    @Column("verified_at")
    private LocalDateTime verifiedAt;

    @Column("verified_by")
    private UUID verifiedBy;

    @Column("created_by")
    private UUID createdBy;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}