package com.intellisure.riskunderwritingservice.dto.request;

import com.intellisure.riskunderwritingservice.enums.ControlStatus;
import com.intellisure.riskunderwritingservice.enums.FindingSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreateRiskFindingRequest(

        @NotBlank(message = "Finding type is required")
        @Size(max = 100)
        String findingType,

        @NotBlank(message = "Finding description is required")
        @Size(max = 5000)
        String description,

        @NotNull(message = "Finding severity is required")
        FindingSeverity severity,

        @NotNull(message = "Control status is required")
        ControlStatus controlStatus,

        List<UUID> evidenceDocumentIds,

        @NotNull(message = "Created-by user ID is required")
        UUID createdBy
) {
}