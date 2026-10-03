package com.intellisure.riskunderwritingservice.entity;

import com.intellisure.riskunderwritingservice.enums.SubjectivityStatus;
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
@Table("subjectivity")
public class Subjectivity {

    @Id
    @Column("subjectivity_id")
    private UUID subjectivityId;

    @Column("assessment_id")
    private UUID assessmentId;

    @Column("quote_id")
    private UUID quoteId;

    @Column("subjectivity_type")
    private String subjectivityType;

    @Column("description")
    private String description;

    @Column("required_before_bind")
    private Boolean requiredBeforeBind;

    @Column("status")
    private SubjectivityStatus status;

    @Column("due_date")
    private LocalDate dueDate;

    @Column("evidence_document_ids")
    private String evidenceDocumentIds;

    @Column("submitted_at")
    private LocalDateTime submittedAt;

    @Column("satisfied_at")
    private LocalDateTime satisfiedAt;

    @Column("verified_by")
    private UUID verifiedBy;

    @Column("verification_note")
    private String verificationNote;

    @Column("created_by")
    private UUID createdBy;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}