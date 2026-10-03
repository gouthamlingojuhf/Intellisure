package com.intellisure.quotepolicyservice.dto;

import com.intellisure.quotepolicyservice.enums.SubjectivityStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SubjectivityResponse(
        UUID subjectivityId,
        UUID quoteId,
        String subjectivityCode,
        String description,
        SubjectivityStatus status,
        UUID satisfiedByUserId,
        LocalDateTime satisfiedAt,
        List<UUID> evidenceDocumentIds,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}