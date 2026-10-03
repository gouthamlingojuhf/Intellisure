package com.intellisure.quotepolicyservice.entity;

import com.intellisure.quotepolicyservice.enums.SubjectivityStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

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

    public Subjectivity() {}

    public Subjectivity(UUID subjectivityId, UUID quoteId, String subjectivityCode, String description,
                        SubjectivityStatus status, UUID satisfiedByUserId, LocalDateTime satisfiedAt,
                        String evidenceDocumentIds, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.subjectivityId = subjectivityId;
        this.quoteId = quoteId;
        this.subjectivityCode = subjectivityCode;
        this.description = description;
        this.status = status;
        this.satisfiedByUserId = satisfiedByUserId;
        this.satisfiedAt = satisfiedAt;
        this.evidenceDocumentIds = evidenceDocumentIds;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static SubjectivityBuilder builder() {
        return new SubjectivityBuilder();
    }

    public static class SubjectivityBuilder {
        private UUID subjectivityId;
        private UUID quoteId;
        private String subjectivityCode;
        private String description;
        private SubjectivityStatus status;
        private UUID satisfiedByUserId;
        private LocalDateTime satisfiedAt;
        private String evidenceDocumentIds;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public SubjectivityBuilder subjectivityId(UUID subjectivityId) { this.subjectivityId = subjectivityId; return this; }
        public SubjectivityBuilder quoteId(UUID quoteId) { this.quoteId = quoteId; return this; }
        public SubjectivityBuilder subjectivityCode(String subjectivityCode) { this.subjectivityCode = subjectivityCode; return this; }
        public SubjectivityBuilder description(String description) { this.description = description; return this; }
        public SubjectivityBuilder status(SubjectivityStatus status) { this.status = status; return this; }
        public SubjectivityBuilder satisfiedByUserId(UUID satisfiedByUserId) { this.satisfiedByUserId = satisfiedByUserId; return this; }
        public SubjectivityBuilder satisfiedAt(LocalDateTime satisfiedAt) { this.satisfiedAt = satisfiedAt; return this; }
        public SubjectivityBuilder evidenceDocumentIds(String evidenceDocumentIds) { this.evidenceDocumentIds = evidenceDocumentIds; return this; }
        public SubjectivityBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public SubjectivityBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public Subjectivity build() { return new Subjectivity(subjectivityId, quoteId, subjectivityCode, description, status, satisfiedByUserId, satisfiedAt, evidenceDocumentIds, createdAt, updatedAt); }
    }

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