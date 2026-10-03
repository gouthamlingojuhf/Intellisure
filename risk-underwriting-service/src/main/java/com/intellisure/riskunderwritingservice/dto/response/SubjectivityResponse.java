package com.intellisure.riskunderwritingservice.dto.response;

import com.intellisure.riskunderwritingservice.enums.SubjectivityStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SubjectivityResponse(

        UUID subjectivityId,

        UUID assessmentId,

        UUID quoteId,

        String subjectivityType,

        String description,

        Boolean requiredBeforeBind,

        SubjectivityStatus status,

        LocalDate dueDate,

        List<UUID> evidenceDocumentIds,

        LocalDateTime submittedAt,

        LocalDateTime satisfiedAt,

        UUID verifiedBy,

        String verificationNote,

        UUID createdBy,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}