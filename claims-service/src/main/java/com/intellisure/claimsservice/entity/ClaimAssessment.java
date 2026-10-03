package com.intellisure.claimsservice.entity;

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
@Table("claim_assessment")
public class ClaimAssessment {

    @Id
    @Column("assessment_id")
    private UUID assessmentId;

    @Column("claim_id")
    private UUID claimId;

    @Column("assessor_id")
    private UUID assessorId;

    @Column("assessment_date")
    private LocalDate assessmentDate;

    @Column("findings")
    private String findings;

    @Column("damage_estimate")
    private java.math.BigDecimal damageEstimate;

    @Column("status")
    private String status;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    public UUID getAssessmentId() { return assessmentId; }
    public void setAssessmentId(UUID assessmentId) { this.assessmentId = assessmentId; }
    public UUID getClaimId() { return claimId; }
    public void setClaimId(UUID claimId) { this.claimId = claimId; }
    public UUID getAssessorId() { return assessorId; }
    public void setAssessorId(UUID assessorId) { this.assessorId = assessorId; }
    public LocalDate getAssessmentDate() { return assessmentDate; }
    public void setAssessmentDate(LocalDate assessmentDate) { this.assessmentDate = assessmentDate; }
    public String getFindings() { return findings; }
    public void setFindings(String findings) { this.findings = findings; }
    public java.math.BigDecimal getDamageEstimate() { return damageEstimate; }
    public void setDamageEstimate(java.math.BigDecimal damageEstimate) { this.damageEstimate = damageEstimate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}