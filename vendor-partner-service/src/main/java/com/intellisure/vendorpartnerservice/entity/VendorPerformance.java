package com.intellisure.vendorpartnerservice.entity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Table("vendor_performance")
public class VendorPerformance implements Persistable<UUID> {

    @Id
    @Column("performance_id")
    private UUID performanceId;

    @Column("vendor_id")
    private UUID vendorId;

    @Column("assignment_id")
    private UUID assignmentId;

    @Column("quality_score")
    private BigDecimal qualityScore;

    @Column("timeliness_score")
    private BigDecimal timelinessScore;

    @Column("communication_score")
    private BigDecimal communicationScore;

    @Column("outcome_score")
    private BigDecimal outcomeScore;

    @Column("overall_score")
    private BigDecimal overallScore;

    private String note;
    
    @Column("recorded_at")
    private LocalDateTime recordedAt;

    @Transient
    private boolean isNew = true;

    public static VendorPerformance builder() {
        return new VendorPerformance();
    }
    
    public VendorPerformance performanceId(UUID performanceId) { this.performanceId = performanceId; return this; }
    public VendorPerformance vendorId(UUID vendorId) { this.vendorId = vendorId; return this; }
    public VendorPerformance assignmentId(UUID assignmentId) { this.assignmentId = assignmentId; return this; }
    public VendorPerformance qualityScore(BigDecimal qualityScore) { this.qualityScore = qualityScore; return this; }
    public VendorPerformance timelinessScore(BigDecimal timelinessScore) { this.timelinessScore = timelinessScore; return this; }
    public VendorPerformance communicationScore(BigDecimal communicationScore) { this.communicationScore = communicationScore; return this; }
    public VendorPerformance outcomeScore(BigDecimal outcomeScore) { this.outcomeScore = outcomeScore; return this; }
    public VendorPerformance overallScore(BigDecimal overallScore) { this.overallScore = overallScore; return this; }
    public VendorPerformance note(String note) { this.note = note; return this; }
    public VendorPerformance recordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; return this; }
    public VendorPerformance isNew(boolean isNew) { this.isNew = isNew; return this; }
    
    public VendorPerformance build() { return this; }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @Override
    public UUID getId() {
        return performanceId;
    }

    public UUID getPerformanceId() { return performanceId; }
    public void setPerformanceId(UUID performanceId) { this.performanceId = performanceId; }
    public UUID getVendorId() { return vendorId; }
    public void setVendorId(UUID vendorId) { this.vendorId = vendorId; }
    public UUID getAssignmentId() { return assignmentId; }
    public void setAssignmentId(UUID assignmentId) { this.assignmentId = assignmentId; }
    public BigDecimal getQualityScore() { return qualityScore; }
    public void setQualityScore(BigDecimal qualityScore) { this.qualityScore = qualityScore; }
    public BigDecimal getTimelinessScore() { return timelinessScore; }
    public void setTimelinessScore(BigDecimal timelinessScore) { this.timelinessScore = timelinessScore; }
    public BigDecimal getCommunicationScore() { return communicationScore; }
    public void setCommunicationScore(BigDecimal communicationScore) { this.communicationScore = communicationScore; }
    public BigDecimal getOutcomeScore() { return outcomeScore; }
    public void setOutcomeScore(BigDecimal outcomeScore) { this.outcomeScore = outcomeScore; }
    public BigDecimal getOverallScore() { return overallScore; }
    public void setOverallScore(BigDecimal overallScore) { this.overallScore = overallScore; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
    public void setNew(boolean isNew) { this.isNew = isNew; }
}