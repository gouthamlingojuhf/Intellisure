package com.intellisure.quotepolicyservice.entity;

import com.intellisure.quotepolicyservice.enums.SubjectivityStatus;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Table("subjectivity")
public class Subjectivity {

    @Id
    @Column("subjectivity_id")
    private UUID subjectivityId;

    @Column("quote_id")
    private UUID quoteId;

    @Column("subjectivity_code")
    private String subjectivityCode;

    @Column("description")
    private String description;

    @Column("status")
    private SubjectivityStatus status;

    @Column("satisfied_by_user_id")
    private UUID satisfiedByUserId;

    @Column("satisfied_at")
    private LocalDateTime satisfiedAt;

    @Column("evidence_document_ids")
    private String evidenceDocumentIds;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    public UUID getSubjectivityId() { return subjectivityId; }
    public void setSubjectivityId(UUID subjectivityId) { this.subjectivityId = subjectivityId; }
    public UUID getQuoteId() { return quoteId; }
    public void setQuoteId(UUID quoteId) { this.quoteId = quoteId; }
    public String getSubjectivityCode() { return subjectivityCode; }
    public void setSubjectivityCode(String subjectivityCode) { this.subjectivityCode = subjectivityCode; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public SubjectivityStatus getStatus() { return status; }
    public void setStatus(SubjectivityStatus status) { this.status = status; }
    public UUID getSatisfiedByUserId() { return satisfiedByUserId; }
    public void setSatisfiedByUserId(UUID satisfiedByUserId) { this.satisfiedByUserId = satisfiedByUserId; }
    public LocalDateTime getSatisfiedAt() { return satisfiedAt; }
    public void setSatisfiedAt(LocalDateTime satisfiedAt) { this.satisfiedAt = satisfiedAt; }
    public String getEvidenceDocumentIds() { return evidenceDocumentIds; }
    public void setEvidenceDocumentIds(String evidenceDocumentIds) { this.evidenceDocumentIds = evidenceDocumentIds; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}