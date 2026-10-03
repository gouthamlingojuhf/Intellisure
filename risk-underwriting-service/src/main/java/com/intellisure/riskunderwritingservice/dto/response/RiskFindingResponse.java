package com.intellisure.riskunderwritingservice.dto.response;

import com.intellisure.riskunderwritingservice.enums.ControlStatus;
import com.intellisure.riskunderwritingservice.enums.FindingSeverity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record RiskFindingResponse(

        UUID findingId,

        UUID assessmentId,

        String findingType,

        String description,

        FindingSeverity severity,

        ControlStatus controlStatus,

        List<UUID> evidenceDocumentIds,

        UUID createdBy,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}