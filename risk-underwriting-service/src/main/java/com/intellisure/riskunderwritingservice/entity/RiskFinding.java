package com.intellisure.riskunderwritingservice.entity;

import com.intellisure.riskunderwritingservice.enums.ControlStatus;
import com.intellisure.riskunderwritingservice.enums.FindingSeverity;
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
@Table("risk_finding")
public class RiskFinding {

    @Id
    @Column("finding_id")
    private UUID findingId;

    @Column("assessment_id")
    private UUID assessmentId;

    @Column("finding_type")
    private String findingType;

    @Column("description")
    private String description;

    @Column("severity")
    private FindingSeverity severity;

    @Column("control_status")
    private ControlStatus controlStatus;

    @Column("evidence_document_ids")
    private String evidenceDocumentIds;

    @Column("created_by")
    private UUID createdBy;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}